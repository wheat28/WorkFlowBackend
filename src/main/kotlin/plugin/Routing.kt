package plugin

import data.repository.*
import domain.usecase.auth.LoginUseCase
import domain.usecase.resume.*
import domain.usecase.vacancy.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import routes.*

fun Application.configureRouting() {
    val userRepository = UserRepositoryImpl()
    val employerRepository = EmployerRepositoryImpl()
    val vacancyRepository = VacancyRepositoryImpl()
    val resumeRepository = ResumeRepositoryImpl()
    val applicationRepository = ApplicationRepositoryImpl()

    val loginUseCase = LoginUseCase(userRepository, employerRepository)

    val updateVacancyUseCase = UpdateVacancyUseCase(vacancyRepository)
    val deleteVacancyUseCase = DeleteVacancyUseCase(vacancyRepository)

    val updateResumeUseCase = UpdateResumeUseCase(resumeRepository)
    val deleteResumeUseCase = DeleteResumeUseCase(resumeRepository)
    val addWorkExperienceUseCase = AddWorkExperienceUseCase(resumeRepository)
    val deleteWorkExperienceUseCase = DeleteWorkExperienceUseCase(resumeRepository)

    routing {
        get("/") {
            call.respondText("WorkFlow API")
        }

        authRoutes(loginUseCase)
        userRoutes(userRepository)
        employerRoutes(employerRepository, vacancyRepository)
        vacancyRoutes(vacancyRepository, updateVacancyUseCase, deleteVacancyUseCase)
        resumeRoutes(resumeRepository, updateResumeUseCase, deleteResumeUseCase, addWorkExperienceUseCase, deleteWorkExperienceUseCase)
        applicationRoutes(applicationRepository)
    }
}
