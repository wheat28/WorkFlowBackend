package data.repository

import data.database.*
import data.dto.resume.*
import domain.repository.ResumeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.util.UUID

class ResumeRepositoryImpl : ResumeRepository {

    override suspend fun getById(id: UUID): ResumeResponse? = withContext(Dispatchers.IO) {
        transaction {
            val row = ResumeTable.selectAll()
                .where { ResumeTable.id eq id }
                .singleOrNull() ?: return@transaction null
            val skills = skillsFor(listOf(id))[id] ?: emptyList()
            val experiences = experiencesFor(listOf(id))[id] ?: emptyList()
            row.toResumeResponse(skills, experiences)
        }
    }

    override suspend fun getBySeekerID(seekerId: UUID): List<ResumeResponse> = withContext(Dispatchers.IO) {
        transaction {
            val rows = ResumeTable.selectAll()
                .where { ResumeTable.seekerId eq seekerId }
                .toList()
            val ids = rows.map { it[ResumeTable.id] }
            val skills = skillsFor(ids)
            val experiences = experiencesFor(ids)
            rows.map {
                it.toResumeResponse(
                    skills[it[ResumeTable.id]] ?: emptyList(),
                    experiences[it[ResumeTable.id]] ?: emptyList()
                )
            }
        }
    }

    override suspend fun getOwnerId(id: UUID): UUID? = withContext(Dispatchers.IO) {
        transaction {
            ResumeTable.selectAll()
                .where { ResumeTable.id eq id }
                .singleOrNull()
                ?.get(ResumeTable.seekerId)
        }
    }

    override suspend fun create(seekerId: UUID, request: ResumeRequest): UUID = withContext(Dispatchers.IO) {
        transaction {
            val resumeId = ResumeTable.insert {
                it[ResumeTable.seekerId] = seekerId
                it[title] = request.title
                it[position] = request.position
                it[salaryExpected] = request.salaryExpected
                it[currency] = request.currency
                it[city] = request.city
                it[employmentType] = request.employmentType
                it[about] = request.about
            }[ResumeTable.id]

            request.skillIds.forEach { skillId ->
                ResumeSkillTable.insert {
                    it[ResumeSkillTable.resumeId] = resumeId
                    it[ResumeSkillTable.skillId] = skillId
                }
            }
            resumeId
        }
    }

    override suspend fun update(id: UUID, request: ResumeRequest): Boolean = withContext(Dispatchers.IO) {
        transaction {
            val updated = ResumeTable.update({ ResumeTable.id eq id }) {
                it[title] = request.title
                it[position] = request.position
                it[salaryExpected] = request.salaryExpected
                it[currency] = request.currency
                it[city] = request.city
                it[employmentType] = request.employmentType
                it[about] = request.about
            }

            ResumeSkillTable.deleteWhere { resumeId eq id }
            request.skillIds.forEach { skillId ->
                ResumeSkillTable.insert {
                    it[resumeId] = id
                    it[ResumeSkillTable.skillId] = skillId
                }
            }
            updated > 0
        }
    }

    override suspend fun setActive(id: UUID, isActive: Boolean): Boolean = withContext(Dispatchers.IO) {
        transaction {
            ResumeTable.update({ ResumeTable.id eq id }) {
                it[ResumeTable.isActive] = isActive
            } > 0
        }
    }

    override suspend fun delete(id: UUID): Boolean = withContext(Dispatchers.IO) {
        transaction {
            ResumeSkillTable.deleteWhere { resumeId eq id }
            WorkExperienceTable.deleteWhere { WorkExperienceTable.resumeId eq id }
            ResumeTable.deleteWhere { ResumeTable.id eq id } > 0
        }
    }

    override suspend fun addWorkExperience(resumeId: UUID, request: WorkExperienceRequest): UUID = withContext(Dispatchers.IO) {
        transaction {
            WorkExperienceTable.insert {
                it[WorkExperienceTable.resumeId] = resumeId
                it[companyName] = request.companyName
                it[position] = request.position
                it[startDate] = LocalDate.parse(request.startDate)
                it[endDate] = request.endDate?.let { d -> LocalDate.parse(d) }
                it[description] = request.description
            }[WorkExperienceTable.id]
        }
    }

    override suspend fun deleteWorkExperience(id: UUID): Boolean = withContext(Dispatchers.IO) {
        transaction {
            WorkExperienceTable.deleteWhere { WorkExperienceTable.id eq id } > 0
        }
    }

    private fun skillsFor(ids: List<UUID>): Map<UUID, List<String>> {
        if (ids.isEmpty()) return emptyMap()
        return (ResumeSkillTable innerJoin SkillTable)
            .selectAll()
            .where { ResumeSkillTable.resumeId inList ids }
            .groupBy({ it[ResumeSkillTable.resumeId] }) { it[SkillTable.name] }
    }

    private fun experiencesFor(ids: List<UUID>): Map<UUID, List<WorkExperienceResponse>> {
        if (ids.isEmpty()) return emptyMap()
        return WorkExperienceTable.selectAll()
            .where { WorkExperienceTable.resumeId inList ids }
            .groupBy({ it[WorkExperienceTable.resumeId] }) { row ->
                WorkExperienceResponse(
                    id = row[WorkExperienceTable.id].toString(),
                    companyName = row[WorkExperienceTable.companyName],
                    position = row[WorkExperienceTable.position],
                    startDate = row[WorkExperienceTable.startDate].toString(),
                    endDate = row[WorkExperienceTable.endDate]?.toString(),
                    description = row[WorkExperienceTable.description]
                )
            }
    }

    private fun ResultRow.toResumeResponse(skills: List<String>, workExperiences: List<WorkExperienceResponse>) = ResumeResponse(
        id = this[ResumeTable.id].toString(),
        seekerId = this[ResumeTable.seekerId].toString(),
        title = this[ResumeTable.title],
        position = this[ResumeTable.position],
        salaryExpected = this[ResumeTable.salaryExpected],
        currency = this[ResumeTable.currency],
        city = this[ResumeTable.city],
        employmentType = this[ResumeTable.employmentType],
        about = this[ResumeTable.about],
        isActive = this[ResumeTable.isActive],
        skills = skills,
        workExperiences = workExperiences
    )
}
