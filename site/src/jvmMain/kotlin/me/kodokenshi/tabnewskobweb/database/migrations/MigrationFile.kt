package me.kodokenshi.tabnewskobweb.database.migrations

import me.kodokenshi.tabnewskobweb.json.jsonBuild

data class MigrationFile(
  val version: Long,
  val author: String,
  val description: String,
  val script: String,
  val checksum: String,
) {
  fun toInfoJson() =
    jsonBuild {
      "version" eq version
      "author" eq author
      "description" eq description
    }
}
