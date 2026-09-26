package me.kodokenshi.tabnewskobweb.api.v1.status

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import me.kodokenshi.tabnewskobweb.database.Database
import me.kodokenshi.tabnewskobweb.infra.createRouter
import me.kodokenshi.tabnewskobweb.infra.setResponse
import me.kodokenshi.tabnewskobweb.json.jsonBuild
import kotlin.time.Clock
import kotlin.time.Instant

@Api("/v1/status")
suspend fun status(ctx: ApiContext) {
  createRouter(ctx)
    .routing {
      get {
        val updatedAt = Instant.fromEpochMilliseconds(Clock.System.now().toEpochMilliseconds()).toString()
        val databaseVersion = Database.version()
        val databaseMaxConnections = Database.maxConnections()
        val databaseOpenedConnections = Database.openedConnections()

        ctx.setResponse(
          jsonBuild {
            "updated_at" eq updatedAt
            "dependencies" {
              "database" {
                "version" eq databaseVersion
                "opened_connections" eq databaseOpenedConnections
                "max_connections" eq databaseMaxConnections
              }
            }
          },
        )
      }
    }
}
