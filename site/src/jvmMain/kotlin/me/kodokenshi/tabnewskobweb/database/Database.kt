package me.kodokenshi.tabnewskobweb.database

import io.github.cdimascio.dotenv.dotenv
import org.jetbrains.exposed.v1.core.IColumnType
import org.jetbrains.exposed.v1.core.VarCharColumnType
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.sql.ResultSet

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
			url = env.get("POSTGRES_URL", "jdbc:postgresql://POSTGRES_HOST:POSTGRES_PORT/POSTGRES_DB")
				.replace("POSTGRES_HOST", env.get("POSTGRES_HOST", "localhost"))
				.replace("POSTGRES_PORT", env.get("POSTGRES_PORT", "5432"))
				.replace("POSTGRES_DB", env.get("POSTGRES_DB", "local_db"))
				.replace("POSTGRES_PASSWORD", env.get("POSTGRES_PASSWORD", "local_password"))
				.replace("POSTGRES_USER", env.get("POSTGRES_USER", "local_user")),
			user = env.get("POSTGRES_USER", "local_user"),
			password = env.get("POSTGRES_PASSWORD", "local_password"),
			driver = env.get("POSTGRES_DRIVER", "org.postgresql.Driver"),
		)
	}
	
	fun version() = database.fullVersion
	fun maxConnections() = query("show max_connections") { if (it.next()) it.getInt(1) else 0 }
	fun openedConnections() =
		query(
			"select count(*)::int from pg_stat_activity where datname = ?",
			listOf(
				VarCharColumnType() to env.get("POSTGRES_DB")
			)
		) { if (it.next()) it.getInt(1) else 0 }
	
	private fun <T> query(
		sql: String,
		args: Iterable<Pair<IColumnType<*>, Any?>> = emptyList(),
		exec: (ResultSet) -> T,
	) =
		transaction(database) {
			exec(sql, args, transform = exec)
		}
	
}