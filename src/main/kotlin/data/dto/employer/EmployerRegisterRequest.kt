package data.dto.employer

import kotlinx.serialization.Serializable

@Serializable
data class EmployerRegisterRequest(
    val email: String,
    val password: String,
    val companyName: String,
    val description: String? = null,
    val website: String? = null,
    val city: String? = null,
    val industry: String? = null,
    val phone: String? = null
)
