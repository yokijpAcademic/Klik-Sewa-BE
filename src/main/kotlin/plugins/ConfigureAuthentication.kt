package com.gity.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.gity.config.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class AuthErrorResponse(
    val success: Boolean = false,
    val message: String
)

fun Application.configureAuthentication() {
    val appConfig by inject<AppConfig>()

    install(Authentication) {
        jwt("auth-jwt") {
            // Konfigurasi JWT Realm
            realm = appConfig.app.frontendUrl

            // Verifier untuk memvalidasi JWT token
            verifier(
                JWT.require(Algorithm.HMAC256(appConfig.jwt.secret))
                    .build()
            )

            // Validasi JWT token
            validate { credential ->
                // Cek apakah token memiliki subject (user ID) dan role
                val userId = credential.payload.subject
                val role = credential.payload.getClaim("role").asString()

                if (userId != null && role != null) {
                    // Return JWTPrincipal dengan user ID dan role
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            // Custom challenge untuk unauthorized request
            challenge { defaultScheme, realm ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    AuthErrorResponse(
                        message = "Token is not valid or has expired"
                    )
                )
            }
        }

        // JWT Authentication khusus untuk admin (bisa dipake untuk endpoint admin)
        jwt("auth-admin") {
            realm = appConfig.app.frontendUrl

            verifier(
                JWT.require(Algorithm.HMAC256(appConfig.jwt.secret))
                    .build()
            )

            validate { credential ->
                val userId = credential.payload.subject
                val role = credential.payload.getClaim("role").asString()

                // Hanya allow jika role adalah ADMIN
                if (userId != null && role == "ADMIN") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { defaultScheme, realm ->
                call.respond(
                    HttpStatusCode.Forbidden,
                    AuthErrorResponse(
                        message = "Admin access required"
                    )
                )
            }
        }
    }
}

// Extension functions untuk memudahkan akses user info di route handlers

/**
 * Get user ID dari JWT token
 */
fun ApplicationCall.getUserId(): String {
    val principal = principal<JWTPrincipal>()
    return principal?.payload?.subject
        ?: throw IllegalStateException("User ID not found in token")
}

/**
 * Get user role dari JWT token
 */
fun ApplicationCall.getUserRole(): String {
    val principal = principal<JWTPrincipal>()
    return principal?.payload?.getClaim("role")?.asString()
        ?: throw IllegalStateException("User role not found in token")
}

/**
 * Check apakah user adalah admin
 */
fun ApplicationCall.isAdmin(): Boolean {
    return try {
        getUserRole() == "ADMIN"
    } catch (e: Exception) {
        false
    }
}

/**
 * Get full JWT payload (untuk akses claim lainnya jika diperlukan)
 */
fun ApplicationCall.getJwtPayload(): Map<String, Any> {
    val principal = principal<JWTPrincipal>()
    return principal?.payload?.claims?.mapValues { it.value.asString() }
        ?: throw IllegalStateException("JWT payload not found")
}