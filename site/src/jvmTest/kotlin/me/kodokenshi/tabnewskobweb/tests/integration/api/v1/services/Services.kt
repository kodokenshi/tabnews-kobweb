package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.services

import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import me.kodokenshi.tabnewskobweb.tests.TestContext
import me.kodokenshi.tabnewskobweb.tests.testContext
import kotlin.time.Duration.Companion.milliseconds

suspend fun waitForAllServices(
  context: String,
  timeoutInMillis: Long = TestContext.defaultTestTimeoutInMillis,
  maxRetries: Int = 100,
) = testContext("$context - waitForAllServices", timeoutInMillis, hide = true) {
  var retries = 0

  suspend fun fetchStatusPage() =
    try {
      HttpClient()
        .get("http://localhost:8080/api/v1/status") {
          timeout { connectTimeoutMillis = 1000 }
        }.status == HttpStatusCode.OK
    } catch (_: Exception) {
      delay(500.milliseconds)
      false
    }
	
  var fetch = false
  while (!fetchStatusPage().also { fetch = it } && retries <= maxRetries) retries++
	
  if (!fetch) throw Exception("Could not fetch status page.")
}
