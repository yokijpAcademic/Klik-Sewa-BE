package com.gity.shared.utils

import com.gity.features.auth.models.dtos.requests.LoginRequest
import com.gity.features.auth.models.dtos.requests.RegisterRequest
import com.gity.features.auth.models.dtos.requests.UpdateProfileRequest

data class ValidationResult(
    val isValid: Boolean,
    val message: String
)

object ValidationUtil {

    /**
     * Validasi email format
     */
    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$".toRegex()
        return email.matches(emailRegex)
    }

    /**
     * Validasi password strength
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * Validasi nama
     */
    fun isValidName(name: String): Boolean {
        return name.isNotBlank() && name.length >= 2 && name.length <= 100
    }

    /**
     * Validasi register request
     */
    fun validateRegisterRequest(request: RegisterRequest): ValidationResult {
        if (request.email.isBlank()) {
            return ValidationResult(false, "Email tidak boleh kosong")
        }

        if (!isValidEmail(request.email)) {
            return ValidationResult(false, "Format email tidak valid")
        }

        if (request.password.isBlank()) {
            return ValidationResult(false, "Password tidak boleh kosong")
        }

        if (!isValidPassword(request.password)) {
            return ValidationResult(false, "Password minimal 6 karakter")
        }

        if (request.name.isBlank()) {
            return ValidationResult(false, "Nama tidak boleh kosong")
        }

        if (!isValidName(request.name)) {
            return ValidationResult(false, "Nama harus antara 2-100 karakter")
        }

        return ValidationResult(true, "Valid")
    }

    /**
     * Validasi login request
     */
    fun validateLoginRequest(request: LoginRequest): ValidationResult {
        if (request.email.isBlank()) {
            return ValidationResult(false, "Email tidak boleh kosong")
        }

        if (!isValidEmail(request.email)) {
            return ValidationResult(false, "Format email tidak valid")
        }

        if (request.password.isBlank()) {
            return ValidationResult(false, "Password tidak boleh kosong")
        }

        return ValidationResult(true, "Valid")
    }

    /**
     * Validasi update profile request
     */
    fun validateUpdateProfileRequest(request: UpdateProfileRequest): ValidationResult {
        if (request.email.isBlank()) {
            return ValidationResult(false, "Email tidak boleh kosong")
        }

        if (!isValidEmail(request.email)) {
            return ValidationResult(false, "Format email tidak valid")
        }

        if (request.name.isBlank()) {
            return ValidationResult(false, "Nama tidak boleh kosong")
        }

        if (!isValidName(request.name)) {
            return ValidationResult(false, "Nama harus antara 2-100 karakter")
        }

        return ValidationResult(true, "Valid")
    }
}