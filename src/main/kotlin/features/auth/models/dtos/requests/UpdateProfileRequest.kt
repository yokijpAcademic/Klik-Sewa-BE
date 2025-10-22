package com.gity.features.auth.models.dtos.requests


import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(
    val email: String,
    val name: String
)

