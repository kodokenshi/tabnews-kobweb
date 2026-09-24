package me.kodokenshi.tabnewskobweb.database

import me.kodokenshi.tabnewskobweb.infra.InternalServerError
import me.kodokenshi.tabnewskobweb.infra.ServiceError
import me.kodokenshi.tabnewskobweb.infra.env
import org.jetbrains.exposed.v1.core.VarCharColumnType
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

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

  private val database by lazy {
    Database.connect(
      url = POSTGRES_URL,
      user = POSTGRES_USER,
      password = POSTGRES_PASSWORD,
      driver = POSTGRES_DRIVER,
    )
  }

  fun version() =
    transaction {
      exec("show server_version;") {
        if (it.next()) it.getString(1) else null
      }
    }

  fun maxConnections() =
    transaction {
      exec("show max_connections;") {
        if (it.next()) it.getInt(1) else 0
      }
    }

  fun openedConnections() =
    transaction {
      exec(
        stmt = "select count(*)::int from pg_stat_activity where datname = ?;",
        args = listOf(VarCharColumnType() to env.get("POSTGRES_DB")),
      ) {
        if (it.next()) it.getInt(1) else 0
      }
    }

  fun <T> transaction(statement: JdbcTransaction.() -> T) =
    try {
      transaction(database, statement = statement)
    } catch (cause: Throwable) {
      throw cause as? InternalServerError ?: ServiceError(
        message = "Erro na conexão com banco ou na query.",
        cause = cause,
      )
    }
}
