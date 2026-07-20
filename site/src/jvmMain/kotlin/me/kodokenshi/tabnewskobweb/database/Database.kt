package me.kodokenshi.tabnewskobweb.database

import io.github.cdimascio.dotenv.dotenv
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object Database {
	
	private val env by lazy {
		dotenv {
			directory = "../"
			filename = ".env.development"
			ignoreIfMissing = true
		}
	}
	private val database by lazy {
		Database.connect(
			url = env.get("POSTGRES_URL")
				.replace("POSTGRES_HOST", env.get("POSTGRES_HOST"))
				.replace("POSTGRES_PORT", env.get("POSTGRES_PORT"))
				.replace("POSTGRES_DB", env.get("POSTGRES_DB")),
			driver = env.get("POSTGRES_DRIVER"),
			user = env.get("POSTGRES_USER"),
			password = env.get("POSTGRES_PASSWORD")
		)
	}
	
	fun query(sql: String) =
		transaction(database) {
			exec(sql) {
				if (it.next()) it.getObject(1) else null
			}
		}
	
}