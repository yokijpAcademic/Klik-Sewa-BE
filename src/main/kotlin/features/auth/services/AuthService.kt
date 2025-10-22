package com.gity.features.auth.services

import com.gity.config.AppConfig
import com.gity.features.auth.models.dtos.requests.LoginRequest
import com.gity.features.auth.models.dtos.requests.RegisterRequest
import com.gity.features.auth.models.dtos.requests.UpdateProfileRequest
import com.gity.features.auth.models.dtos.response.AuthResponse
import com.gity.features.auth.models.dtos.response.UserProfileResponse
import com.gity.features.auth.repositories.UserRepository
import com.gity.shared.dtos.response.CommonResponse
import com.gity.shared.models.User
import com.gity.shared.models.enums.UserRole
import com.gity.shared.utils.EmailUtil
import com.gity.shared.utils.HashingUtil
import com.gity.shared.utils.JwtUtil
import com.gity.shared.utils.ValidationUtil
import java.time.Instant

class AuthService(
    private val userRepository: UserRepository,
    private val hashingUtil: HashingUtil,
    private val jwtUtil: JwtUtil,
    private val emailUtil: EmailUtil,
    private val appConfig: AppConfig
) {

    /**
     * Register user baru
     */
    suspend fun register(request: RegisterRequest): CommonResponse<AuthResponse> {
        // Validasi input
        val validation = ValidationUtil.validateRegisterRequest(request)
        if (!validation.isValid) {
            return CommonResponse.error(validation.message)
        }

        // Cek apakah email sudah ada
        if (userRepository.emailExists(request.email)) {
            return CommonResponse.error("Email sudah terdaftar")
        }

        // Hash password
        val hashedPassword = hashingUtil.hashPassword(request.password)

        // Generate email verification token
        val verificationToken = hashingUtil.generateToken()
        val verificationExpires = Instant.now().plusSeconds(24 * 60 * 60).epochSecond // 24 jam

        // Buat user baru
        val newUser = User(
            email = request.email.lowercase(),
            password = hashedPassword,
            name = request.name,
            role = UserRole.USER,
            isActive = true,
            emailVerified = false,
            emailVerificationToken = verificationToken,
            emailVerificationExpires = verificationExpires
        )

        // Simpan ke database
        val savedUser = userRepository.create(newUser)
            ?: return CommonResponse.error("Gagal membuat akun")

        // Kirim email verifikasi
        try {
            emailUtil.sendVerificationEmail(
                toEmail = savedUser.email,
                verificationToken = verificationToken
            )
        } catch (e: Exception) {
            // Log error tapi jangan gagalkan registrasi
            println("Failed to send verification email: ${e.message}")
        }

        // Generate JWT token
        val token = jwtUtil.generateToken(savedUser.id!!, savedUser.role.name)

        val authResponse = AuthResponse(
            token = token,
            user = UserProfileResponse(
                id = savedUser.id!!,
                email = savedUser.email,
                name = savedUser.name,
                role = savedUser.role,
                isActive = savedUser.isActive,
                emailVerified = savedUser.emailVerified,
                createdAt = savedUser.createdAt
            )
        )

        return CommonResponse.success(
            data = authResponse,
            message = "Registrasi berhasil. Silakan cek email untuk verifikasi."
        )
    }

    /**
     * Login user
     */
    suspend fun login(request: LoginRequest): CommonResponse<AuthResponse> {
        // Validasi input
        val validation = ValidationUtil.validateLoginRequest(request)
        if (!validation.isValid) {
            return CommonResponse.error(validation.message)
        }

        // Cari user berdasarkan email
        val user = userRepository.findByEmail(request.email.lowercase())
            ?: return CommonResponse.error("Email atau password salah")

        // Cek apakah user aktif
        if (!user.isActive) {
            return CommonResponse.error("Akun Anda telah dinonaktifkan")
        }

        // Verifikasi password
        if (!hashingUtil.verifyPassword(request.password, user.password)) {
            return CommonResponse.error("Email atau password salah")
        }

        // Generate JWT token
        val token = jwtUtil.generateToken(user.id!!, user.role.name)

        val authResponse = AuthResponse(
            token = token,
            user = UserProfileResponse(
                id = user.id!!,
                email = user.email,
                name = user.name,
                role = user.role,
                isActive = user.isActive,
                emailVerified = user.emailVerified,
                createdAt = user.createdAt
            )
        )

        return CommonResponse.success(
            data = authResponse,
            message = "Login berhasil"
        )
    }

    /**
     * Verifikasi email
     */
    suspend fun verifyEmail(token: String): CommonResponse<String> {
        // Cari user berdasarkan token
        val user = userRepository.findByEmailVerificationToken(token)
            ?: return CommonResponse.error("Token verifikasi tidak valid atau sudah expired")

        // Update status email verified
        val updated = userRepository.verifyEmail(user.id!!)
        if (!updated) {
            return CommonResponse.error("Gagal memverifikasi email")
        }

        return CommonResponse.success(
            data = "Email berhasil diverifikasi",
            message = "Email berhasil diverifikasi"
        )
    }

    /**
     * Kirim ulang email verifikasi
     */
    suspend fun resendVerificationEmail(email: String): CommonResponse<String> {
        // Cari user berdasarkan email
        val user = userRepository.findByEmail(email.lowercase())
            ?: return CommonResponse.error("Email tidak ditemukan")

        // Cek apakah email sudah diverifikasi
        if (user.emailVerified) {
            return CommonResponse.error("Email sudah diverifikasi")
        }

        // Generate token baru
        val verificationToken = hashingUtil.generateToken()
        val verificationExpires = Instant.now().plusSeconds(24 * 60 * 60).epochSecond

        // Update token di database
        val updated = userRepository.setEmailVerificationToken(
            user.id!!,
            verificationToken,
            verificationExpires
        )

        if (!updated) {
            return CommonResponse.error("Gagal mengirim email verifikasi")
        }

        // Kirim email
        try {
            emailUtil.sendVerificationEmail(
                toEmail = user.email,
                verificationToken = verificationToken
            )
        } catch (e: Exception) {
            return CommonResponse.error("Gagal mengirim email: ${e.message}")
        }

        return CommonResponse.success(
            data = "Email verifikasi berhasil dikirim",
            message = "Email verifikasi berhasil dikirim"
        )
    }

    /**
     * Get profile user
     */
    suspend fun getProfile(userId: String): CommonResponse<UserProfileResponse> {
        val user = userRepository.findById(userId)
            ?: return CommonResponse.error("User tidak ditemukan")

        val profile = UserProfileResponse(
            id = user.id!!,
            email = user.email,
            name = user.name,
            role = user.role,
            isActive = user.isActive,
            emailVerified = user.emailVerified,
            createdAt = user.createdAt
        )

        return CommonResponse.success(
            data = profile,
            message = "Profile berhasil diambil"
        )
    }

    /**
     * Update profile user
     */
    suspend fun updateProfile(userId: String, request: UpdateProfileRequest): CommonResponse<UserProfileResponse> {
        // Validasi input
        val validation = ValidationUtil.validateUpdateProfileRequest(request)
        if (!validation.isValid) {
            return CommonResponse.error(validation.message)
        }

        // Cek apakah user ada
        val user = userRepository.findById(userId)
            ?: return CommonResponse.error("User tidak ditemukan")

        // Cek apakah email baru sudah digunakan user lain
        if (request.email != user.email && userRepository.emailExists(request.email)) {
            return CommonResponse.error("Email sudah digunakan")
        }

        // Prepare updates
        val updates = mutableMapOf<String, Any?>()
        updates["name"] = request.name

        // Jika email berubah, reset email verification
        if (request.email != user.email) {
            updates["email"] = request.email.lowercase()
            updates["emailVerified"] = false
            updates["emailVerificationToken"] = null
            updates["emailVerificationExpires"] = null
        }

        // Update di database
        val updated = userRepository.updateById(userId, updates)
        if (!updated) {
            return CommonResponse.error("Gagal mengupdate profile")
        }

        // Get updated user
        val updatedUser = userRepository.findById(userId)!!

        val profile = UserProfileResponse(
            id = updatedUser.id!!,
            email = updatedUser.email,
            name = updatedUser.name,
            role = updatedUser.role,
            isActive = updatedUser.isActive,
            emailVerified = updatedUser.emailVerified,
            createdAt = updatedUser.createdAt
        )

        return CommonResponse.success(
            data = profile,
            message = "Profile berhasil diupdate"
        )
    }

    /**
     * Request password reset
     */
    suspend fun requestPasswordReset(email: String): CommonResponse<String> {
        // Cari user berdasarkan email
        val user = userRepository.findByEmail(email.lowercase())
            ?: return CommonResponse.error("Email tidak ditemukan")

        // Generate reset token
        val resetToken = hashingUtil.generateToken()
        val resetExpires = Instant.now().plusSeconds(60 * 60).epochSecond // 1 jam

        // Update token di database
        val updated = userRepository.setPasswordResetToken(
            user.id!!,
            resetToken,
            resetExpires
        )

        if (!updated) {
            return CommonResponse.error("Gagal mengirim email reset password")
        }

        // Kirim email
        try {
            emailUtil.sendPasswordResetEmail(
                toEmail = user.email,
                resetToken = resetToken
            )
        } catch (e: Exception) {
            return CommonResponse.error("Gagal mengirim email: ${e.message}")
        }

        return CommonResponse.success(
            data = "Email reset password berhasil dikirim",
            message = "Email reset password berhasil dikirim"
        )
    }

    /**
     * Reset password
     */
    suspend fun resetPassword(token: String, newPassword: String): CommonResponse<String> {
        // Validasi password
        if (newPassword.length < 6) {
            return CommonResponse.error("Password minimal 6 karakter")
        }

        // Cari user berdasarkan token
        val user = userRepository.findByPasswordResetToken(token)
            ?: return CommonResponse.error("Token reset password tidak valid atau sudah expired")

        // Hash password baru
        val hashedPassword = hashingUtil.hashPassword(newPassword)

        // Update password
        val updated = userRepository.updatePassword(user.id!!, hashedPassword)
        if (!updated) {
            return CommonResponse.error("Gagal mereset password")
        }

        return CommonResponse.success(
            data = "Password berhasil direset",
            message = "Password berhasil direset"
        )
    }
}