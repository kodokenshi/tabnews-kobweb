package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.migrations

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import me.kodokenshi.tabnewskobweb.json.parseJsonList
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.database.clearDatabase
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.testContext
import me.kodokenshi.tabnewskobweb.tests.services.client
import me.kodokenshi.tabnewskobweb.tests.services.waitForAllServices
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toBe
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toNotBeEmpty
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toNotBeNull
import org.junit.jupiter.api.Test

class GetTest {
  @Test
  suspend fun test() =
    testContext(this::class) {
      beforeAll {
        waitForAllServices()
        clearDatabase()
      }
      describe("GET /api/v1/migrations") {
        describe("Anonymous user") {
          repeat(2) {
            describe("Retrieving current migration status $it") {
              val response = client.get("http://localhost:8080/api/v1/migrations")
              expect(response.status).toBe(HttpStatusCode.OK)
								
              val body = expect(response.bodyAsText().parseJsonList()).toNotBeNull()
              expect(body).toNotBeEmpty()
            }
          }
        }
      }
    }
}
