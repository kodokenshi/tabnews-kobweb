package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.users.username

import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import me.kodokenshi.tabnewskobweb.json.json
import me.kodokenshi.tabnewskobweb.json.jsonBuild
import me.kodokenshi.tabnewskobweb.json.toJsonOrNull
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.Orchestrator
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.testContext
import me.kodokenshi.tabnewskobweb.models.Password
import me.kodokenshi.tabnewskobweb.models.User
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toBe
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toNotBeNull
import org.junit.jupiter.api.Test

class PatchTest {
  @Test
  suspend fun test() =
    testContext(this::class) {
      beforeAll {
        Orchestrator.waitForAllServices()
        Orchestrator.clearDatabase()
        Orchestrator.runPendingMigrations()
      }
      describe("PATCH /api/v1/users/[username]") {
        describe("Anonymous user") {
          describe("With nonexistent 'username'") {
            val response = Orchestrator.updateUserURL("nonexistentuser")
            expect(response.status).toBe(HttpStatusCode.NotFound)

            expect(response.bodyAsText()).toBe(
              jsonBuild {
                "name" eq "NotFoundError"
                "message" eq "O username informado não foi encontrado."
                "action" eq "Verifique se o username está correto."
                "status_code" eq HttpStatusCode.NotFound.value
              },
            )
          }
					
          describe("With duplicated 'username'") {
            Orchestrator.createUser(
              json {
                "username" eq "duplicatedUser1"
              },
            )
            Orchestrator.createUser(
              json {
                "username" eq "duplicatedUser2"
              },
            )

            val response =
              Orchestrator.updateUserURL(
                username = "duplicatedUser2",
                values =
                  json {
                    "username" eq "duplicatedUser1"
                  },
              )
            expect(response.status).toBe(HttpStatusCode.BadRequest)

            expect(response.bodyAsText()).toBe(
              jsonBuild {
                "name" eq "ValidationError"
                "message" eq "O username informado já está sendo utilizado."
                "action" eq "Utilize outro username para realizar esta operação."
                "status_code" eq HttpStatusCode.BadRequest.value
              },
            )
          }

          describe("With duplicated 'email'") {
            Orchestrator.createUser(
              json {
                "email" eq "duplicated.email1@email.com"
              },
            )
            val user2 =
              expect(
                Orchestrator.createUser(
                  json {
                    "email" eq "duplicated.email2@email.com"
                  },
                ),
              ).toNotBeNull()

            val response =
              Orchestrator.updateUserURL(
                username = user2.getString("username")!!,
                values =
                  json {
                    "email" eq "duplicated.email1@email.com"
                  },
              )
            expect(response.status).toBe(HttpStatusCode.BadRequest)

            expect(response.bodyAsText()).toBe(
              jsonBuild {
                "name" eq "ValidationError"
                "message" eq "O email informado já está sendo utilizado."
                "action" eq "Utilize outro email para realizar esta operação."
                "status_code" eq HttpStatusCode.BadRequest.value
              },
            )
          }

          describe("With unique 'username'") {
            Orchestrator.createUser(
              json {
                "username" eq "uniqueUser1"
                "email" eq "unique.user1@email.com"
              },
            )

            val response =
              Orchestrator.updateUserURL(
                username = "uniqueUser1",
                values =
                  json {
                    "username" eq "uniqueUser2"
                  },
              )
            expect(response.status).toBe(HttpStatusCode.OK)

            val body = expect(response.bodyAsText().toJsonOrNull()).toNotBeNull()
            expect(Orchestrator.extractUuidVersion(body.getString("id"))).toBe(4)
            expect(body.getString("username")).toBe("uniqueUser2")
            expect(body.getString("email")).toBe("unique.user1@email.com")
            val createdAt =
              expect(
                Orchestrator.parsePostgresTimestamp(
                  body.getString("created_at").orEmpty(),
                ),
              ).toNotBeNull()
            val updatedAt =
              expect(
                Orchestrator.parsePostgresTimestamp(
                  body.getString("updated_at").orEmpty(),
                ),
              ).toNotBeNull()
            expect(updatedAt > createdAt).toBe(true)
          }

          describe("With unique 'email'") {
            Orchestrator.createUser(
              json {
                "username" eq "uniqueEmail1"
                "email" eq "unique.email1@email.com"
              },
            )

            val response =
              Orchestrator.updateUserURL(
                username = "uniqueEmail1",
                values =
                  json {
                    "email" eq "unique.email2@email.com"
                  },
              )
            expect(response.status).toBe(HttpStatusCode.OK)

            val body = expect(response.bodyAsText().toJsonOrNull()).toNotBeNull()
            expect(Orchestrator.extractUuidVersion(body.getString("id"))).toBe(4)
            expect(body.getString("username")).toBe("uniqueEmail1")
            expect(body.getString("email")).toBe("unique.email2@email.com")
            val createdAt =
              expect(
                Orchestrator.parsePostgresTimestamp(
                  body.getString("created_at").orEmpty(),
                ),
              ).toNotBeNull()
            val updatedAt =
              expect(
                Orchestrator.parsePostgresTimestamp(
                  body.getString("updated_at").orEmpty(),
                ),
              ).toNotBeNull()
            expect(updatedAt > createdAt).toBe(true)
          }

          describe("With new 'password'") {
            Orchestrator.createUser(
              json {
                "username" eq "newPassword1"
                "email" eq "new.password1@email.com"
                "passwd" eq "senha123"
              },
            )

            val response =
              Orchestrator.updateUserURL(
                username = "newPassword1",
                values =
                  json {
                    "passwd" eq "123senha"
                  },
              )
            expect(response.status).toBe(HttpStatusCode.OK)

            val updatedUser = expect(response.bodyAsText().toJsonOrNull()).toNotBeNull()
            expect(Orchestrator.extractUuidVersion(updatedUser.getString("id"))).toBe(4)
            expect(updatedUser.getString("username")).toBe("newPassword1")
            expect(updatedUser.getString("email")).toBe("new.password1@email.com")
            val createdAt =
              expect(
                Orchestrator.parsePostgresTimestamp(updatedUser.getString("created_at").orEmpty()),
              ).toNotBeNull()
            val updatedAt =
              expect(
                Orchestrator.parsePostgresTimestamp(updatedUser.getString("updated_at").orEmpty()),
              ).toNotBeNull()
            expect(updatedAt > createdAt).toBe(true)

            val updatedUserInDatabase = User.findOneByUsername("newPassword1")
            val newPassword = updatedUserInDatabase.getString("passwd")
            expect(Password.compare("123senha", newPassword)).toBe(true)
            expect(Password.compare("123senhA", newPassword)).toBe(false)
            expect(Password.compare("SenhaErrada", newPassword)).toBe(false)
          }
        }
      }
    }
}
