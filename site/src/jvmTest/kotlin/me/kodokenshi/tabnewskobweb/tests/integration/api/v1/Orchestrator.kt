package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.parseOrNull
import kotlinx.datetime.toInstant
import me.kodokenshi.tabnewskobweb.database.Database
import me.kodokenshi.tabnewskobweb.database.migrations.defaultMigrations
import me.kodokenshi.tabnewskobweb.json.JsonReader
import me.kodokenshi.tabnewskobweb.json.JsonWriter
import me.kodokenshi.tabnewskobweb.json.json
import me.kodokenshi.tabnewskobweb.json.jsonBuild
import me.kodokenshi.tabnewskobweb.models.User
import net.datafaker.Faker
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.Uuid

val faker by lazy { Faker() }

val client by lazy {
  HttpClient {
    install(HttpTimeout) {
      connectTimeoutMillis = 1000
    }
  }
}

object Orchestrator {
  suspend fun waitForAllServices(maxRetries: Int = 100) =
    check(
      withTimeoutOrNull(1.minutes) {
        var retries = 0
        var fetch = false
        while (!fetch && retries <= maxRetries) {
          fetch = fetchStatusPage()
          if (!fetch) {
            delay(500.milliseconds)
            retries++
          }
        }
        fetch
      } == true,
    ) { "Could not fetch status page" }

  private suspend fun fetchStatusPage() =
    try {
      client.get("http://localhost:8080/api/v1/status").status == HttpStatusCode.OK
    } catch (_: Throwable) {
      false
    }

  fun clearDatabase() =
    Database.transaction {
      exec("drop schema public cascade; create schema public; grant all on schema public to public;")
      val tables =
        exec(
          "select count(*) from information_schema.tables where table_schema = 'public';",
        ) { rs ->
          if (rs.next()) rs.getInt(1) else -1
        }
			
      tables == 0
    }

  fun runPendingMigrations() = defaultMigrations().migrate(logInfoOnConsole = TestConfig.IS_VERBOSE)

  suspend fun createUserURL(values: JsonWriter = json {}): HttpResponse {
    populateUserValues(values)
    return client.post("http://localhost:8080/api/v1/users") {
      contentType(ContentType.Application.Json)
      setBody(values.toString())
    }
  }

  fun createUser(values: JsonWriter = json {}): JsonReader? {
    populateUserValues(values)
    return User.create(values)
  }

  suspend fun findUserURL(username: String) = client.get("http://localhost:8080/api/v1/users/$username")

  fun findUser(username: String) = User.findOneByUsername(username)

  suspend fun updateUserURL(
    username: String,
    values: JsonReader = json {},
  ) = client.patch("http://localhost:8080/api/v1/users/$username") {
    contentType(ContentType.Application.Json)
    setBody(
      jsonBuild {
        values.getString("username")?.let { "username" eq it }
        values.getString("email")?.let { "email" eq it }
        values.getString("passwd")?.let { "passwd" eq it }
      },
    )
  }

  fun updateUser(
    username: String,
    values: JsonWriter = json {},
  ) = User.update(username, values)

  private fun populateUserValues(values: JsonWriter) {
    values write {
      "username" eq
        "username".getString(
          faker
            .credentials()
            .username()
            .replace("[_.-]".toRegex(), ""),
        )
      "email" eq "email".getString(faker.internet().emailAddress())
      "passwd" eq "passwd".getString("senha123")
    }
  }

  fun extractUuidVersion(uuid: String?) =
    Uuid.parseOrNull(uuid.orEmpty())?.toLongs { mostSignificantBits, _ ->
      (mostSignificantBits shr 12) and 0xF
    }

  fun parsePostgresTimestamp(timestamp: String?) =
    timestamp
      ?.replace(' ', 'T')
      ?.let {
        LocalDateTime.parseOrNull(it)
      }?.toInstant(TimeZone.UTC)
}
