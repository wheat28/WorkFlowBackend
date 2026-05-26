package domain.repository

import data.dto.vacancy.VacancyResponse
import java.util.UUID

interface FavoriteRepository {
    suspend fun add(seekerId: UUID, vacancyId: UUID)
    suspend fun remove(seekerId: UUID, vacancyId: UUID)
    suspend fun getBySeekerId(seekerId: UUID): List<VacancyResponse>
    suspend fun isFavorite(seekerId: UUID, vacancyId: UUID): Boolean
}
