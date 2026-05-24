package data.dto.vacancy

import kotlinx.serialization.Serializable

@Serializable
data class VacancyRequest(
    val categoryId: Int? = null,
    val title: String,
    val description: String,
    val salaryFrom: Int? = null,
    val salaryTo: Int? = null,
    val currency: String = "RUB",
    val city: String? = null,
    val employmentType: String,
    val experience: String,
    val skillIds: List<Int> = emptyList()
)
