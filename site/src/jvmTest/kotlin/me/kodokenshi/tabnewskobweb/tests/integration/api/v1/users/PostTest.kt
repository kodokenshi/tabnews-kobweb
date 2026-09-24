package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.users

import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.parseOrNull
import kotlinx.datetime.toInstant
import me.kodokenshi.tabnewskobweb.json.buildJson
import me.kodokenshi.tabnewskobweb.json.parseJson
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.TestConfig
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.database.clearDatabase
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.database.waitForMigrations
import me.kodokenshi.tabnewskobweb.models.Password
import me.kodokenshi.tabnewskobweb.models.User
import me.kodokenshi.tabnewskobweb.tests.services.client
import me.kodokenshi.tabnewskobweb.tests.services.waitForAllServices
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toBe
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toNotBeNull
import me.kodokenshi.tabnewskobweb.tests.test.testContext
import org.junit.jupiter.api.Test
import kotlin.uuid.Uuid

class PostTest {
  @Test
  suspend fun test() =
    testContext(this::class, TestConfig.IS_VERBOSE) {
      beforeAll {
        waitForAllServices()
        clearDatabase()
        waitForMigrations()
      }
      describe("POST /api/v1/users") {
        describe("Anonymous user") {
          describe("With unique and valid data") {
            val response =
              client.post("http://localhost:8080/api/v1/users") {
                contentType(ContentType.Application.Json)
                setBody(
                  buildJson {
                    put("username", "kodo")
                    put("email", "kodo@email.com")
                    put("passwd", "senha123")
                  },
                )
              }
            expect(response.status).toBe(HttpStatusCode.Created)

            val body = expect(response.bodyAsText().parseJson()).toNotBeNull()
            expect(extractUuidVersion(body.getString("id"))).toBe(4)
            expect(body.getString("username")).toBe("kodo")
            expect(body.getString("email")).toBe("kodo@email.com")
            expect(parsePostgresTimestamp(body.getString("created_at").orEmpty())).toNotBeNull()
            expect(parsePostgresTimestamp(body.getString("updated_at").orEmpty())).toNotBeNull()

            val userInDatabase = User.findOneByUsername("kodo")
            val storedPassword = userInDatabase.getString("passwd")
            expect(Password.compare("senha123", storedPassword)).toBe(true)
            expect(Password.compare("Senha123", storedPassword)).toBe(false)
            expect(Password.compare("SenhaErrada", storedPassword)).toBe(false)
          }

          describe("With invalid 'username'") {
            val response =
              client.post("http://localhost:8080/api/v1/users") {
                contentType(ContentType.Application.Json)
                setBody(
                  buildJson {
                    put("username", "")
                    put("email", "kodo@email.com")
                    put("passwd", "senha123")
                  },
                )
              }
            expect(response.status).toBe(HttpStatusCode.BadRequest)
            val response2 =
              client.post("http://localhost:8080/api/v1/users") {
                contentType(ContentType.Application.Json)
                setBody(
                  buildJson {
                    put("username", "kodokodokodokodokodokodokodokodo")
                    put("email", "kodo@email.com")
                    put("passwd", "senha123")
                  },
                )
              }
            expect(response2.status).toBe(HttpStatusCode.BadRequest)
          }

          describe("With invalid 'email'") {
            val response =
              client.post("http://localhost:8080/api/v1/users") {
                contentType(ContentType.Application.Json)
                setBody(
                  buildJson {
                    put("username", "kodo")
                    put("email", "")
                    put("passwd", "senha123")
                  },
                )
              }
            expect(response.status).toBe(HttpStatusCode.BadRequest)
          }

          describe("With invalid 'passwd'") {
            val response =
              client.post("http://localhost:8080/api/v1/users") {
                contentType(ContentType.Application.Json)
                setBody(
                  buildJson {
                    put("username", "kodo")
                    put("email", "kodo@email.com")
                    put("passwd", "")
                  },
                )
              }
            expect(response.status).toBe(HttpStatusCode.BadRequest)
          }

          describe("With duplicated 'username'") {
            val response =
              client.post("http://localhost:8080/api/v1/users") {
                contentType(ContentType.Application.Json)
                setBody(
                  buildJson {
                    put("username", "Kodo")
                    put("email", "kodo1@email.com")
                    put("passwd", "senha123")
                  },
                )
              }
            expect(response.status).toBe(HttpStatusCode.BadRequest)

            val body = expect(response.bodyAsText().parseJson()).toNotBeNull()
            expect(body.getString("name")).toBe("ValidationError")
            expect(body.getString("message")).toBe("O username informado já está sendo utilizado.")
            expect(body.getString("action")).toBe("Utilize outro username para realizar o cadastro.")
            expect(body.getInt("status_code")).toBe(HttpStatusCode.BadRequest.value)
          }

          describe("With duplicated 'email'") {
            val response =
              client.post("http://localhost:8080/api/v1/users") {
                contentType(ContentType.Application.Json)
                setBody(
                  buildJson {
                    put("username", "kodo1")
                    put("email", "Kodo@email.com")
                    put("passwd", "senha123")
                  },
                )
              }
            expect(response.status).toBe(HttpStatusCode.BadRequest)

            val body = expect(response.bodyAsText().parseJson()).toNotBeNull()
            expect(body.getString("name")).toBe("ValidationError")
            expect(body.getString("message")).toBe("O email informado já está sendo utilizado.")
            expect(body.getString("action")).toBe("Utilize outro email para realizar o cadastro.")
            expect(body.getInt("status_code")).toBe(HttpStatusCode.BadRequest.value)
          }
        }
      }
    }
}

private fun extractUuidVersion(uuid: String?) =
  Uuid.parseOrNull(uuid.orEmpty())?.toLongs { mostSignificantBits, _ ->
    (mostSignificantBits shr 12) and 0xF
  }

private fun parsePostgresTimestamp(timestamp: String?) =
  timestamp
    ?.replace(' ', 'T')
    ?.let {
      LocalDateTime.parseOrNull(it)
    }?.toInstant(TimeZone.UTC)
