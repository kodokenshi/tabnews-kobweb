package me.kodokenshi.tabnewskobweb.tests.services

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

val client by lazy {
  HttpClient {
    install(HttpTimeout) {
      connectTimeoutMillis = 1000
    }
  }
}

suspend fun waitForAllServices(maxRetries: Int = 100) =
  check(
    withTimeoutOrNull(1.minutes) {
      var retries = 0
      var fetch = false
      while (!fetch && retries <= maxRetries) {
        fetch = fetchStatusPage()
        if (!fetch) {
          delay(500.milliseconds)
          retries++
        }
      }
      fetch
    } == true,
  ) { "Could not fetch status page" }

private suspend fun fetchStatusPage() =
  try {
    client.get("http://localhost:8080/api/v1/status").status == HttpStatusCode.OK
  } catch (_: Throwable) {
    false
  }
