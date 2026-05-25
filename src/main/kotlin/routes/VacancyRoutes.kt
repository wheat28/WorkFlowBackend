package routes

import data.dto.vacancy.VacancyRequest
import domain.repository.VacancyRepository
import domain.usecase.vacancy.DeleteVacancyUseCase
import domain.usecase.vacancy.UpdateVacancyUseCase
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.vacancyRoutes(
    vacancyRepository: VacancyRepository,
    updateVacancyUseCase: UpdateVacancyUseCase,
    deleteVacancyUseCase: DeleteVacancyUseCase
) {
    route("/vacancies") {

        get {
            call.respond(vacancyRepository.getAll())
        }

        get("/{id}") {
            val id = runCatching {
                UUID.fromString(call.parameters["id"])
            }.getOrElse {
                call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                return@get
            }

            val vacancy = vacancyRepository.getById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, "Вакансия не найдена")
            call.respond(vacancy)
        }

        authenticate("auth-jwt") {
            post {
                val employerId = UUID.fromString(
                    call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                )
                val request = call.receive<VacancyRequest>()
                val id = vacancyRepository.create(employerId, request)
                call.respond(HttpStatusCode.Created, mapOf("id" to id.toString()))
            }

            put("/{id}") {
                val vacancyId = runCatching {
                    UUID.fromString(call.parameters["id"])
                }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                    return@put
                }

                val callerId = UUID.fromString(
                    call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                )

                val request = call.receive<VacancyRequest>()
                when (updateVacancyUseCase(callerId, vacancyId, request)) {
                    is UpdateVacancyUseCase.Result.Success -> call.respond(HttpStatusCode.OK)
                    is UpdateVacancyUseCase.Result.Forbidden -> call.respond(HttpStatusCode.Forbidden, "Нет доступа")
                    is UpdateVacancyUseCase.Result.NotFound -> call.respond(HttpStatusCode.NotFound, "Вакансия не найдена")
                }
            }

            delete("/{id}") {
                val vacancyId = runCatching {
                    UUID.fromString(call.parameters["id"])
                }.getOrElse {
                    call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                    return@delete
                }

                val callerId = UUID.fromString(
                    call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
                )

                when (deleteVacancyUseCase(callerId, vacancyId)) {
                    is DeleteVacancyUseCase.Result.Success -> call.respond(HttpStatusCode.OK)
                    is DeleteVacancyUseCase.Result.Forbidden -> call.respond(HttpStatusCode.Forbidden, "Нет доступа")
                    is DeleteVacancyUseCase.Result.NotFound -> call.respond(HttpStatusCode.NotFound, "Вакансия не найдена")
                }
            }

}
    }
}
