package data.repository

import data.database.*
import data.dto.resume.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.util.UUID

class ResumeRepository {

    suspend fun getById(id: UUID): ResumeResponse? = withContext(Dispatchers.IO) {
        transaction {
            ResumeTable.selectAll()
                .where { ResumeTable.id eq id }
                .singleOrNull()
                ?.toResumeResponse()
        }
    }

    suspend fun getBySeekerID(seekerId: UUID): List<ResumeResponse> = withContext(Dispatchers.IO) {
        transaction {
            ResumeTable.selectAll()
                .where { ResumeTable.seekerId eq seekerId }
                .map { it.toResumeResponse() }
        }
    }

    suspend fun create(seekerId: UUID, request: ResumeRequest): UUID = withContext(Dispatchers.IO) {
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

    suspend fun update(id: UUID, request: ResumeRequest): Boolean = withContext(Dispatchers.IO) {
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

    suspend fun delete(id: UUID): Boolean = withContext(Dispatchers.IO) {
        transaction {
            ResumeSkillTable.deleteWhere { resumeId eq id }
            WorkExperienceTable.deleteWhere { WorkExperienceTable.resumeId eq id }
            ResumeTable.deleteWhere { ResumeTable.id eq id } > 0
        }
    }

    suspend fun addWorkExperience(resumeId: UUID, request: WorkExperienceRequest): UUID = withContext(Dispatchers.IO) {
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

    suspend fun deleteWorkExperience(id: UUID): Boolean = withContext(Dispatchers.IO) {
        transaction {
            WorkExperienceTable.deleteWhere { WorkExperienceTable.id eq id } > 0
        }
    }

    private fun ResultRow.toResumeResponse(): ResumeResponse {
        val id = this[ResumeTable.id]

        val skills = (ResumeSkillTable innerJoin SkillTable)
            .selectAll()
            .where { ResumeSkillTable.resumeId eq id }
            .map { it[SkillTable.name] }

        val workExperiences = WorkExperienceTable.selectAll()
            .where { WorkExperienceTable.resumeId eq id }
            .map { row ->
                WorkExperienceResponse(
                    id = row[WorkExperienceTable.id].toString(),
                    companyName = row[WorkExperienceTable.companyName],
                    position = row[WorkExperienceTable.position],
                    startDate = row[WorkExperienceTable.startDate].toString(),
                    endDate = row[WorkExperienceTable.endDate]?.toString(),
                    description = row[WorkExperienceTable.description]
                )
            }

        return ResumeResponse(
            id = id.toString(),
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
}
