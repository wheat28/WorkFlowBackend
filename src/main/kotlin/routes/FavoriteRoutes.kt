package routes

import domain.repository.FavoriteRepository
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.favoriteRoutes(favoriteRepository: FavoriteRepository) {
    authenticate("auth-jwt") {

        post("/favorites/{vacancyId}") {
            val seekerId = UUID.fromString(
                call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
            )
            val vacancyId = runCatching {
                UUID.fromString(call.parameters["vacancyId"])
            }.getOrElse {
                call.respond(HttpStatusCode.BadRequest, "Неверный ID вакансии")
                return@post
            }
            favoriteRepository.add(seekerId, vacancyId)
            call.respond(HttpStatusCode.Created)
        }

        delete("/favorites/{vacancyId}") {
            val seekerId = UUID.fromString(
                call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
            )
            val vacancyId = runCatching {
                UUID.fromString(call.parameters["vacancyId"])
            }.getOrElse {
                call.respond(HttpStatusCode.BadRequest, "Неверный ID вакансии")
                return@delete
            }
            favoriteRepository.remove(seekerId, vacancyId)
            call.respond(HttpStatusCode.OK)
        }

        get("/seekers/{id}/favorites") {
            val seekerId = runCatching {
                UUID.fromString(call.parameters["id"])
            }.getOrElse {
                call.respond(HttpStatusCode.BadRequest, "Неверный ID")
                return@get
            }
            call.respond(favoriteRepository.getBySeekerId(seekerId))
        }

        get("/favorites/check/{vacancyId}") {
            val seekerId = UUID.fromString(
                call.principal<JWTPrincipal>()!!.payload.getClaim("userId").asString()
            )
            val vacancyId = runCatching {
                UUID.fromString(call.parameters["vacancyId"])
            }.getOrElse {
                call.respond(HttpStatusCode.BadRequest, "Неверный ID вакансии")
                return@get
            }
            call.respond(mapOf("isFavorite" to favoriteRepository.isFavorite(seekerId, vacancyId)))
        }
    }
}
