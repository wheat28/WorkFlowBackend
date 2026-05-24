package plugin

import data.repository.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import routes.*

fun Application.configureRouting() {
    val userRepository = UserRepository()
    val employerRepository = EmployerRepository()
    val vacancyRepository = VacancyRepository()
    val resumeRepository = ResumeRepository()
    val applicationRepository = ApplicationRepository()

    routing {
        get("/") {
            call.respondText("WorkFlow API")
        }

        userRoutes(userRepository)
        employerRoutes(employerRepository)
        vacancyRoutes(vacancyRepository)
        resumeRoutes(resumeRepository)
        applicationRoutes(applicationRepository)
    }
}
