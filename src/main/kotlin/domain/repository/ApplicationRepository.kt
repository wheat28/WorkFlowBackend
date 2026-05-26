package domain.repository

import data.dto.application.ApplicationRequest
import data.dto.application.ApplicationResponse
import java.util.UUID

interface ApplicationRepository {
    suspend fun getBySeekerId(seekerId: UUID): List<ApplicationResponse>
    suspend fun getByVacancyId(vacancyId: UUID): List<ApplicationResponse>
    suspend fun create(seekerId: UUID, request: ApplicationRequest): UUID
    suspend fun updateStatus(id: UUID, status: String): Boolean
    suspend fun isApplied(seekerId: UUID, vacancyId: UUID): Boolean
    suspend fun delete(id: UUID): Boolean
}
