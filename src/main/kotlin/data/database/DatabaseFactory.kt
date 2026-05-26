package data.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init(application: Application) {
        val config = application.environment.config
        val jdbcUrl = config.property("database.url").getString()
        val user = config.property("database.user").getString()
        val password = config.property("database.password").getString()

        val hikariConfig = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            this.jdbcUrl = jdbcUrl
            username = user
            this.password = password
            maximumPoolSize = 10
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }

        Database.connect(HikariDataSource(hikariConfig))

        transaction {
            SchemaUtils.create(
                UserTable,
                EmployerTable,
                CategoryTable,
                SkillTable,
                VacancyTable,
                ResumeTable,
                WorkExperienceTable,
                ApplicationTable,
                VacancySkillTable,
                ResumeSkillTable,
                RefreshTokenTable,
                FavoriteTable
            )
        }

        println("PostgreSQL (Neon) подключён успешно")
    }
}