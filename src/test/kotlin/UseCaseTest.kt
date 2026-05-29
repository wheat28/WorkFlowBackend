package com.example

import data.dto.employer.EmployerRegisterRequest
import data.dto.employer.EmployerResponse
import data.dto.employer.EmployerStatsResponse
import data.dto.employer.EmployerUpdateRequest
import data.dto.resume.ResumeRequest
import data.dto.resume.ResumeResponse
import data.dto.resume.WorkExperienceRequest
import data.dto.user.UserRegisterRequest
import data.dto.user.UserResponse
import data.dto.user.UserUpdateRequest
import data.dto.vacancy.VacancyRequest
import data.dto.vacancy.VacancyResponse
import domain.repository.EmployerRepository
import domain.repository.ResumeRepository
import domain.repository.UserRepository
import domain.repository.VacancyRepository
import domain.usecase.auth.LoginUseCase
import domain.usecase.resume.DeleteResumeUseCase
import domain.usecase.vacancy.DeleteVacancyUseCase
import domain.usecase.vacancy.UpdateVacancyUseCase
import kotlinx.coroutines.runBlocking
import security.PasswordHasher
import java.util.UUID
import kotlin.test.*


private class FakeUserRepository(
    private val hashes: Map<String, String> = emptyMap(),
    private val users: Map<String, UserResponse> = emptyMap()
) : UserRepository {
    override suspend fun findById(id: UUID): UserResponse? = users.values.find { it.id == id.toString() }
    override suspend fun findByEmail(email: String): UserResponse? = users[email]
    override suspend fun getPasswordHash(email: String): String? = hashes[email]
    override suspend fun create(request: UserRegisterRequest): UUID = UUID.randomUUID()
    override suspend fun emailExists(email: String): Boolean = hashes.containsKey(email)
    override suspend fun update(id: UUID, request: UserUpdateRequest): Boolean = true
}

private class FakeEmployerRepository(
    private val hashes: Map<String, String> = emptyMap(),
    private val employers: Map<String, EmployerResponse> = emptyMap()
) : EmployerRepository {
    override suspend fun findById(id: UUID): EmployerResponse? = employers.values.find { it.id == id.toString() }
    override suspend fun findByEmail(email: String): EmployerResponse? = employers[email]
    override suspend fun getPasswordHash(email: String): String? = hashes[email]
    override suspend fun create(request: EmployerRegisterRequest): UUID = UUID.randomUUID()
    override suspend fun update(id: UUID, request: EmployerUpdateRequest): Boolean = true
    override suspend fun emailExists(email: String): Boolean = hashes.containsKey(email)
}

private class FakeVacancyRepository(
    private val ownerMap: Map<UUID, UUID> = emptyMap()
) : VacancyRepository {
    var deleteCalled = false
    var updateCalled = false

    override suspend fun getAll(): List<VacancyResponse> = emptyList()
    override suspend fun getById(id: UUID): VacancyResponse? = null
    override suspend fun getByEmployerId(employerId: UUID): List<VacancyResponse> = emptyList()
    override suspend fun getOwnerId(id: UUID): UUID? = ownerMap[id]
    override suspend fun create(employerId: UUID, request: VacancyRequest): UUID = UUID.randomUUID()
    override suspend fun update(id: UUID, request: VacancyRequest): Boolean { updateCalled = true; return true }
    override suspend fun setActive(id: UUID, isActive: Boolean): Boolean = true
    override suspend fun delete(id: UUID): Boolean { deleteCalled = true; return true }
    override suspend fun getEmployerStats(employerId: UUID): EmployerStatsResponse =
        EmployerStatsResponse(0, 0, 0, 0)
}

private class FakeResumeRepository(
    private val ownerMap: Map<UUID, UUID> = emptyMap()
) : ResumeRepository {
    var deleteCalled = false

    override suspend fun getById(id: UUID): ResumeResponse? = null
    override suspend fun getBySeekerID(seekerId: UUID): List<ResumeResponse> = emptyList()
    override suspend fun getOwnerId(id: UUID): UUID? = ownerMap[id]
    override suspend fun create(seekerId: UUID, request: ResumeRequest): UUID = UUID.randomUUID()
    override suspend fun update(id: UUID, request: ResumeRequest): Boolean = true
    override suspend fun setActive(id: UUID, isActive: Boolean): Boolean = true
    override suspend fun delete(id: UUID): Boolean { deleteCalled = true; return true }
    override suspend fun addWorkExperience(resumeId: UUID, request: WorkExperienceRequest): UUID = UUID.randomUUID()
    override suspend fun deleteWorkExperience(id: UUID): Boolean = true
}

class UseCaseTest {

    @Test
    fun `hash returns bcrypt formatted string`() {
        val hash = PasswordHasher.hash("password123")
        assertTrue(hash.startsWith("\$2"), "Expected bcrypt hash, got: $hash")
    }

    @Test
    fun `verify returns true for matching password`() {
        val password = "secret123"
        assertTrue(PasswordHasher.verify(password, PasswordHasher.hash(password)))
    }

    @Test
    fun `verify returns false for wrong password`() {
        val hash = PasswordHasher.hash("correctPassword")
        assertFalse(PasswordHasher.verify("wrongPassword", hash))
    }


    @Test
    fun `login with seeker credentials returns Success with SEEKER type`() = runBlocking {
        val email = "seeker@test.com"
        val password = "pass123"
        val id = UUID.randomUUID().toString()
        val user = UserResponse(id, email, "Ivan", "Petrov", null, null, null, null)

        val result = LoginUseCase(
            FakeUserRepository(mapOf(email to PasswordHasher.hash(password)), mapOf(email to user)),
            FakeEmployerRepository()
        ).invoke(email, password)

        assertIs<LoginUseCase.Result.Success>(result)
        assertEquals("SEEKER", result.userType)
        assertEquals(id, result.userId)
    }

