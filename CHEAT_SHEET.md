# 📝 Quick Reference Cheat Sheet

## Docker Commands

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose stop

# View logs
docker-compose logs -f

# Check status
docker-compose ps

# Restart service
docker-compose restart mongodb

# Stop and remove (data safe)
docker-compose down

# Stop and remove (DELETE ALL DATA!)
docker-compose down -v
```

## Access URLs

| Service | URL | Credentials |
|---------|-----|-------------|
| **Backend API** | http://localhost:8080 | - |
| **MongoDB Express** | http://localhost:8081 | admin / admin123 |
| **Redis Commander** | http://localhost:8082 | - |

## Gradle Commands

```bash
# Build
./gradlew build

# Run
./gradlew run

# Clean
./gradlew clean

# Test
./gradlew test

# Fat JAR
./gradlew buildFatJar
```

## MongoDB Commands

```bash
# Connect to MongoDB
docker exec -it kliksewa-mongodb mongosh

# Inside mongosh:
show dbs                      # Show all databases
use kliksewa_db              # Switch to database
show collections             # Show collections
db.users.find()              # Query users
db.users.find().pretty()     # Pretty print
db.users.insertOne({...})    # Insert document
db.users.updateOne({...})    # Update document
db.users.deleteOne({...})    # Delete document
```

## Redis Commands

```bash
# Connect to Redis
docker exec -it kliksewa-redis redis-cli

# Inside redis-cli:
PING                  # Test connection
KEYS *                # Show all keys
GET key               # Get value
SET key value         # Set value
DEL key               # Delete key
FLUSHALL              # Clear all (CAREFUL!)
```

## Environment Variables (.env)

```env
MONGODB_URI=mongodb://localhost:27017/kliksewa_db
REDIS_HOST=localhost
REDIS_PORT=6379
JWT_SECRET=your-secret-key
JWT_EXPIRATION_IN_MINUTES=10080
BREVO_API_KEY=your-api-key
SENDER_EMAIL=noreply@kliksewa.com
SENDER_NAME=Klik Sewa
FRONTEND_URL=http://localhost:3000
```

## API Endpoints (After Implementation)

### Auth
```
POST   /api/v1/auth/register          # Register new user
POST   /api/v1/auth/login             # Login
GET    /api/v1/auth/verify-email      # Verify email
POST   /api/v1/auth/forgot-password   # Request password reset
POST   /api/v1/auth/reset-password    # Reset password
GET    /api/v1/auth/profile           # Get current user (Protected)
PUT    /api/v1/auth/profile           # Update profile (Protected)
```

### Listings
```
GET    /api/v1/listings               # Get all listings (with filters)
POST   /api/v1/listings               # Create listing (Protected)
GET    /api/v1/listings/:id           # Get listing detail
PUT    /api/v1/listings/:id           # Update listing (Protected)
DELETE /api/v1/listings/:id           # Delete listing (Protected)
GET    /api/v1/listings/my-listings   # Get user's listings (Protected)
```

### Categories
```
GET    /api/v1/categories             # Get all categories
POST   /api/v1/categories             # Create category (Admin)
PUT    /api/v1/categories/:id         # Update category (Admin)
DELETE /api/v1/categories/:id         # Delete category (Admin)
```

### Admin
```
GET    /api/v1/admin/dashboard        # Dashboard stats (Admin)
GET    /api/v1/admin/listings/pending # Pending listings (Admin)
PUT    /api/v1/admin/listings/:id/approve  # Approve listing (Admin)
PUT    /api/v1/admin/listings/:id/reject   # Reject listing (Admin)
GET    /api/v1/admin/users            # Get all users (Admin)
```

## Common HTTP Status Codes

| Code | Meaning | Usage |
|------|---------|-------|
| **200** | OK | Success (GET, PUT) |
| **201** | Created | Success (POST) |
| **204** | No Content | Success (DELETE) |
| **400** | Bad Request | Validation error |
| **401** | Unauthorized | Not authenticated |
| **403** | Forbidden | Not authorized |
| **404** | Not Found | Resource not found |
| **500** | Server Error | Internal error |

## Postman Testing Examples

### Register User
```http
POST http://localhost:8080/api/v1/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "Password123!",
  "fullName": "John Doe",
  "phoneNumber": "081234567890"
}
```

### Login
```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "Password123!"
}
```

### Get Profile (Protected)
```http
GET http://localhost:8080/api/v1/auth/profile
Authorization: Bearer <your-jwt-token>
```

### Create Listing (Protected)
```http
POST http://localhost:8080/api/v1/listings
Authorization: Bearer <your-jwt-token>
Content-Type: application/json

