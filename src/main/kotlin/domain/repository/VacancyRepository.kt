package domain.repository

import data.dto.employer.EmployerStatsResponse
import data.dto.vacancy.VacancyRequest
import data.dto.vacancy.VacancyResponse
import java.util.UUID

interface VacancyRepository {
    suspend fun getAll(): List<VacancyResponse>
    suspend fun getById(id: UUID): VacancyResponse?
    suspend fun getByEmployerId(employerId: UUID): List<VacancyResponse>
    suspend fun getOwnerId(id: UUID): UUID?
    suspend fun create(employerId: UUID, request: VacancyRequest): UUID
    suspend fun update(id: UUID, request: VacancyRequest): Boolean
    suspend fun setActive(id: UUID, isActive: Boolean): Boolean
    suspend fun delete(id: UUID): Boolean
    suspend fun getEmployerStats(employerId: UUID): EmployerStatsResponse
}
