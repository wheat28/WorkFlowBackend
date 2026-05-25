package domain.usecase.resume

import domain.repository.ResumeRepository
import java.util.UUID

class DeleteResumeUseCase(private val repository: ResumeRepository) {

    sealed class Result {
        object Success : Result()
        object Forbidden : Result()
        object NotFound : Result()
    }

    suspend operator fun invoke(callerId: UUID, resumeId: UUID): Result {
        val ownerId = repository.getOwnerId(resumeId) ?: return Result.NotFound
        if (ownerId != callerId) return Result.Forbidden
        repository.delete(resumeId)
        return Result.Success
    }
}
