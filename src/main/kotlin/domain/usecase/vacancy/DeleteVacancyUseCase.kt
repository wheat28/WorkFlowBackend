package domain.usecase.vacancy

import domain.repository.VacancyRepository
import java.util.UUID

class DeleteVacancyUseCase(private val repository: VacancyRepository) {

    sealed class Result {
        object Success : Result()
        object Forbidden : Result()
        object NotFound : Result()
    }

    suspend operator fun invoke(callerId: UUID, vacancyId: UUID): Result {
        val ownerId = repository.getOwnerId(vacancyId) ?: return Result.NotFound
        if (ownerId != callerId) return Result.Forbidden
        repository.delete(vacancyId)
        return Result.Success
    }
}