    @Test
    fun `login with employer credentials returns Success with EMPLOYER type`() = runBlocking {
        val email = "hr@corp.com"
        val password = "pass456"
        val id = UUID.randomUUID().toString()
        val employer = EmployerResponse(id, email, "ACME Corp", null, null, null, null, null, null)

        val result = LoginUseCase(
            FakeUserRepository(),
            FakeEmployerRepository(mapOf(email to PasswordHasher.hash(password)), mapOf(email to employer))
        ).invoke(email, password)

        assertIs<LoginUseCase.Result.Success>(result)
        assertEquals("EMPLOYER", result.userType)
        assertEquals("ACME Corp", result.displayName)
    }

    @Test
    fun `login with wrong password returns InvalidCredentials`() {
        runBlocking {
            val email = "seeker@test.com"
            val id = UUID.randomUUID().toString()
            val user = UserResponse(id, email, "Ivan", "Petrov", null, null, null, null)

            val result = LoginUseCase(
                FakeUserRepository(mapOf(email to PasswordHasher.hash("correctPass")), mapOf(email to user)),
                FakeEmployerRepository()
            ).invoke(email, "wrongPass")

            assertIs<LoginUseCase.Result.InvalidCredentials>(result)
        }
    }

    @Test
    fun `login with unknown email returns InvalidCredentials`() {
        runBlocking {
            val result = LoginUseCase(FakeUserRepository(), FakeEmployerRepository())
                .invoke("nobody@test.com", "anypass")
            assertIs<LoginUseCase.Result.InvalidCredentials>(result)
        }
    }


    @Test
    fun `delete vacancy as owner returns Success`() = runBlocking {
        val ownerId = UUID.randomUUID()
        val vacancyId = UUID.randomUUID()
        val repo = FakeVacancyRepository(mapOf(vacancyId to ownerId))

        val result = DeleteVacancyUseCase(repo).invoke(ownerId, vacancyId)

        assertIs<DeleteVacancyUseCase.Result.Success>(result)
        assertTrue(repo.deleteCalled)
    }

    @Test
    fun `delete vacancy as non-owner returns Forbidden`() = runBlocking {
        val vacancyId = UUID.randomUUID()
        val repo = FakeVacancyRepository(mapOf(vacancyId to UUID.randomUUID()))

        val result = DeleteVacancyUseCase(repo).invoke(UUID.randomUUID(), vacancyId)

        assertIs<DeleteVacancyUseCase.Result.Forbidden>(result)
        assertFalse(repo.deleteCalled)
    }

    @Test
    fun `delete non-existent vacancy returns NotFound`() {
        runBlocking {
            val result = DeleteVacancyUseCase(FakeVacancyRepository())
                .invoke(UUID.randomUUID(), UUID.randomUUID())
            assertIs<DeleteVacancyUseCase.Result.NotFound>(result)
        }
    }


    private val sampleVacancyRequest = VacancyRequest(
        title = "Backend Dev", description = "Kotlin developer",
        employmentType = "FULL_TIME", experience = "NO_EXPERIENCE"
    )

    @Test
    fun `update vacancy as owner returns Success`() = runBlocking {
        val ownerId = UUID.randomUUID()
        val vacancyId = UUID.randomUUID()
        val repo = FakeVacancyRepository(mapOf(vacancyId to ownerId))

        val result = UpdateVacancyUseCase(repo).invoke(ownerId, vacancyId, sampleVacancyRequest)

        assertIs<UpdateVacancyUseCase.Result.Success>(result)
        assertTrue(repo.updateCalled)
    }

    @Test
    fun `update vacancy as non-owner returns Forbidden`() = runBlocking {
        val vacancyId = UUID.randomUUID()
        val repo = FakeVacancyRepository(mapOf(vacancyId to UUID.randomUUID()))

        val result = UpdateVacancyUseCase(repo).invoke(UUID.randomUUID(), vacancyId, sampleVacancyRequest)

        assertIs<UpdateVacancyUseCase.Result.Forbidden>(result)
        assertFalse(repo.updateCalled)
    }

    @Test
    fun `update non-existent vacancy returns NotFound`() {
        runBlocking {
            val result = UpdateVacancyUseCase(FakeVacancyRepository())
                .invoke(UUID.randomUUID(), UUID.randomUUID(), sampleVacancyRequest)
            assertIs<UpdateVacancyUseCase.Result.NotFound>(result)
        }
    }


    @Test
    fun `delete resume as owner returns Success`() = runBlocking {
        val ownerId = UUID.randomUUID()
        val resumeId = UUID.randomUUID()
        val repo = FakeResumeRepository(mapOf(resumeId to ownerId))

        val result = DeleteResumeUseCase(repo).invoke(ownerId, resumeId)

        assertIs<DeleteResumeUseCase.Result.Success>(result)
        assertTrue(repo.deleteCalled)
    }

    @Test
    fun `delete resume as non-owner returns Forbidden`() = runBlocking {
        val resumeId = UUID.randomUUID()
        val repo = FakeResumeRepository(mapOf(resumeId to UUID.randomUUID()))

        val result = DeleteResumeUseCase(repo).invoke(UUID.randomUUID(), resumeId)

        assertIs<DeleteResumeUseCase.Result.Forbidden>(result)
        assertFalse(repo.deleteCalled)
    }
}
