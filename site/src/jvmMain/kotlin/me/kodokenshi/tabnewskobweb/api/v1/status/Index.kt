package me.kodokenshi.tabnewskobweb.api.v1.status

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.bodyOf
import io.ktor.http.HttpStatusCode
import me.kodokenshi.tabnewskobweb.database.Database
import me.kodokenshi.tabnewskobweb.infra.InternalServerError
import me.kodokenshi.tabnewskobweb.json.buildJson
import kotlin.time.Clock
import kotlin.time.Instant

@Api("/v1/status")
fun status(ctx: ApiContext) {
  try {
    val updatedAt = Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds()).toString()
    val databaseVersion = Database.version()
    val databaseMaxConnections = Database.maxConnections()
    val databaseOpenedConnections = Database.openedConnections()
		
    ctx.res.body =
      bodyOf(
        buildJson {
          put("updated_at", updatedAt)
          putNested("dependencies.database") {
            put("version", databaseVersion)
            put("opened_connections", databaseOpenedConnections)
            put("max_connections", databaseMaxConnections)
          }
        },
        "application/json",
      )
  } catch (e: Throwable) {
    ctx.res.body = bodyOf(InternalServerError(cause = e).toString())
    ctx.res.status = HttpStatusCode.InternalServerError.value
  }
}
