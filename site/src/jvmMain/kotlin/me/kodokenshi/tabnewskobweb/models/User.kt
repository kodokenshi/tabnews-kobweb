package me.kodokenshi.tabnewskobweb.models

import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.text
import me.kodokenshi.tabnewskobweb.database.Database
import me.kodokenshi.tabnewskobweb.database.uuidColumn
import me.kodokenshi.tabnewskobweb.database.varCharColumn
import me.kodokenshi.tabnewskobweb.infra.NotFoundError
import me.kodokenshi.tabnewskobweb.infra.ValidationError
import me.kodokenshi.tabnewskobweb.json.Json
import me.kodokenshi.tabnewskobweb.json.json
import me.kodokenshi.tabnewskobweb.json.parseJson
import me.kodokenshi.tabnewskobweb.json.toJsonElement
import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.v1.core.IColumnType
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
        ?.parseJson() ?: throw ValidationError()

    hashPasswordInValues(values)

    val username = values.getString("username")
    val email = values.getString("email")
    val password = values.getString("passwd") // nesse ponto, já deve estar validado

    validateUniqueUsername(username)
    validateUniqueEmail(email)

    // nesse ponto, nada é nulo
    val newUser = runInsertUserQuery(username!!, email!!, password!!)
    return newUser
  }

  suspend fun update(
    username: String?,
    ctx: ApiContext,
  ): Json? {
    val values =
      ctx.req.body
        ?.text()
        ?.parseJson() ?: throw ValidationError()
		
    val currentUser = findOneByUsername(username)

    if (values.contains("username")) {
      validateUniqueUsername(values.getString("username"))
    }
    if (values.contains("email")) {
      validateUniqueEmail(values.getString("email"))
    }
    if (values.contains("passwd")) {
      validatePassword(values.getString("passwd"))
      hashPasswordInValues(values)
    }

    val newUser = Json()
    newUser.spread(currentUser)
    newUser.spread(values)

    return runUpdateUserQuery(
      id = newUser.getString("id")!!,
      username = newUser.getString("username")!!,
      email = newUser.getString("email")!!,
      password = newUser.getString("passwd")!!,
    )
  }

  private fun hashPasswordInValues(values: Json) {
    val password = values.getString("passwd")
		
    validatePassword(password)
		
    val hashedPassword = Password.hash(password!!) // nesse ponto, não é nulo
    values.put("passwd", hashedPassword)
  }

  private fun validateUniqueUsername(username: String?) {
    validateUsername(username)
    validateField("username", username)
  }

  private fun validateUsername(username: String?) {
    if (username.isNullOrBlank()) {
      throw ValidationError(
        message = "Nenhum username informado.",
        action = "Informe um username para realizar esta operação.",
      )
    }
    if (username.length !in 1..30) {
      throw ValidationError(
        message = "Tamanho de username incorreto.",
        action = "Informe um username com 1 até 30 caracteres.",
      )
    }
  }

  private fun validateUniqueEmail(email: String?) {
    validateEmail(email)
    validateField("email", email)
  }

  private fun validateEmail(email: String?) {
    if (email.isNullOrBlank()) {
      throw ValidationError(
        message = "Nenhum email informado.",
        action = "Informe um email para realizar esta operação.",
      )
    }
    if (email.length !in 3..254) {
      throw ValidationError(
        message = "Tamanho de email incorreto.",
        action = "Informe um email com 3 até 254 caracteres.",
      )
    }
  }

  private fun validatePassword(password: String?) {
    if (password.isNullOrBlank()) {
      throw ValidationError(
        message = "Nenhuma senha informada.",
        action = "Informe uma senha para realizar esta operação.",
      )
    }
    if (password.length !in 1..60) {
      throw ValidationError(
        message = "Tamanho de senha incorreto.",
        action = "Informe uma senha com 1 até 60 caracteres.",
      )
    }
  }

  private fun validateField(
    field: String,
    value: String?,
  ) {
    if (value.isNullOrBlank()) {
      throw ValidationError(
        message = "Nenhum $field informado.",
        action = "Informe um $field para realizar esta operação.",
      )
    }
    val result =
      query(
        stmt =
          """
          select
          	$field
          from
          	users
          where
          	lower($field) = lower(?)
          limit
          	1
          """.trimIndent(),
        values = listOf(varCharColumn(value)),
      )
    if (result?.isNotEmpty() == true) {
      throw ValidationError(
        message = "O $field informado já está sendo utilizado.",
        action = "Utilize outro $field para realizar esta operação.",
      )
    }
  }

  private fun runInsertUserQuery(
    username: String,
    email: String,
    password: String,
  ) = query(
    stmt = "insert into users (username, email, passwd) values (?, ?, ?) returning *;",
    explicitStatementType = StatementType.SELECT,
    values =
      listOf(
        varCharColumn(username),
        varCharColumn(email),
        varCharColumn(password),
      ),
  )

  private fun runUpdateUserQuery(
    id: String,
    username: String,
    email: String,
    password: String,
  ) = query(
    stmt =
      """
      update
      	users
      set
      	username = ?,
      	email = ?,
      	passwd = ?,
      	updated_at = timezone('UTC', now())
      where
      	id = ?
      returning
      	*
      """.trimIndent(),
    explicitStatementType = StatementType.SELECT,
    values =
      listOf(
        varCharColumn(username),
        varCharColumn(email),
        varCharColumn(password),
        uuidColumn(id),
      ),
  )

  private fun runSelectUsernameQuery(username: String) =
    query(
      stmt =
        """
        select
        	*
        from
        	users
        where
        	lower(username) = lower(?)
        limit
        	1
        """.trimIndent(),
      values = listOf(varCharColumn(username)),
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
    @Language("sql") stmt: String,
    explicitStatementType: StatementType? = null,
    values: Iterable<Pair<IColumnType<*>, Any?>> = emptyList(),
  ) = Database.transaction {
    exec(
      stmt = stmt,
      explicitStatementType = explicitStatementType,
      args = values,
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
