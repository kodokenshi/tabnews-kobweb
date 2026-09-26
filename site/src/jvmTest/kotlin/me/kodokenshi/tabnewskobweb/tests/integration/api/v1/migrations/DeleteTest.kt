package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.migrations

import io.ktor.client.request.delete
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import me.kodokenshi.tabnewskobweb.json.jsonBuild
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.Orchestrator
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.client
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.testContext
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toBe
import org.junit.jupiter.api.Test

class DeleteTest {
  @Test
  suspend fun test() =
    testContext(this::class) {
      beforeAll {
        Orchestrator.waitForAllServices()
        Orchestrator.clearDatabase()
      }
      describe("DELETE /api/v1/migrations") {
        describe("Anonymous user") {
          describe("Trying to delete migrations data") {
            val response = client.delete("http://localhost:8080/api/v1/migrations")
            expect(response.status).toBe(HttpStatusCode.MethodNotAllowed)
	
            expect(response.bodyAsText()).toBe(
              jsonBuild {
                "name" eq "MethodNotAllowedError"
                "message" eq "Método não permitido para este endpoint."
                "action" eq "Verifique se o método HTTP enviado é válido para este endpoint."
                "status_code" eq HttpStatusCode.MethodNotAllowed.value
              },
            )
          }
        }
      }
    }
}
