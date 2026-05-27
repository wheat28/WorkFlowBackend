package data.dto.resume

import kotlinx.serialization.Serializable

@Serializable
data class ResumeStatusRequest(val isActive: Boolean)
