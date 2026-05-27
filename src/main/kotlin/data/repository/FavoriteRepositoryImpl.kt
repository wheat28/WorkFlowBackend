package data.repository

import data.database.*
import data.dto.vacancy.VacancyResponse
import domain.repository.FavoriteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class FavoriteRepositoryImpl : FavoriteRepository {

    override suspend fun add(seekerId: UUID, vacancyId: UUID) = withContext(Dispatchers.IO) {
        transaction {
            val exists = FavoriteTable.selectAll()
                .where { (FavoriteTable.seekerId eq seekerId) and (FavoriteTable.vacancyId eq vacancyId) }
                .count() > 0
            if (!exists) {
                FavoriteTable.insert {
                    it[FavoriteTable.seekerId] = seekerId
                    it[FavoriteTable.vacancyId] = vacancyId
                }
            }
        }
    }

    override suspend fun remove(seekerId: UUID, vacancyId: UUID) = withContext(Dispatchers.IO) {
        transaction {
            FavoriteTable.deleteWhere {
                (FavoriteTable.seekerId eq seekerId) and (FavoriteTable.vacancyId eq vacancyId)
            }
        }
        Unit
    }

    override suspend fun getBySeekerId(seekerId: UUID): List<VacancyResponse> = withContext(Dispatchers.IO) {
        transaction {
            val rows = (FavoriteTable
                .join(VacancyTable, JoinType.INNER, FavoriteTable.vacancyId, VacancyTable.id)
                .join(EmployerTable, JoinType.INNER, VacancyTable.employerId, EmployerTable.id))
                .selectAll()
                .where { FavoriteTable.seekerId eq seekerId }
                .toList()

            val vacancyIds = rows.map { it[VacancyTable.id] }
            val skillsByVacancy = if (vacancyIds.isEmpty()) emptyMap() else {
                (VacancySkillTable innerJoin SkillTable)
                    .selectAll()
                    .where { VacancySkillTable.vacancyId inList vacancyIds }
                    .groupBy({ it[VacancySkillTable.vacancyId] }) { it[SkillTable.name] }
            }

            rows.map { row ->
                val vacancyId = row[VacancyTable.id]
                VacancyResponse(
                    id = vacancyId.toString(),
                    employerId = row[VacancyTable.employerId].toString(),
                    companyName = row[EmployerTable.companyName],
                    categoryId = row[VacancyTable.categoryId],
                    title = row[VacancyTable.title],
                    description = row[VacancyTable.description],
                    salaryFrom = row[VacancyTable.salaryFrom],
                    salaryTo = row[VacancyTable.salaryTo],
                    currency = row[VacancyTable.currency],
                    city = row[VacancyTable.city],
                    employmentType = row[VacancyTable.employmentType],
                    experience = row[VacancyTable.experience],
                    isActive = row[VacancyTable.isActive],
                    skills = skillsByVacancy[vacancyId] ?: emptyList()
                )
            }
        }
    }

    override suspend fun isFavorite(seekerId: UUID, vacancyId: UUID): Boolean = withContext(Dispatchers.IO) {
        transaction {
            FavoriteTable.selectAll()
                .where { (FavoriteTable.seekerId eq seekerId) and (FavoriteTable.vacancyId eq vacancyId) }
                .count() > 0
        }
    }
}
