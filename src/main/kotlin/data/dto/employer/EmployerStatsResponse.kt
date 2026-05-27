package data.dto.employer

import kotlinx.serialization.Serializable

@Serializable
data class EmployerStatsResponse(
    val totalVacancies: Int,
    val activeVacancies: Int,
    val totalApplications: Int,
    val pendingApplications: Int
)
