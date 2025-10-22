# 💡 Contoh Implementasi - Register User Flow

Ini contoh lengkap implementasi fitur Register User dari ujung ke ujung.

## 1. DTO Request & Response

### RegisterRequest.kt
```kotlin
package com.gity.features.auth.models.dtos.requests

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String,
    val phoneNumber: String? = null
)
```

### AuthResponse.kt
```kotlin
package com.gity.features.auth.models.dtos.response

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserProfileResponse
)

@Serializable
data class UserProfileResponse(
    val id: String,
    val email: String,
    val fullName: String,
    val phoneNumber: String?,
    val role: String,
    val isEmailVerified: Boolean,
    val profilePictureUrl: String?
)
```

## 2. Repository

### UserRepository.kt
```kotlin
package com.gity.features.auth.repositories

import com.gity.config.DatabaseConfig
import com.gity.shared.models.User
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates
import org.bson.types.ObjectId

class UserRepository(
    private val databaseConfig: DatabaseConfig
) {
    private val collection = databaseConfig.getCollection<User>("users")

    suspend fun findByEmail(email: String): User? {
        return collection.find(eq("email", email)).firstOrNull()
    }

    suspend fun findById(userId: ObjectId): User? {
        return collection.find(eq("_id", userId)).firstOrNull()
    }

    suspend fun create(user: User): User {
        collection.insertOne(user)
        return user
    }

    suspend fun updateEmailVerificationStatus(userId: ObjectId, isVerified: Boolean): Boolean {
        val result = collection.updateOne(
            eq("_id", userId),
            Updates.combine(
                Updates.set("isEmailVerified", isVerified),
                Updates.set("emailVerificationToken", null),
                Updates.set("updatedAt", java.time.Instant.now())
            )
        )
        return result.modifiedCount > 0
    }

    suspend fun existsByEmail(email: String): Boolean {
        return collection.countDocuments(eq("email", email)) > 0
    }
}
```

## 3. Service

### AuthService.kt
```kotlin
package com.gity.features.auth.services

import com.gity.features.auth.models.dtos.requests.RegisterRequest
import com.gity.features.auth.models.dtos.response.AuthResponse
import com.gity.features.auth.models.dtos.response.UserProfileResponse
import com.gity.features.auth.repositories.UserRepository
import com.gity.shared.models.User
import com.gity.shared.models.enums.UserRole
import com.gity.shared.utils.EmailUtil
import com.gity.shared.utils.HashingUtil
import com.gity.shared.utils.JwtUtil

class AuthService(
    private val userRepository: UserRepository,
    private val hashingUtil: HashingUtil,
    private val jwtUtil: JwtUtil,
    private val emailUtil: EmailUtil
) {

    suspend fun register(request: RegisterRequest): AuthResponse {
        // 1. Validasi input
        validateRegisterRequest(request)

        // 2. Cek apakah email sudah terdaftar
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already registered")
        }

        // 3. Hash password
        val passwordHash = hashingUtil.hashPassword(request.password)

        // 4. Generate verification token
        val verificationToken = hashingUtil.generateToken()

        // 5. Buat user baru
        val newUser = User(
            email = request.email,
            passwordHash = passwordHash,
            fullName = request.fullName,
            phoneNumber = request.phoneNumber,
            role = UserRole.USER,
            isEmailVerified = false,
            emailVerificationToken = hashingUtil.hashToken(verificationToken)
        )

        // 6. Simpan ke database
        val savedUser = userRepository.create(newUser)

        // 7. Kirim email verifikasi (async, jangan block)
        try {
            emailUtil.sendVerificationEmail(savedUser.email, verificationToken)
        } catch (e: Exception) {
            // Log error tapi jangan gagalkan registrasi
            println("Failed to send verification email: ${e.message}")
        }

        // 8. Generate JWT token
        val token = jwtUtil.generateToken(savedUser.id.toString(), savedUser.role.name)

        // 9. Return response
        return AuthResponse(
            token = token,
            user = savedUser.toUserProfileResponse()
        )
    }

    private fun validateRegisterRequest(request: RegisterRequest) {
        // Validasi email
        require(request.email.isNotBlank()) { "Email is required" }
        require(request.email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))) {
            "Invalid email format"
        }

        // Validasi password
        require(request.password.length >= 8) {
            "Password must be at least 8 characters"
        }
        require(request.password.any { it.isUpperCase() }) {
            "Password must contain at least one uppercase letter"
        }
        require(request.password.any { it.isLowerCase() }) {
            "Password must contain at least one lowercase letter"
        }
        require(request.password.any { it.isDigit() }) {
            "Password must contain at least one number"
        }

        // Validasi full name
        require(request.fullName.isNotBlank()) { "Full name is required" }
        require(request.fullName.length >= 3) { "Full name must be at least 3 characters" }

        // Validasi phone number (optional)
        request.phoneNumber?.let { phone ->
            require(phone.matches(Regex("^\\+?[0-9]{10,15}$"))) {
                "Invalid phone number format"
            }
        }
    }

    // Extension function untuk convert User -> UserProfileResponse
    private fun User.toUserProfileResponse() = UserProfileResponse(
        id = this.id.toString(),
        email = this.email,
        fullName = this.fullName,
        phoneNumber = this.phoneNumber,
        role = this.role.name,
        isEmailVerified = this.isEmailVerified,
        profilePictureUrl = this.profilePictureUrl
    )
}
```

