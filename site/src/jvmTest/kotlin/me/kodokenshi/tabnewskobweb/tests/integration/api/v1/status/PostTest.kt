package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.status

import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import me.kodokenshi.tabnewskobweb.json.jsonBuild
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.Orchestrator
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.client
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.testContext
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toBe
import org.junit.jupiter.api.Test

class PostTest {
  @Test
  suspend fun test() =
    testContext(this::class) {
      beforeAll {
        Orchestrator.waitForAllServices()
      }
      describe("POST /api/v1/status") {
        describe("Anonymous user") {
          describe("Retrieving current system status") {
            val response = client.post("http://localhost:8080/api/v1/status")
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
