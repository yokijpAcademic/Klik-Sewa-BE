package com.gity.features.auth.models.dtos.response


import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserProfileResponse
)

