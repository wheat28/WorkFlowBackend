package data.dto.resume

import kotlinx.serialization.Serializable

@Serializable
data class ResumeRequest(
    val title: String,
    val position: String,
    val salaryExpected: Int? = null,
    val currency: String = "RUB",
    val city: String? = null,
    val employmentType: String,
    val about: String? = null,
    val skillIds: List<Int> = emptyList()
)
