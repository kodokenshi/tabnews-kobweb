package me.kodokenshi.tabnewskobweb.api.v1.status

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.bodyOf
import me.kodokenshi.tabnewskobweb.database.Database
import me.kodokenshi.tabnewskobweb.json.buildJson
import kotlin.time.Clock
import kotlin.time.Instant

@Api("/v1/status")
fun status(ctx: ApiContext) {
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
}
