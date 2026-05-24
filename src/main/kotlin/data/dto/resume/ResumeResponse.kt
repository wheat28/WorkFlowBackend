package data.dto.resume

import kotlinx.serialization.Serializable

@Serializable
data class ResumeResponse(
    val id: String,
    val seekerId: String,
    val title: String,
    val position: String,
    val salaryExpected: Int?,
    val currency: String,
    val city: String?,
    val employmentType: String,
    val about: String?,
    val isActive: Boolean,
    val skills: List<String>,
    val workExperiences: List<WorkExperienceResponse>
)
