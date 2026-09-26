package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.migrations

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import me.kodokenshi.tabnewskobweb.json.toJsonArrayOrNull
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.Orchestrator
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.client
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.testContext
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
      }
      describe("POST /api/v1/migrations") {
        describe("Anonymous user") {
          repeat(2) {
            describe(if (it == 0) "Running pending migrations" else "Retrieving current migrations status") {
              val response =
                if (it == 0) {
                  client.post("http://localhost:8080/api/v1/migrations")
                } else {
                  client.get("http://localhost:8080/api/v1/migrations")
                }
              expect(response.status).toBe(
                if (it == 0) {
                  HttpStatusCode.Created
                } else {
                  HttpStatusCode.OK
                },
              )
								
              val body = expect(response.bodyAsText().toJsonArrayOrNull()).toNotBeNull()
              expect(body.isEmpty()).toBe(it != 0)
            }
          }
        }
      }
    }
}
