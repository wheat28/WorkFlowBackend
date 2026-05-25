package domain.usecase.resume

import data.dto.resume.ResumeRequest
import domain.repository.ResumeRepository
import java.util.UUID

class UpdateResumeUseCase(private val repository: ResumeRepository) {

    sealed class Result {
        object Success : Result()
        object Forbidden : Result()
        object NotFound : Result()
    }

    suspend operator fun invoke(callerId: UUID, resumeId: UUID, request: ResumeRequest): Result {
        val ownerId = repository.getOwnerId(resumeId) ?: return Result.NotFound
        if (ownerId != callerId) return Result.Forbidden
        repository.update(resumeId, request)
        return Result.Success
    }
}
