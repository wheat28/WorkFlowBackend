package data.dto.vacancy

import kotlinx.serialization.Serializable

@Serializable
data class VacancyResponse(
    val id: String,
    val employerId: String,
    val companyName: String,
    val categoryId: Int?,
    val title: String,
    val description: String,
    val salaryFrom: Int?,
    val salaryTo: Int?,
    val currency: String,
    val city: String?,
    val employmentType: String,
    val experience: String,
    val isActive: Boolean,
    val skills: List<String>,
    val applicationCount: Int = 0
)
