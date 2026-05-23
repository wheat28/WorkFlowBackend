package data.dto.application

import kotlinx.serialization.Serializable

@Serializable
data class ApplicationRequest(
    val vacancyId: String,
    val resumeId: String,
    val coverLetter: String? = null
)
