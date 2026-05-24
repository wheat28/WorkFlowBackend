package data.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val city: String?,
    val avatarUrl: String?,
    val about: String?
)
