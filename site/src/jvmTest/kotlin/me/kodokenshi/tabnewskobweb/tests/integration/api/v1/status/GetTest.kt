package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.status

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import me.kodokenshi.tabnewskobweb.json.jsonBuild
import me.kodokenshi.tabnewskobweb.json.toJsonOrNull
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.Orchestrator
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.client
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.testContext
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toBe
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toNotBeNull
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class GetTest {
  @Test
  suspend fun test() =
    testContext(this::class) {
      beforeAll {
        Orchestrator.waitForAllServices()
      }
      describe("GET /api/v1/status") {
        describe("Anonymous user") {
          describe("Retrieving current system status") {
            val response = client.get("http://localhost:8080/api/v1/status")
            expect(response.status).toBe(HttpStatusCode.OK)
							
            val body = expect(response.bodyAsText().toJsonOrNull()).toNotBeNull()
							
            val updatedAt = expect(body.getString("updated_at")).toNotBeNull()
            expect(Instant.parseOrNull(updatedAt).toString()).toBe(updatedAt)

            expect(body.getJson("dependencies")?.toString()).toBe(
              jsonBuild {
                "database" {
                  "version" eq "16.0"
                  "opened_connections" eq 1
                  "max_connections" eq 100
                }
              },
            )
          }
        }
      }
    }
}
