package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.status

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import me.kodokenshi.tabnewskobweb.json.parseJson
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.TestConfig
import me.kodokenshi.tabnewskobweb.tests.services.client
import me.kodokenshi.tabnewskobweb.tests.services.waitForAllServices
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toBe
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toNotBeNull
import me.kodokenshi.tabnewskobweb.tests.test.testContext
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class GetTest {
  @Test
  suspend fun test() =
    testContext(this::class, TestConfig.IS_VERBOSE) {
      beforeAll {
        waitForAllServices()
      }
      describe("GET /api/v1/status") {
        describe("Anonymous user") {
          describe("Retrieving current system status") {
            val response = client.get("http://localhost:8080/api/v1/status")
            expect(response.status).toBe(HttpStatusCode.OK)
						
            val body = expect(response.bodyAsText().parseJson()).toNotBeNull()
						
            val updatedAt = expect(body.getString("updated_at")).toNotBeNull()
            expect(Instant.parseOrNull(updatedAt).toString()).toBe(updatedAt)
						
            expect(body.getNestedString("dependencies.database.version")).toBe("16.0")
            expect(body.getNestedInt("dependencies.database.opened_connections")).toBe(1)
            expect(body.getNestedInt("dependencies.database.max_connections")).toBe(100)
          }
        }
      }
    }
}
