import com.example.configurePostgres
import com.example.configureRouting
import com.example.configureSecurity
import com.example.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.engine.*
import io.ktor.server.netty.Netty

fun main(args: Array<String>) {
    embeddedServer(
        factory = Netty,
        port = 8080,
        host = "0.0.0.0",
        module = Application::rootModule
    ).start(wait = true)
}


fun Application.rootModule() {
    configurePostgres()
    configureSerialization()
    configureSecurity()
    configureRouting()
}
