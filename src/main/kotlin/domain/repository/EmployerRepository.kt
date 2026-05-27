package domain.repository

import data.dto.employer.EmployerRegisterRequest
import data.dto.employer.EmployerResponse
import data.dto.employer.EmployerUpdateRequest
import java.util.UUID

interface EmployerRepository {
    suspend fun findById(id: UUID): EmployerResponse?
    suspend fun findByEmail(email: String): EmployerResponse?
    suspend fun getPasswordHash(email: String): String?
    suspend fun create(request: EmployerRegisterRequest): UUID
    suspend fun update(id: UUID, request: EmployerUpdateRequest): Boolean
    suspend fun emailExists(email: String): Boolean
}
