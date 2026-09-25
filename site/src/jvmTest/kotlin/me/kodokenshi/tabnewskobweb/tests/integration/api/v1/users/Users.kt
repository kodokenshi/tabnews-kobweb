package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.users

import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.parseOrNull
import kotlinx.datetime.toInstant
import me.kodokenshi.tabnewskobweb.json.buildJson
import me.kodokenshi.tabnewskobweb.tests.services.client
import kotlin.uuid.Uuid

suspend fun findUser(username: String) = client.get("http://localhost:8080/api/v1/users/$username")

suspend fun createUser(
  username: String? = null,
  email: String? = null,
  password: String? = null,
) = client.post("http://localhost:8080/api/v1/users") {
  contentType(ContentType.Application.Json)
  setBody(
    buildJson {
      username?.let { put("username", it) }
      email?.let { put("email", it) }
      password?.let { put("passwd", it) }
    },
  )
}

suspend fun updateUser(
  currentUsername: String,
  username: String? = null,
  email: String? = null,
  password: String? = null,
) = client.patch("http://localhost:8080/api/v1/users/$currentUsername") {
  contentType(ContentType.Application.Json)
  setBody(
    buildJson {
      username?.let { put("username", it) }
      email?.let { put("email", it) }
      password?.let { put("passwd", it) }
    },
  )
}

fun extractUuidVersion(uuid: String?) =
  Uuid.parseOrNull(uuid.orEmpty())?.toLongs { mostSignificantBits, _ ->
    (mostSignificantBits shr 12) and 0xF
  }

fun parsePostgresTimestamp(timestamp: String?) =
  timestamp
    ?.replace(' ', 'T')
    ?.let {
      LocalDateTime.parseOrNull(it)
    }?.toInstant(TimeZone.UTC)
