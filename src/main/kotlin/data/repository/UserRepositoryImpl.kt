package data.repository

import data.database.UserTable
import data.dto.user.UserRegisterRequest
import data.dto.user.UserResponse
import data.dto.user.UserUpdateRequest
import domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import security.PasswordHasher
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class UserRepositoryImpl : UserRepository {

    override suspend fun findById(id: UUID): UserResponse? = withContext(Dispatchers.IO) {
        transaction {
            UserTable.selectAll()
                .where { UserTable.id eq id }
                .singleOrNull()
                ?.toUserResponse()
        }
    }

    override suspend fun findByEmail(email: String): UserResponse? = withContext(Dispatchers.IO) {
        transaction {
            UserTable.selectAll()
                .where { UserTable.email eq email }
                .singleOrNull()
                ?.toUserResponse()
        }
    }

    override suspend fun getPasswordHash(email: String): String? = withContext(Dispatchers.IO) {
        transaction {
            UserTable.selectAll()
                .where { UserTable.email eq email }
                .singleOrNull()
                ?.get(UserTable.passwordHash)
        }
    }

    override suspend fun create(request: UserRegisterRequest): UUID = withContext(Dispatchers.IO) {
        transaction {
            UserTable.insert {
                it[email] = request.email
                it[passwordHash] = PasswordHasher.hash(request.password)
                it[firstName] = request.firstName
                it[lastName] = request.lastName
                it[phone] = request.phone
                it[city] = request.city
            }[UserTable.id]
        }
    }

    override suspend fun emailExists(email: String): Boolean = withContext(Dispatchers.IO) {
        transaction {
            UserTable.selectAll()
                .where { UserTable.email eq email }
                .any()
        }
    }

    override suspend fun update(id: UUID, request: UserUpdateRequest): Boolean = withContext(Dispatchers.IO) {
        transaction {
            UserTable.update({ UserTable.id eq id }) {
                it[firstName] = request.firstName
                it[lastName] = request.lastName
                it[phone] = request.phone
                it[city] = request.city
                it[about] = request.about
            } > 0
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