## 4. Routes/Module

### AuthModule.kt
```kotlin
package com.gity.features.auth.modules

import com.gity.features.auth.models.dtos.requests.RegisterRequest
import com.gity.features.auth.services.AuthService
import com.gity.shared.dtos.response.CommonResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureAuthModule() {
    routing {
        authRoutes()
    }
}

fun Route.authRoutes() {
    val authService by inject<AuthService>()

    route("/api/v1/auth") {
        
        // POST /api/v1/auth/register
        post("/register") {
            try {
                // 1. Receive request body
                val request = call.receive<RegisterRequest>()

                // 2. Call service
                val response = authService.register(request)

                // 3. Send response
                call.respond(
                    HttpStatusCode.Created,
                    CommonResponse(
                        success = true,
                        message = "Registration successful. Please check your email for verification.",
                        data = response
                    )
                )
            } catch (e: IllegalArgumentException) {
                // Validation error
                call.respond(
                    HttpStatusCode.BadRequest,
                    CommonResponse<Unit>(
                        success = false,
                        message = e.message ?: "Invalid request"
                    )
                )
            } catch (e: Exception) {
                // Unexpected error
                call.application.environment.log.error("Registration error", e)
                call.respond(
                    HttpStatusCode.InternalServerError,
                    CommonResponse<Unit>(
                        success = false,
                        message = "An error occurred during registration"
                    )
                )
            }
        }

        // POST /api/v1/auth/login
        // TODO: Implement login

        // GET /api/v1/auth/verify-email?token=xxx
        // TODO: Implement email verification

        // POST /api/v1/auth/forgot-password
        // TODO: Implement forgot password

        // POST /api/v1/auth/reset-password
        // TODO: Implement reset password

        // Protected routes
        // authenticate("auth-jwt") {
        //     GET /api/v1/auth/profile
        //     PUT /api/v1/auth/profile
        // }
    }
}
```

## 5. Update KoinModules.kt

Uncomment module yang sudah diimplementasi:

```kotlin
package com.gity.di

// ... imports ...

import com.gity.features.auth.repositories.UserRepository
import com.gity.features.auth.services.AuthService

fun koinModules(app: Application) {
    app.install(Koin) {
        slf4jLogger()
        modules(
            configModule,
            databaseModule,
            redisModule,
            sharedUtilModule,
            userRepositoryModule,  // ✅ Uncomment
            authServiceModule      // ✅ Uncomment
        )
    }
}

// ... other modules ...

val userRepositoryModule = module {
    single { UserRepository(get()) }
}

val authServiceModule = module {
    single { AuthService(get(), get(), get(), get()) }
}
```

## 6. Update Application.kt

Uncomment module yang sudah diimplementasi:

```kotlin
package com.gity

import com.gity.di.koinModules
import com.gity.features.auth.modules.configureAuthModule  // ✅ Import
import com.gity.plugins.*
import io.ktor.server.application.*

fun Application.module() {
    koinModules(this)
    
    configureSerialization()
    configureMonitoring()
    configureCORS()
    configureAuthentication()
    configureStatusPages()
    configureHTTP()

    // Routes
    configureAuthModule()  // ✅ Uncomment
}
```

## 7. Testing dengan Postman

### Request:
```http
POST http://localhost:8080/api/v1/auth/register
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "SecurePass123",
  "fullName": "John Doe",
  "phoneNumber": "081234567890"
}
```

### Success Response (201 Created):
```json
{
  "success": true,
  "message": "Registration successful. Please check your email for verification.",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": "65f1a2b3c4d5e6f7a8b9c0d1",
      "email": "john.doe@example.com",
      "fullName": "John Doe",
      "phoneNumber": "081234567890",
      "role": "USER",
      "isEmailVerified": false,
      "profilePictureUrl": null
    }
  }
}
```

### Error Response (400 Bad Request):
```json
{
  "success": false,
  "message": "Email already registered"
}
```

## 8. Verifikasi di MongoDB Express

1. Buka http://localhost:8081
2. Login dengan admin/admin123
3. Pilih database `kliksewa_db`
4. Pilih collection `users`
5. Anda akan lihat document user baru yang ter-insert

## Flow Diagram

```
Client -> POST /register
    |
    v
AuthModule.authRoutes() -> receive request
    |
    v
AuthService.register()
    |
    ├─> Validate input
    ├─> Check if email exists (UserRepository)
    ├─> Hash password (HashingUtil)
    ├─> Generate verification token
    ├─> Save user to DB (UserRepository)
    ├─> Send verification email (EmailUtil)
    └─> Generate JWT token (JwtUtil)
    |
    v
Return AuthResponse
    |
    v
Client receives token + user data
```

## Error Handling Flow

```
Try {
    authService.register()
}
Catch IllegalArgumentException {
    -> 400 Bad Request (validation error)
}
Catch Exception {
    -> 500 Internal Server Error
    -> Log error
}
```

## Pola yang Sama untuk Fitur Lain

Gunakan pola yang sama untuk implementasi fitur lainnya:

1. **DTO** - Request & Response
2. **Repository** - Data access layer
3. **Service** - Business logic
4. **Module/Routes** - HTTP endpoints
5. **Register di Koin** - Dependency injection
6. **Register di Application** - Enable routes

Selamat mengimplementasi! 🚀