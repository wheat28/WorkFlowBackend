package domain.usecase.auth

import domain.repository.EmployerRepository
import domain.repository.UserRepository
import security.JwtConfig
import security.PasswordHasher

class LoginUseCase(
    private val userRepository: UserRepository,
    private val employerRepository: EmployerRepository
) {

    sealed class Result {
        data class Success(val token: String, val userType: String) : Result()
        object InvalidCredentials : Result()
    }

    suspend operator fun invoke(email: String, password: String): Result {
        val seekerHash = userRepository.getPasswordHash(email)
        if (seekerHash != null && PasswordHasher.verify(password, seekerHash)) {
            val user = userRepository.findByEmail(email)!!
            return Result.Success(JwtConfig.generateToken(user.id, "SEEKER"), "SEEKER")
        }

        val employerHash = employerRepository.getPasswordHash(email)
        if (employerHash != null && PasswordHasher.verify(password, employerHash)) {
            val employer = employerRepository.findByEmail(email)!!
            return Result.Success(JwtConfig.generateToken(employer.id, "EMPLOYER"), "EMPLOYER")
        }

        return Result.InvalidCredentials
    }
}
