package data.database

import org.jetbrains.exposed.sql.Table

object CategoryTable : Table("categories") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100).uniqueIndex()
    val slug = varchar("slug", 100).uniqueIndex()

    override val primaryKey = PrimaryKey(id)
}
