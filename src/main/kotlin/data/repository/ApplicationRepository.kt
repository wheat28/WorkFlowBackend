package data.repository

import data.database.*
import data.dto.application.ApplicationRequest
import data.dto.application.ApplicationResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class ApplicationRepository {

    suspend fun getBySeekerId(seekerId: UUID): List<ApplicationResponse> = withContext(Dispatchers.IO) {
        transaction {
            ApplicationTable
                .join(UserTable, JoinType.INNER, ApplicationTable.seekerId, UserTable.id)
                .join(VacancyTable, JoinType.INNER, ApplicationTable.vacancyId, VacancyTable.id)
                .selectAll()
                .where { ApplicationTable.seekerId eq seekerId }
                .map { it.toApplicationResponse() }
        }
    }

    suspend fun getByVacancyId(vacancyId: UUID): List<ApplicationResponse> = withContext(Dispatchers.IO) {
        transaction {
            ApplicationTable
                .join(UserTable, JoinType.INNER, ApplicationTable.seekerId, UserTable.id)
                .join(VacancyTable, JoinType.INNER, ApplicationTable.vacancyId, VacancyTable.id)
                .selectAll()
                .where { ApplicationTable.vacancyId eq vacancyId }
                .map { it.toApplicationResponse() }
        }
    }

    suspend fun create(seekerId: UUID, request: ApplicationRequest): UUID = withContext(Dispatchers.IO) {
        transaction {
            ApplicationTable.insert {
                it[ApplicationTable.seekerId] = seekerId
                it[ApplicationTable.vacancyId] = UUID.fromString(request.vacancyId)
                it[resumeId] = UUID.fromString(request.resumeId)
                it[status] = "PENDING"
                it[coverLetter] = request.coverLetter
            }[ApplicationTable.id]
        }
    }

    suspend fun updateStatus(id: UUID, status: String): Boolean = withContext(Dispatchers.IO) {
        transaction {
            ApplicationTable.update({ ApplicationTable.id eq id }) {
                it[ApplicationTable.status] = status
            } > 0
        }
    }

    private fun ResultRow.toApplicationResponse() = ApplicationResponse(
        id = this[ApplicationTable.id].toString(),
        seekerId = this[ApplicationTable.seekerId].toString(),
        seekerFirstName = this[UserTable.firstName],
        seekerLastName = this[UserTable.lastName],
        vacancyId = this[ApplicationTable.vacancyId].toString(),
        vacancyTitle = this[VacancyTable.title],
        resumeId = this[ApplicationTable.resumeId].toString(),
        status = this[ApplicationTable.status],
        coverLetter = this[ApplicationTable.coverLetter],
        createdAt = this[ApplicationTable.createdAt].toString()
    )
}
