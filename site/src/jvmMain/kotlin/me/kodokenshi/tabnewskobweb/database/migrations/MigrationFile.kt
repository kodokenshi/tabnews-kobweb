package me.kodokenshi.tabnewskobweb.database.migrations

import me.kodokenshi.tabnewskobweb.json.buildJson

data class MigrationFile(
	val version: Long,
	val author: String,
	val description: String,
	val script: String,
	val checksum: String
) {
	
	fun toInfoJson() = buildJson {
		put("version", version)
		put("author", author)
		put("description", description)
	}
	
}