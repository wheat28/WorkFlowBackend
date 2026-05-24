package data.repository

import data.database.UserTable
import data.dto.user.UserRegisterRequest
import data.dto.user.UserResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class UserRepository {

    suspend fun findById(id: UUID): UserResponse? = withContext(Dispatchers.IO) {
        transaction {
            UserTable.selectAll()
                .where { UserTable.id eq id }
                .singleOrNull()
                ?.toUserResponse()
        }
    }

    suspend fun findByEmail(email: String): UserResponse? = withContext(Dispatchers.IO) {
        transaction {
            UserTable.selectAll()
                .where { UserTable.email eq email }
                .singleOrNull()
                ?.toUserResponse()
        }
    }

    suspend fun getPasswordHash(email: String): String? = withContext(Dispatchers.IO) {
        transaction {
            UserTable.selectAll()
                .where { UserTable.email eq email }
                .singleOrNull()
                ?.get(UserTable.passwordHash)
        }
    }

    suspend fun create(request: UserRegisterRequest): UUID = withContext(Dispatchers.IO) {
        transaction {
            UserTable.insert {
                it[email] = request.email
                it[passwordHash] = request.password
                it[firstName] = request.firstName
                it[lastName] = request.lastName
                it[phone] = request.phone
                it[city] = request.city
            }[UserTable.id]
        }
    }

    suspend fun emailExists(email: String): Boolean = withContext(Dispatchers.IO) {
        transaction {
            UserTable.selectAll()
                .where { UserTable.email eq email }
                .any()
        }
    }

    private fun ResultRow.toUserResponse() = UserResponse(
        id = this[UserTable.id].toString(),
        email = this[UserTable.email],
        firstName = this[UserTable.firstName],
        lastName = this[UserTable.lastName],
        phone = this[UserTable.phone],
        city = this[UserTable.city],
        avatarUrl = this[UserTable.avatarUrl],
        about = this[UserTable.about]
    )
}
