package data.dto.resume

import kotlinx.serialization.Serializable

@Serializable
data class WorkExperienceResponse(
    val id: String,
    val companyName: String,
    val position: String,
    val startDate: String,
    val endDate: String?,
    val description: String?
)
