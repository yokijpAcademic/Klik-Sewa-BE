package com.gity.features.auth.models.dtos.requests


import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

