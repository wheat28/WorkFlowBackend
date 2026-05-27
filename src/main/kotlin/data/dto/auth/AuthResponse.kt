package data.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val userType: String,
    val userId: String,
    val displayName: String
)
