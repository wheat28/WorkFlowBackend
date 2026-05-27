package data.repository

import data.database.*
import data.dto.employer.EmployerStatsResponse
import data.dto.vacancy.VacancyRequest
import data.dto.vacancy.VacancyResponse
import domain.repository.VacancyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class VacancyRepositoryImpl : VacancyRepository {

    override suspend fun getAll(): List<VacancyResponse> = withContext(Dispatchers.IO) {
        transaction {
            val rows = (VacancyTable innerJoin EmployerTable)
                .selectAll()
                .where { VacancyTable.isActive eq true }
                .orderBy(VacancyTable.createdAt to SortOrder.DESC)
                .toList()
            val skills = skillsFor(rows.map { it[VacancyTable.id] })
            rows.map { it.toVacancyResponse(skills[it[VacancyTable.id]] ?: emptyList()) }
        }
    }

    override suspend fun getById(id: UUID): VacancyResponse? = withContext(Dispatchers.IO) {
        transaction {
            val row = (VacancyTable innerJoin EmployerTable)
                .selectAll()
                .where { VacancyTable.id eq id }
                .singleOrNull() ?: return@transaction null
            val skills = skillsFor(listOf(id))[id] ?: emptyList()
            row.toVacancyResponse(skills)
        }
    }

    override suspend fun getByEmployerId(employerId: UUID): List<VacancyResponse> = withContext(Dispatchers.IO) {
        transaction {
            val rows = (VacancyTable innerJoin EmployerTable)
                .selectAll()
                .where { VacancyTable.employerId eq employerId }
                .toList()
            val ids = rows.map { it[VacancyTable.id] }
            val skills = skillsFor(ids)
            val appCounts = appCountsFor(ids)
            rows.map { it.toVacancyResponse(skills[it[VacancyTable.id]] ?: emptyList(), appCounts[it[VacancyTable.id]] ?: 0) }
        }
    }

    override suspend fun getOwnerId(id: UUID): UUID? = withContext(Dispatchers.IO) {
        transaction {
            VacancyTable.selectAll()
                .where { VacancyTable.id eq id }
                .singleOrNull()
                ?.get(VacancyTable.employerId)
        }
    }

    override suspend fun create(employerId: UUID, request: VacancyRequest): UUID = withContext(Dispatchers.IO) {
        transaction {
            val vacancyId = VacancyTable.insert {
                it[VacancyTable.employerId] = employerId
                it[categoryId] = request.categoryId
                it[title] = request.title
                it[description] = request.description
                it[salaryFrom] = request.salaryFrom
                it[salaryTo] = request.salaryTo
                it[currency] = request.currency
                it[city] = request.city
                it[employmentType] = request.employmentType
                it[experience] = request.experience
            }[VacancyTable.id]

            request.skillIds.forEach { skillId ->
                VacancySkillTable.insert {
                    it[VacancySkillTable.vacancyId] = vacancyId
                    it[VacancySkillTable.skillId] = skillId
                }
            }
            vacancyId
        }
    }

    override suspend fun update(id: UUID, request: VacancyRequest): Boolean = withContext(Dispatchers.IO) {
        transaction {
            val updated = VacancyTable.update({ VacancyTable.id eq id }) {
                it[categoryId] = request.categoryId
                it[title] = request.title
                it[description] = request.description
                it[salaryFrom] = request.salaryFrom
                it[salaryTo] = request.salaryTo
                it[currency] = request.currency
                it[city] = request.city
                it[employmentType] = request.employmentType
                it[experience] = request.experience
            }

            VacancySkillTable.deleteWhere { vacancyId eq id }
            request.skillIds.forEach { skillId ->
                VacancySkillTable.insert {
                    it[vacancyId] = id
                    it[VacancySkillTable.skillId] = skillId
                }
            }
            updated > 0
        }
    }

    override suspend fun setActive(id: UUID, isActive: Boolean): Boolean = withContext(Dispatchers.IO) {
        transaction {
            VacancyTable.update({ VacancyTable.id eq id }) {
                it[VacancyTable.isActive] = isActive
            } > 0
        }
    }

    override suspend fun delete(id: UUID): Boolean = withContext(Dispatchers.IO) {
        transaction {
            FavoriteTable.deleteWhere { FavoriteTable.vacancyId eq id }
            ApplicationTable.deleteWhere { ApplicationTable.vacancyId eq id }
            VacancySkillTable.deleteWhere { vacancyId eq id }
            VacancyTable.deleteWhere { VacancyTable.id eq id } > 0
        }
    }

    override suspend fun getEmployerStats(employerId: UUID): EmployerStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val vacancies = VacancyTable.selectAll().where { VacancyTable.employerId eq employerId }.toList()
            val ids = vacancies.map { it[VacancyTable.id] }
            val totalVacancies = vacancies.size
            val activeVacancies = vacancies.count { it[VacancyTable.isActive] }
            if (ids.isEmpty()) return@transaction EmployerStatsResponse(totalVacancies, activeVacancies, 0, 0)
            val apps = ApplicationTable.selectAll().where { ApplicationTable.vacancyId inList ids }.toList()
            val totalApplications = apps.size
            val pendingApplications = apps.count { it[ApplicationTable.status] == "PENDING" }
            EmployerStatsResponse(totalVacancies, activeVacancies, totalApplications, pendingApplications)
        }
    }

    private fun appCountsFor(ids: List<UUID>): Map<UUID, Int> {
        if (ids.isEmpty()) return emptyMap()
        return ApplicationTable
            .selectAll()
            .where { ApplicationTable.vacancyId inList ids }
            .groupBy({ it[ApplicationTable.vacancyId] }) { 1 }
            .mapValues { it.value.size }
    }

    private fun skillsFor(ids: List<UUID>): Map<UUID, List<String>> {
        if (ids.isEmpty()) return emptyMap()
        return (VacancySkillTable innerJoin SkillTable)
            .selectAll()
            .where { VacancySkillTable.vacancyId inList ids }
            .groupBy({ it[VacancySkillTable.vacancyId] }) { it[SkillTable.name] }
    }

    private fun ResultRow.toVacancyResponse(skills: List<String>, applicationCount: Int = 0) = VacancyResponse(
        id = this[VacancyTable.id].toString(),
        employerId = this[VacancyTable.employerId].toString(),
        companyName = this[EmployerTable.companyName],
        categoryId = this[VacancyTable.categoryId],
        title = this[VacancyTable.title],
        description = this[VacancyTable.description],
        salaryFrom = this[VacancyTable.salaryFrom],
        salaryTo = this[VacancyTable.salaryTo],
        currency = this[VacancyTable.currency],
        city = this[VacancyTable.city],
        employmentType = this[VacancyTable.employmentType],
        experience = this[VacancyTable.experience],
        isActive = this[VacancyTable.isActive],
        skills = skills,
        applicationCount = applicationCount
    )
}
