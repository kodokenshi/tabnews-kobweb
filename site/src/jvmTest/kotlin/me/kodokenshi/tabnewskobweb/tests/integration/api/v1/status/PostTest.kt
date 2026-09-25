package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.status

import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import me.kodokenshi.tabnewskobweb.json.parseJson
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.testContext
import me.kodokenshi.tabnewskobweb.tests.services.client
import me.kodokenshi.tabnewskobweb.tests.services.waitForAllServices
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toBe
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toNotBeNull
import org.junit.jupiter.api.Test

class PostTest {
  @Test
  suspend fun test() =
    testContext(this::class) {
      beforeAll {
        waitForAllServices()
      }
      describe("POST /api/v1/status") {
        describe("Anonymous user") {
          describe("Retrieving current system status") {
            val response = client.post("http://localhost:8080/api/v1/status")
            expect(response.status).toBe(HttpStatusCode.MethodNotAllowed)
	
            val body = expect(response.bodyAsText().parseJson()).toNotBeNull()
            expect(body.getString("name")).toBe("MethodNotAllowedError")
            expect(body.getString("message")).toBe("Método não permitido para este endpoint.")
            expect(body.getString("action")).toBe("Verifique se o método HTTP enviado é válido para este endpoint.")
            expect(body.getInt("status_code")).toBe(HttpStatusCode.MethodNotAllowed.value)
          }
        }
      }
    }
}
