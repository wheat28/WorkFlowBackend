package data.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class UserRegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val city: String? = null
)
