package data.repository

import data.database.*
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
            (VacancyTable innerJoin EmployerTable)
                .selectAll()
                .where { VacancyTable.isActive eq true }
                .orderBy(VacancyTable.createdAt to SortOrder.DESC)
                .map { it.toVacancyResponse() }
        }
    }

    override suspend fun getById(id: UUID): VacancyResponse? = withContext(Dispatchers.IO) {
        transaction {
            (VacancyTable innerJoin EmployerTable)
                .selectAll()
                .where { VacancyTable.id eq id }
                .singleOrNull()
                ?.toVacancyResponse()
        }
    }

    override suspend fun getByEmployerId(employerId: UUID): List<VacancyResponse> = withContext(Dispatchers.IO) {
        transaction {
            (VacancyTable innerJoin EmployerTable)
                .selectAll()
                .where { VacancyTable.employerId eq employerId }
                .map { it.toVacancyResponse() }
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

    override suspend fun delete(id: UUID): Boolean = withContext(Dispatchers.IO) {
        transaction {
            VacancySkillTable.deleteWhere { vacancyId eq id }
            VacancyTable.deleteWhere { VacancyTable.id eq id } > 0
        }
    }

    private fun ResultRow.toVacancyResponse(): VacancyResponse {
        val vacancyId = this[VacancyTable.id]
        val skills = (VacancySkillTable innerJoin SkillTable)
            .selectAll()
            .where { VacancySkillTable.vacancyId eq vacancyId }
            .map { it[SkillTable.name] }

        return VacancyResponse(
            id = vacancyId.toString(),
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
            skills = skills
        )
    }
}
