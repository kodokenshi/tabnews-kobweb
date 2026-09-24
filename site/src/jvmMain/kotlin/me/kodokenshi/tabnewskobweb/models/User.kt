package me.kodokenshi.tabnewskobweb.models

import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.text
import me.kodokenshi.tabnewskobweb.database.Database
import me.kodokenshi.tabnewskobweb.infra.NotFoundError
import me.kodokenshi.tabnewskobweb.infra.ValidationError
import me.kodokenshi.tabnewskobweb.json.Json
import me.kodokenshi.tabnewskobweb.json.buildJson
import me.kodokenshi.tabnewskobweb.json.json
import me.kodokenshi.tabnewskobweb.json.parseJson
import me.kodokenshi.tabnewskobweb.json.toJsonElement
import org.jetbrains.exposed.v1.core.VarCharColumnType
import org.jetbrains.exposed.v1.core.statements.StatementType

object User {
  fun findOneByUsername(username: String?): Json {
    validateUsername(username)
    val foundUser = runSelectUsernameQuery(username!!) // nesse ponto, não é nulo
    return foundUser
  }

  suspend fun create(ctx: ApiContext): Json? {
    val values =
      ctx.req.body
        ?.text()
        ?.parseJson() ?: return null

    val username = values.getString("username")
    val email = values.getString("email")
    val passwd = values.getString("passwd")

    validateUniqueUsername(username)
    validateUniqueEmail(email)
    validatePassword(passwd)

    // nesse ponto, nada é nulo
    val newUser = runInsertUserQuery(username!!, email!!, passwd!!)
    return newUser
  }

  private fun validateUniqueUsername(username: String?) {
    validateUsername(username)
    validateField("username", username)
  }

  private fun validateUsername(username: String?) {
    if (username == null) {
      throw ValidationError(
        message = "Nenhum username informado.",
        action = "Informe um username e tente novamente.",
      )
    }
    if (username.length !in 1..30) {
      throw ValidationError(
        message = "Tamanho de username incorreto.",
        action = "Informe um username com 1 até 30 caracteres.",
      )
    }
  }

  private fun validateUniqueEmail(email: String?) = validateField("email", email)

  private fun validatePassword(passwd: String?) {
    if (passwd == null) {
      throw ValidationError(
        message = "Nenhuma senha informada.",
        action = "Informe uma senha para realizar o cadastro.",
      )
    }
  }

  private fun validateField(
    field: String,
    value: String?,
  ) {
    if (value == null) {
      throw ValidationError(
        message = "Nenhum $field informado.",
        action = "Informe um $field para realizar o cadastro.",
      )
    }
    val result =
      query(
        stmt = "select $field from users where lower($field) = lower(?) limit 1;",
        values = listOf(value),
      )
    if (result?.isNotEmpty() == true) {
      throw ValidationError(
        message = "O $field informado já está sendo utilizado.",
        action = "Utilize outro $field para realizar o cadastro.",
      )
    }
  }

  private fun runInsertUserQuery(
    username: String,
    email: String,
    passwd: String,
  ) = query(
    stmt = "insert into users (username, email, passwd) values (?, ?, ?) returning *;",
    explicitStatementType = StatementType.SELECT,
    values = listOf(username, email, passwd),
  )

  private fun runSelectUsernameQuery(username: String) =
    query(
      "select * from users where lower(username) = lower(?) limit 1;",
      values = listOf(username),
    ).let {
      if (it == null || it.size() == 0) {
        throw NotFoundError(
          message = "O username informado não foi encontrado.",
          action = "Verifique se o username está correto.",
        )
      } else {
        it
      }
    }

  private fun query(
    stmt: String,
    explicitStatementType: StatementType? = null,
    values: List<String>,
  ) = Database.transaction {
    exec(
      stmt = stmt,
      explicitStatementType = explicitStatementType,
      args =
        values.map {
          VarCharColumnType() to it
        },
    ) {
      json {
        val metaData = it.metaData
        val columnCount = metaData.columnCount
        while (it.next()) {
          repeat(columnCount) { index ->
            put(it.metaData.getColumnName(index + 1), it.getObject(index + 1).toJsonElement())
          }
        }
      }
    }
  }
}
