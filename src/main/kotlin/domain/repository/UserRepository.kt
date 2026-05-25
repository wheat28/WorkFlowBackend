package domain.repository

import data.dto.user.UserRegisterRequest
import data.dto.user.UserResponse
import java.util.UUID

interface UserRepository {
    suspend fun findById(id: UUID): UserResponse?
    suspend fun findByEmail(email: String): UserResponse?
    suspend fun getPasswordHash(email: String): String?
    suspend fun create(request: UserRegisterRequest): UUID
    suspend fun emailExists(email: String): Boolean
}
