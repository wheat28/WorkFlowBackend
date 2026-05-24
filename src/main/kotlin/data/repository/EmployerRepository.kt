package data.repository

import data.database.EmployerTable
import data.dto.employer.EmployerRegisterRequest
import data.dto.employer.EmployerResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class EmployerRepository {

    suspend fun findById(id: UUID): EmployerResponse? = withContext(Dispatchers.IO) {
        transaction {
            EmployerTable.selectAll()
                .where { EmployerTable.id eq id }
                .singleOrNull()
                ?.toEmployerResponse()
        }
    }

    suspend fun findByEmail(email: String): EmployerResponse? = withContext(Dispatchers.IO) {
        transaction {
            EmployerTable.selectAll()
                .where { EmployerTable.email eq email }
                .singleOrNull()
                ?.toEmployerResponse()
        }
    }

    suspend fun getPasswordHash(email: String): String? = withContext(Dispatchers.IO) {
        transaction {
            EmployerTable.selectAll()
                .where { EmployerTable.email eq email }
                .singleOrNull()
                ?.get(EmployerTable.passwordHash)
        }
    }

    suspend fun create(request: EmployerRegisterRequest): UUID = withContext(Dispatchers.IO) {
        transaction {
            EmployerTable.insert {
                it[email] = request.email
                it[passwordHash] = request.password
                it[companyName] = request.companyName
                it[description] = request.description
                it[website] = request.website
                it[city] = request.city
                it[industry] = request.industry
                it[phone] = request.phone
            }[EmployerTable.id]
        }
    }

    suspend fun emailExists(email: String): Boolean = withContext(Dispatchers.IO) {
        transaction {
            EmployerTable.selectAll()
                .where { EmployerTable.email eq email }
                .any()
        }
    }

    private fun ResultRow.toEmployerResponse() = EmployerResponse(
        id = this[EmployerTable.id].toString(),
        email = this[EmployerTable.email],
        companyName = this[EmployerTable.companyName],
        description = this[EmployerTable.description],
        website = this[EmployerTable.website],
        logoUrl = this[EmployerTable.logoUrl],
        city = this[EmployerTable.city],
        industry = this[EmployerTable.industry],
        phone = this[EmployerTable.phone]
    )
}
