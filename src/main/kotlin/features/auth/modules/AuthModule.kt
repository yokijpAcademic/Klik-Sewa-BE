package com.gity.features.auth.modules


import com.gity.features.auth.models.dtos.requests.LoginRequest
import com.gity.features.auth.models.dtos.requests.RegisterRequest
import com.gity.features.auth.models.dtos.requests.UpdateProfileRequest
import com.gity.features.auth.services.AuthService
import com.gity.plugins.getUserId
import com.gity.shared.dtos.response.CommonResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class EmailRequest(val email: String)

@Serializable
data class ResetPasswordRequest(
    val token: String,
    val newPassword: String
)

fun Application.configureAuthModule() {
    val authService by inject<AuthService>()

    routing {
        route("/api/auth") {

            // POST /api/auth/register - Registrasi user baru
            post("/register") {
                try {
                    val request = call.receive<RegisterRequest>()
                    val result = authService.register(request)

                    if (result.success) {
                        call.respond(HttpStatusCode.Created, result)
                    } else {
                        call.respond(HttpStatusCode.BadRequest, result)
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        CommonResponse.error<String>("Invalid request format")
                    )
                }
            }

            // POST /api/auth/login - Login user
            post("/login") {
                try {
                    val request = call.receive<LoginRequest>()
                    val result = authService.login(request)

                    if (result.success) {
                        call.respond(HttpStatusCode.OK, result)
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, result)
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        CommonResponse.error<String>("Invalid request format")
                    )
                }
            }

            // GET /api/auth/verify-email?token=xxx - Verifikasi email
            get("/verify-email") {
                val token = call.request.queryParameters["token"]

                if (token.isNullOrBlank()) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        CommonResponse.error<String>("Token verifikasi diperlukan")
                    )
                    return@get
                }

                val result = authService.verifyEmail(token)

                if (result.success) {
                    call.respond(HttpStatusCode.OK, result)
                } else {
                    call.respond(HttpStatusCode.BadRequest, result)
                }
            }

            // POST /api/auth/resend-verification - Kirim ulang email verifikasi
            post("/resend-verification") {
                try {
                    val request = call.receive<EmailRequest>()
                    val result = authService.resendVerificationEmail(request.email)

                    if (result.success) {
                        call.respond(HttpStatusCode.OK, result)
                    } else {
                        call.respond(HttpStatusCode.BadRequest, result)
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        CommonResponse.error<String>("Invalid request format")
                    )
                }
            }

            // POST /api/auth/forgot-password - Request reset password
            post("/forgot-password") {
                try {
                    val request = call.receive<EmailRequest>()
                    val result = authService.requestPasswordReset(request.email)

                    if (result.success) {
                        call.respond(HttpStatusCode.OK, result)
                    } else {
                        call.respond(HttpStatusCode.BadRequest, result)
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        CommonResponse.error<String>("Invalid request format")
                    )
                }
            }

            // POST /api/auth/reset-password - Reset password dengan token
            post("/reset-password") {
                try {
                    val request = call.receive<ResetPasswordRequest>()
                    val result = authService.resetPassword(request.token, request.newPassword)

                    if (result.success) {
                        call.respond(HttpStatusCode.OK, result)
                    } else {
                        call.respond(HttpStatusCode.BadRequest, result)
                    }
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        CommonResponse.error<String>("Invalid request format")
                    )
                }
            }

            // Routes yang memerlukan authentication
            authenticate("auth-jwt") {

                // GET /api/auth/profile - Get profile user yang sedang login
                get("/profile") {
                    try {
                        val userId = call.getUserId()
                        val result = authService.getProfile(userId)

                        if (result.success) {
                            call.respond(HttpStatusCode.OK, result)
                        } else {
                            call.respond(HttpStatusCode.NotFound, result)
                        }
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            CommonResponse.error<String>("Gagal mengambil profile")
                        )
                    }
                }

                // PUT /api/auth/profile - Update profile user
                put("/profile") {
                    try {
                        val userId = call.getUserId()
                        val request = call.receive<UpdateProfileRequest>()
                        val result = authService.updateProfile(userId, request)

                        if (result.success) {
                            call.respond(HttpStatusCode.OK, result)
                        } else {
                            call.respond(HttpStatusCode.BadRequest, result)
                        }
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            CommonResponse.error<String>("Invalid request format")
                        )
                    }
                }

                // POST /api/auth/logout - Logout (optional, untuk invalidate token di client)
                post("/logout") {
                    // Dalam implementasi JWT stateless, logout biasanya dilakukan di client
                    // dengan menghapus token. Tapi kita bisa return success response
                    call.respond(
                        HttpStatusCode.OK,
                        CommonResponse.success(
                            data = "Logout berhasil",
                            message = "Anda telah berhasil logout"
                        )
                    )
                }
            }
        }
    }
}
