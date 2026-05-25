package domain.usecase.vacancy

import data.dto.vacancy.VacancyRequest
import domain.repository.VacancyRepository
import java.util.UUID

class UpdateVacancyUseCase(private val repository: VacancyRepository) {

    sealed class Result {
        object Success : Result()
        object Forbidden : Result()
        object NotFound : Result()
    }

    suspend operator fun invoke(callerId: UUID, vacancyId: UUID, request: VacancyRequest): Result {
        val ownerId = repository.getOwnerId(vacancyId) ?: return Result.NotFound
        if (ownerId != callerId) return Result.Forbidden
        repository.update(vacancyId, request)
        return Result.Success
    }
}
