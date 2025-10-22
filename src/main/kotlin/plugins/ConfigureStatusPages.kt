package com.gity.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val success: Boolean = false,
    val message: String,
    val error: String? = null
)

fun Application.configureStatusPages() {
    install(StatusPages) {
        // Handle 404 Not Found
        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(
                status,
                ErrorResponse(
                    message = "Resource not found",
                    error = "The requested resource does not exist"
                )
            )
        }

        // Handle 401 Unauthorized
        status(HttpStatusCode.Unauthorized) { call, status ->
            call.respond(
                status,
                ErrorResponse(
                    message = "Unauthorized",
                    error = "Authentication is required"
                )
            )
        }

        // Handle 403 Forbidden
        status(HttpStatusCode.Forbidden) { call, status ->
            call.respond(
                status,
                ErrorResponse(
                    message = "Forbidden",
                    error = "You don't have permission to access this resource"
                )
            )
        }

        // Handle general exceptions
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled exception", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    message = "Internal server error",
                    error = if (call.application.environment.developmentMode) {
                        cause.message
                    } else {
                        "Something went wrong"
                    }
                )
            )
        }

        // Handle IllegalArgumentException (biasa untuk validasi)
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    message = "Bad request",
                    error = cause.message ?: "Invalid request data"
                )
            )
        }
    }
}