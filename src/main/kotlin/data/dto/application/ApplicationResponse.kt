package data.dto.application

import kotlinx.serialization.Serializable

@Serializable
data class ApplicationResponse(
    val id: String,
    val seekerId: String,
    val seekerFirstName: String,
    val seekerLastName: String,
    val vacancyId: String,
    val vacancyTitle: String,
    val resumeId: String,
    val status: String,
    val coverLetter: String?,
    val createdAt: String
)
