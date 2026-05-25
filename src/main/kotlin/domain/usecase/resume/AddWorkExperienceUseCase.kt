package domain.usecase.resume

import data.dto.resume.WorkExperienceRequest
import domain.repository.ResumeRepository
import java.util.UUID

class AddWorkExperienceUseCase(private val repository: ResumeRepository) {

    sealed class Result {
        data class Success(val id: UUID) : Result()
        object Forbidden : Result()
        object NotFound : Result()
    }

    suspend operator fun invoke(callerId: UUID, resumeId: UUID, request: WorkExperienceRequest): Result {
        val ownerId = repository.getOwnerId(resumeId) ?: return Result.NotFound
        if (ownerId != callerId) return Result.Forbidden
        val id = repository.addWorkExperience(resumeId, request)
        return Result.Success(id)
    }
}
