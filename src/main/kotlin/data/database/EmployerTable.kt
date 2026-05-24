package data.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object EmployerTable : Table("employers") {
    val id = uuid("id").autoGenerate()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val companyName = varchar("company_name", 255)
    val description = text("description").nullable()
    val website = varchar("website", 500).nullable()
    val logoUrl = varchar("logo_url", 500).nullable()
    val city = varchar("city", 100).nullable()
    val industry = varchar("industry", 100).nullable()
    val phone = varchar("phone", 20).nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
    val updatedAt = timestamp("updated_at").default(Instant.now())

    override val primaryKey = PrimaryKey(id)
}
