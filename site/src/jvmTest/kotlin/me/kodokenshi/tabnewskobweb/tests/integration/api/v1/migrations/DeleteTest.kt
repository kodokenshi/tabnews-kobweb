package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.migrations

import io.ktor.client.request.delete
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import me.kodokenshi.tabnewskobweb.json.parseJson
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.TestConfig
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.database.clearDatabase
import me.kodokenshi.tabnewskobweb.tests.services.client
import me.kodokenshi.tabnewskobweb.tests.services.waitForAllServices
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toBe
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toNotBeNull
import me.kodokenshi.tabnewskobweb.tests.test.testContext
import org.junit.jupiter.api.Test

class DeleteTest {
  @Test
  suspend fun test() =
    testContext(this::class, TestConfig.IS_VERBOSE) {
      beforeAll {
        waitForAllServices()
        clearDatabase()
      }
      describe("DELETE /api/v1/migrations") {
        describe("Anonymous user") {
          describe("Trying to delete migrations data") {
            val response = client.delete("http://localhost:8080/api/v1/migrations")
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
