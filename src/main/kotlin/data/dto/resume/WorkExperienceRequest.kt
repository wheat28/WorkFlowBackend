package data.dto.resume

import kotlinx.serialization.Serializable

@Serializable
data class WorkExperienceRequest(
    val companyName: String,
    val position: String,
    val startDate: String,
    val endDate: String? = null,
    val description: String? = null
)