{
  "title": "Kamera Canon EOS R5",
  "description": "Kamera mirrorless terbaru",
  "categoryId": "65abc123...",
  "pricePerDay": 500000,
  "location": "Jakarta Selatan",
  "specifications": {
    "Brand": "Canon",
    "Model": "EOS R5",
    "Year": "2023"
  }
}
```

## Koin Injection Pattern

```kotlin
// In Route/Controller
val authService by inject<AuthService>()
val userRepository by inject<UserRepository>()

// In Service
class AuthService(
    private val userRepository: UserRepository,
    private val hashingUtil: HashingUtil,
    private val jwtUtil: JwtUtil,
    private val emailUtil: EmailUtil
) {
    // Your logic here
}
```

## Kotlin Coroutines with MongoDB

```kotlin
// Repository pattern
suspend fun findUserByEmail(email: String): User? {
    return collection.find(User::email eq email).firstOrNull()
}

suspend fun createUser(user: User): User {
    collection.insertOne(user)
    return user
}

suspend fun updateUser(userId: ObjectId, update: Bson): Boolean {
    val result = collection.updateOne(User::id eq userId, update)
    return result.modifiedCount > 0
}

suspend fun deleteUser(userId: ObjectId): Boolean {
    val result = collection.deleteOne(User::id eq userId)
    return result.deletedCount > 0
}
```

## MongoDB Query Examples

```kotlin
import com.mongodb.client.model.Filters.*
import com.mongodb.client.model.Updates.*

// Find by ID
collection.find(eq("_id", objectId)).firstOrNull()

// Find with multiple conditions
collection.find(
    and(
        eq("status", ListingStatus.APPROVED),
        eq("availability", Availability.AVAILABLE)
    )
).toList()

// Update operations
collection.updateOne(
    eq("_id", objectId),
    combine(
        set("fullName", "New Name"),
        set("updatedAt", Instant.now())
    )
)

// Pagination
collection.find()
    .skip(page * limit)
    .limit(limit)
    .toList()

// Count documents
collection.countDocuments(filter)
```

## JWT Authentication Flow

```kotlin
// 1. Generate token (in AuthService)
val token = jwtUtil.generateToken(user.id.toString(), user.role.name)

// 2. Send token to client
CommonResponse(
    success = true,
    message = "Login successful",
    data = AuthResponse(token = token, user = userDto)
)

// 3. Client sends token in header
// Authorization: Bearer <token>

// 4. Verify token (automatic in Ktor with auth-jwt plugin)
authenticate("auth-jwt") {
    get("/profile") {
        val userId = call.getUserId() // From extension function
        // Handle request
    }
}
```

## Error Handling Pattern

```kotlin
// In Service
suspend fun login(email: String, password: String): AuthResponse {
    val user = userRepository.findByEmail(email)
        ?: throw IllegalArgumentException("Invalid email or password")
    
    if (!hashingUtil.verifyPassword(password, user.passwordHash)) {
        throw IllegalArgumentException("Invalid email or password")
    }
    
    if (!user.isEmailVerified) {
        throw IllegalArgumentException("Email not verified")
    }
    
    // Continue...
}

