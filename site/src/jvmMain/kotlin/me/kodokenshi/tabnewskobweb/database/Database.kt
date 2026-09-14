package me.kodokenshi.tabnewskobweb.database

import io.github.cdimascio.dotenv.dotenv
import org.jetbrains.exposed.v1.core.IColumnType
import org.jetbrains.exposed.v1.core.VarCharColumnType
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.sql.ResultSet

object Database {
  val POSTGRES_URL get() =
    env
      .get("POSTGRES_URL", "jdbc:postgresql://POSTGRES_HOST:POSTGRES_PORT/POSTGRES_DB")
      .replace("POSTGRES_HOST", POSTGRES_HOST)
      .replace("POSTGRES_PORT", POSTGRES_PORT)
      .replace("POSTGRES_DB", POSTGRES_DB)
      .replace("POSTGRES_USER", POSTGRES_USER)
      .replace("POSTGRES_PASSWORD", POSTGRES_PASSWORD)
	
  val POSTGRES_HOST: String get() = env.get("POSTGRES_HOST", "localhost")
  val POSTGRES_PORT: String get() = env.get("POSTGRES_PORT", "5432")
  val POSTGRES_DB: String get() = env.get("POSTGRES_DB", "local_db")
  val POSTGRES_USER: String get() = env.get("POSTGRES_USER", "local_user")
  val POSTGRES_PASSWORD: String get() = env.get("POSTGRES_PASSWORD", "local_password")
  val POSTGRES_DRIVER: String get() = env.get("POSTGRES_DRIVER", "org.postgresql.Driver")
	
  private val env by lazy {
    dotenv {
      directory = "../"
      filename = ".env.development"
      ignoreIfMissing = true
    }
  }
  val database by lazy {
    Database.connect(
      url = POSTGRES_URL,
      user = POSTGRES_USER,
      password = POSTGRES_PASSWORD,
      driver = POSTGRES_DRIVER,
    )
  }

  fun version() = database.fullVersion

  fun maxConnections() = query("show max_connections") { if (it.next()) it.getInt(1) else 0 }

  fun openedConnections() =
    query(
      "select count(*)::int from pg_stat_activity where datname = ?",
      listOf(
        VarCharColumnType() to env.get("POSTGRES_DB"),
      ),
    ) { if (it.next()) it.getInt(1) else 0 }

  private fun <T> query(
    sql: String,
    args: Iterable<Pair<IColumnType<*>, Any?>> = emptyList(),
    exec: (ResultSet) -> T,
  ) = transaction(database) {
    exec(sql, args, transform = exec)
  }
}
