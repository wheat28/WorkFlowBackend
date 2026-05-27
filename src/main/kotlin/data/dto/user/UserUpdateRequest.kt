package data.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class UserUpdateRequest(
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val city: String? = null,
    val about: String? = null
)
