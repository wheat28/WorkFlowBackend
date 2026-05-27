package domain.repository

import data.dto.resume.ResumeRequest
import data.dto.resume.ResumeResponse
import data.dto.resume.WorkExperienceRequest
import java.util.UUID

interface ResumeRepository {
    suspend fun getById(id: UUID): ResumeResponse?
    suspend fun getBySeekerID(seekerId: UUID): List<ResumeResponse>
    suspend fun getOwnerId(id: UUID): UUID?
    suspend fun create(seekerId: UUID, request: ResumeRequest): UUID
    suspend fun update(id: UUID, request: ResumeRequest): Boolean
    suspend fun setActive(id: UUID, isActive: Boolean): Boolean
    suspend fun delete(id: UUID): Boolean
    suspend fun addWorkExperience(resumeId: UUID, request: WorkExperienceRequest): UUID
    suspend fun deleteWorkExperience(id: UUID): Boolean
}
