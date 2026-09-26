package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.users

import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import me.kodokenshi.tabnewskobweb.json.json
import me.kodokenshi.tabnewskobweb.json.jsonBuild
import me.kodokenshi.tabnewskobweb.json.toJsonOrNull
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.Orchestrator
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.testContext
import me.kodokenshi.tabnewskobweb.models.Password
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toBe
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toNotBeNull
import org.junit.jupiter.api.Test

class PostTest {
  @Test
  suspend fun test() =
    testContext(this::class) {
      beforeAll {
        Orchestrator.waitForAllServices()
        Orchestrator.clearDatabase()
        Orchestrator.runPendingMigrations()
      }
      describe("POST /api/v1/users") {
        describe("Anonymous user") {
          describe("With unique and valid data") {
            val response =
              Orchestrator.createUserURL(
                json {
                  "username" eq "validUser"
                  "email" eq "valid.user@email.com"
                  "passwd" eq "senha123"
                },
              )
	
            val body = expect(response.bodyAsText().toJsonOrNull()).toNotBeNull()
            expect(Orchestrator.extractUuidVersion(body.getString("id"))).toBe(4)
            expect(body.getString("username")).toBe("validUser")
            expect(body.getString("email")).toBe("valid.user@email.com")
            expect(Orchestrator.parsePostgresTimestamp(body.getString("created_at").orEmpty())).toNotBeNull()
            expect(Orchestrator.parsePostgresTimestamp(body.getString("updated_at").orEmpty())).toNotBeNull()
	
            val userInDatabase = Orchestrator.findUser("validUser")
            val storedPassword = userInDatabase.getString("passwd")
            expect(Password.compare("senha123", storedPassword)).toBe(true)
            expect(Password.compare("Senha123", storedPassword)).toBe(false)
            expect(Password.compare("SenhaErrada", storedPassword)).toBe(false)
          }
	
          describe("With invalid 'username'") {
            expect(
              Orchestrator
                .createUserURL(
                  json {
                    "username" eq ""
                  },
                ).status,
            ).toBe(HttpStatusCode.BadRequest)
            expect(
              Orchestrator
                .createUserURL(
                  json {
                    "username" eq "useruseruseruseruseruseruseruser"
                  },
                ).status,
            ).toBe(HttpStatusCode.BadRequest)
          }
	
          describe("With invalid 'email'") {
            expect(
              Orchestrator
                .createUserURL(
                  json {
                    "email" eq ""
                  },
                ).status,
            ).toBe(HttpStatusCode.BadRequest)
            expect(
              Orchestrator
                .createUserURL(
                  json {
                    "email" eq "@"
                  },
                ).status,
            ).toBe(HttpStatusCode.BadRequest)
          }
	
          describe("With invalid 'password'") {
            expect(
              Orchestrator
                .createUserURL(
                  json {
                    "passwd" eq ""
                  },
                ).status,
            ).toBe(HttpStatusCode.BadRequest)
          }
	
          describe("With duplicated 'username'") {
            expect(
              Orchestrator
                .createUserURL(
                  json {
                    "username" eq "duplicatedUser"
                  },
                ).status,
            ).toBe(HttpStatusCode.Created)
            val user2 =
              Orchestrator
                .createUserURL(
                  json {
                    "username" eq "duplicatedUser"
                  },
                )
            expect(user2.status).toBe(HttpStatusCode.BadRequest)

            expect(user2.bodyAsText()).toBe(
              jsonBuild {
                "name" eq "ValidationError"
                "message" eq "O username informado já está sendo utilizado."
                "action" eq "Utilize outro username para realizar esta operação."
                "status_code" eq HttpStatusCode.BadRequest.value
              },
            )
          }
	
          describe("With duplicated 'email'") {
            expect(
              Orchestrator
                .createUserURL(
                  json {
                    "email" eq "duplicated.email@email.com"
                  },
                ).status,
            ).toBe(HttpStatusCode.Created)
            val user2 =
              Orchestrator
                .createUserURL(
                  json {
                    "email" eq "duplicated.email@email.com"
                  },
                )
            expect(user2.status).toBe(HttpStatusCode.BadRequest)

            expect(user2.bodyAsText()).toBe(
              jsonBuild {
                "name" eq "ValidationError"
                "message" eq "O email informado já está sendo utilizado."
                "action" eq "Utilize outro email para realizar esta operação."
                "status_code" eq HttpStatusCode.BadRequest.value
              },
            )
          }
        }
      }
    }
}
