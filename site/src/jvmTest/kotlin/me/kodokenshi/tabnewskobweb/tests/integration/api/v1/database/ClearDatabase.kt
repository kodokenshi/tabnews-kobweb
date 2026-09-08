package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.database

import me.kodokenshi.tabnewskobweb.database.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun clearDatabase() = transaction(Database.database) {
	
	exec("drop schema public cascade; create schema public; grant all on schema public to public")
	val tables = exec(
		"select count(*) from information_schema.tables where table_schema = 'public'"
	) { rs ->
		if (rs.next()) rs.getInt(1) else -1
	}
	
	tables == 0
	
}