// In Route (automatic handling by StatusPages plugin)
post("/login") {
    try {
        val request = call.receive<LoginRequest>()
        val response = authService.login(request.email, request.password)
        call.respond(HttpStatusCode.OK, CommonResponse(
            success = true,
            message = "Login successful",
            data = response
        ))
    } catch (e: IllegalArgumentException) {
        call.respond(HttpStatusCode.BadRequest, CommonResponse<Unit>(
            success = false,
            message = e.message ?: "Bad request"
        ))
    }
}
```

## Redis Usage Examples

```kotlin
// Get Jedis resource
redisConfig.jedisPool.resource.use { jedis ->
    // Set value with expiration
    jedis.setex("user:${userId}:session", 3600, token)
    
    // Get value
    val value = jedis.get("user:${userId}:session")
    
    // Delete key
    jedis.del("user:${userId}:session")
    
    // Check if exists
    val exists = jedis.exists("user:${userId}:session")
}
```

## Validation Pattern

```kotlin
// Simple validation
fun validateEmail(email: String) {
    require(email.isNotBlank()) { "Email is required" }
    require(email.matches(Regex("^[A-Za-z0-9+_.-]+@(.+)$"))) { 
        "Invalid email format" 
    }
}

fun validatePassword(password: String) {
    require(password.length >= 8) { "Password must be at least 8 characters" }
    require(password.any { it.isUpperCase() }) { 
        "Password must contain uppercase letter" 
    }
    require(password.any { it.isDigit() }) { 
        "Password must contain number" 
    }
}
```

## File Structure Recap

```
src/main/kotlin/
├── Application.kt              # Main entry point
├── config/
│   ├── AppConfig.kt           # Config data classes
│   ├── DatabaseConfig.kt      # MongoDB setup
│   ├── RedisConfig.kt         # Redis setup
│   └── EmailConfig.kt         # Email setup (deprecated, use shared/utils)
├── di/
│   └── KoinModules.kt         # Dependency injection setup
├── features/
│   ├── auth/                  # Authentication feature
│   │   ├── models/
│   │   │   ├── dtos/
│   │   │   │   ├── requests/
│   │   │   │   └── responses/
│   │   ├── repositories/
│   │   ├── services/
│   │   └── modules/           # Routes
│   ├── listing/               # Listing feature
│   ├── category/              # Category feature
│   └── admin/                 # Admin feature
├── plugins/                   # Ktor plugins
│   ├── ConfigureAuthentication.kt
│   ├── ConfigureCORS.kt
│   ├── ConfigureHTTP.kt
│   ├── ConfigureMonitoring.kt
│   ├── ConfigureSerialization.kt
│   └── ConfigureStatusPages.kt
└── shared/
    ├── constants/             # Constants
    ├── models/                # Domain models
    │   ├── User.kt
    │   ├── Listing.kt
    │   ├── Category.kt
    │   └── enums/
    ├── dtos/
    │   └── response/
    │       └── CommonResponse.kt
    └── utils/                 # Utilities
        ├── HashingUtil.kt
        ├── JwtUtil.kt
        ├── EmailUtil.kt
        └── ValidationUtil.kt
```

## Tips & Tricks

### IntelliJ Shortcuts
- `Ctrl + Space` - Code completion
- `Ctrl + Shift + F` - Format code
- `Alt + Enter` - Quick fix
- `Ctrl + /` - Comment line
- `Ctrl + Shift + /` - Block comment

### Debugging
- Add breakpoint (click left margin)
- `Shift + F9` - Debug
- `F8` - Step over
- `F7` - Step into
- `Shift + F8` - Step out

### Git Commands (Bonus)
```bash
git status
git add .
git commit -m "Setup plugins and database"
git push origin main
```

---

## Ready to Implement! 🚀

Setup selesai! Sekarang Anda bisa mulai implementasi:
1. **Auth Module** - Register, Login, Profile
2. **Listing Module** - CRUD Listings
3. **Category Module** - Manage Categories
4. **Admin Module** - Dashboard & Management

Semangat coding! 💪