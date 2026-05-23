package data.database

import org.jetbrains.exposed.sql.Table

object SkillTable : Table("skills") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100).uniqueIndex()

    override val primaryKey = PrimaryKey(id)
}
