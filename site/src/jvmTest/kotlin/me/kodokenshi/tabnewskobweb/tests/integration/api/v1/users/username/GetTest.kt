package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.users.username

import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import me.kodokenshi.tabnewskobweb.json.json
import me.kodokenshi.tabnewskobweb.json.jsonBuild
import me.kodokenshi.tabnewskobweb.json.toJsonOrNull
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.Orchestrator
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.testContext
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toBe
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toNotBeNull
import org.junit.jupiter.api.Test

class GetTest {
  @Test
  suspend fun test() =
    testContext(this::class) {
      beforeAll {
        Orchestrator.waitForAllServices()
        Orchestrator.clearDatabase()
        Orchestrator.runPendingMigrations()
      }
      describe("GET /api/v1/users/[username]") {
        describe("Anonymous user") {
          describe("With exact case match") {
            Orchestrator.createUser(
              json {
                "username" eq "exactUser"
                "email" eq "exact.user@email.com"
                "passwd" eq "senha123"
              },
            )
	
            val response = Orchestrator.findUserURL("exactUser")
            expect(response.status).toBe(HttpStatusCode.OK)
	
            val body = expect(response.bodyAsText().toJsonOrNull()).toNotBeNull()
            expect(Orchestrator.extractUuidVersion(body.getString("id"))).toBe(4)
            expect(body.getString("username")).toBe("exactUser")
            expect(body.getString("email")).toBe("exact.user@email.com")
            expect(Orchestrator.parsePostgresTimestamp(body.getString("created_at").orEmpty())).toNotBeNull()
            expect(Orchestrator.parsePostgresTimestamp(body.getString("updated_at").orEmpty())).toNotBeNull()
          }
	
          describe("With case mismatch") {
            Orchestrator.createUser(
              json {
                "username" eq "mismatchUser"
                "email" eq "mismatch.user@email.com"
                "passwd" eq "senha123"
              },
            )
						
            val response = Orchestrator.findUserURL("MisMATCHusER")
            expect(response.status).toBe(HttpStatusCode.OK)
	
            val body = expect(response.bodyAsText().toJsonOrNull()).toNotBeNull()
            expect(Orchestrator.extractUuidVersion(body.getString("id"))).toBe(4)
            expect(body.getString("username")).toBe("mismatchUser")
            expect(body.getString("email")).toBe("mismatch.user@email.com")
            expect(Orchestrator.parsePostgresTimestamp(body.getString("created_at").orEmpty())).toNotBeNull()
            expect(Orchestrator.parsePostgresTimestamp(body.getString("updated_at").orEmpty())).toNotBeNull()
          }
	
          describe("With nonexistent 'username'") {
            val response = Orchestrator.findUserURL("nonexistentuser")
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
	
          describe("With invalid 'username'") {
            val response = Orchestrator.findUserURL("useruseruseruseruseruseruseruser")
            expect(response.status).toBe(HttpStatusCode.BadRequest)
          }
        }
      }
    }
}
