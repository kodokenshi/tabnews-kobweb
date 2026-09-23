package me.kodokenshi.tabnewskobweb.models

import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.text
import me.kodokenshi.tabnewskobweb.database.Database
import me.kodokenshi.tabnewskobweb.infra.NotFoundError
import me.kodokenshi.tabnewskobweb.infra.ValidationError
import me.kodokenshi.tabnewskobweb.json.Json
import me.kodokenshi.tabnewskobweb.json.buildJson
import me.kodokenshi.tabnewskobweb.json.parseJson
import org.jetbrains.exposed.v1.core.VarCharColumnType
import org.jetbrains.exposed.v1.core.statements.StatementType

object User {
  fun findOneByUsername(username: String?): String? {
    val foundUser = runSelectQuery(username)
    return foundUser
  }

  suspend fun create(ctx: ApiContext): String? {
    val values =
      ctx.req.body
        ?.text()
        ?.parseJson() ?: return null

    validateUniqueUsername(values.getString("username"))
    validateUniqueEmail(values.getString("email"))

    val newUser = runInsertQuery(values)
    return newUser
  }

  private fun validateUniqueUsername(username: String?) {
    Database.transaction {
      exec(
        stmt = "select username from users where lower(username) = lower(?) limit 1;",
        args =
          listOf(
            VarCharColumnType() to username,
          ),
      ) {
        if (it.next()) {
          throw ValidationError(
            message = "O username informado já está sendo utilizado.",
            action = "Utilize outro username para realizar o cadastro.",
          )
        }
      }
    }
  }

  private fun validateUniqueEmail(email: String?) {
    Database.transaction {
      exec(
        stmt = "select email from users where lower(email) = lower(?) limit 1;",
        args =
          listOf(
            VarCharColumnType() to email,
          ),
      ) {
        if (it.next()) {
          throw ValidationError(
            message = "O email informado já está sendo utilizado.",
            action = "Utilize outro email para realizar o cadastro.",
          )
        }
      }
    }
  }

  private fun runInsertQuery(values: Json) =
    Database.transaction {
      exec(
        stmt = "insert into users (username, email, passwd) values (?, ?, ?) returning *;",
        explicitStatementType = StatementType.SELECT,
        args =
          listOf(
            VarCharColumnType() to values.getString("username"),
            VarCharColumnType() to values.getString("email"),
            VarCharColumnType() to values.getString("passwd"),
          ),
      ) {
        buildJson {
          val metaData = it.metaData
          val columnCount = metaData.columnCount
          while (it.next()) {
            repeat(columnCount) { index ->
              put(it.metaData.getColumnName(index + 1), it.getObject(index + 1).toString())
            }
          }
        }
      }
    }

  private fun runSelectQuery(username: String?) =
    Database.transaction {
      exec(
        stmt = "select * from users where lower(username) = lower(?) limit 1;",
        args =
          listOf(
            VarCharColumnType() to username,
          ),
      ) {
        buildJson {
          val metaData = it.metaData
          val columnCount = metaData.columnCount
          while (it.next()) {
            repeat(columnCount) { index ->
              put(it.metaData.getColumnName(index + 1), it.getObject(index + 1).toString())
            }
          }

          if (size() == 0) {
            throw NotFoundError(
              message = "O username informado não foi encontrado.",
              action = "Verifique se o username está correto.",
            )
          }
        }
      }
    }
}
