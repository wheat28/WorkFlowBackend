package data.dto.employer

import kotlinx.serialization.Serializable

@Serializable
data class EmployerUpdateRequest(
    val companyName: String,
    val description: String? = null,
    val website: String? = null,
    val city: String? = null,
    val industry: String? = null,
    val phone: String? = null
)
