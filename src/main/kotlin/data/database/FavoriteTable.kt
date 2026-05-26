package data.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object FavoriteTable : Table("favorites") {
    val id = uuid("id").autoGenerate()
    val seekerId = uuid("seeker_id").references(UserTable.id)
    val vacancyId = uuid("vacancy_id").references(VacancyTable.id)
    val createdAt = timestamp("created_at").default(Instant.now())

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex(seekerId, vacancyId)
    }
}
