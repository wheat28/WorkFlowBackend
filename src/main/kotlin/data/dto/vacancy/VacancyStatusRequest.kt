package data.dto.vacancy

import kotlinx.serialization.Serializable

@Serializable
data class VacancyStatusRequest(val isActive: Boolean)
