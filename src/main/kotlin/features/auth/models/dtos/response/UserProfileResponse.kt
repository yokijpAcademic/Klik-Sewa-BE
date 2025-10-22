package com.gity.features.auth.models.dtos.response


import com.gity.shared.models.enums.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponse(
    val id: String,
    val email: String,
    val name: String,
    val role: UserRole,
    val isActive: Boolean,
    val emailVerified: Boolean,
    val createdAt: Long
)

