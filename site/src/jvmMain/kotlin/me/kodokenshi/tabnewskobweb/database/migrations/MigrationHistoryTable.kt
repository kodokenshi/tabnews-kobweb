package me.kodokenshi.tabnewskobweb.database.migrations

import org.jetbrains.exposed.v1.core.Table

object MigrationHistoryTable : Table("kodokenshi_migration_history") {
  val version = long("version").uniqueIndex()
  val author = varchar("author", 50)
  val description = varchar("description", 255)
  val checksum = varchar("checksum", 64)
  val installedOn = long("installed_on")
	
  override val primaryKey = PrimaryKey(version)
}
