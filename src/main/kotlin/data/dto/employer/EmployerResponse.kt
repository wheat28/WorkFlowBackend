package data.dto.employer

import kotlinx.serialization.Serializable

@Serializable
data class EmployerResponse(
    val id: String,
    val email: String,
    val companyName: String,
    val description: String?,
    val website: String?,
    val logoUrl: String?,
    val city: String?,
    val industry: String?,
    val phone: String?
)
