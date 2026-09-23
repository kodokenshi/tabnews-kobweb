package me.kodokenshi.tabnewskobweb.api.v1.migrations

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import io.ktor.http.HttpStatusCode
import me.kodokenshi.tabnewskobweb.database.migrations.defaultMigrations
import me.kodokenshi.tabnewskobweb.infra.createRouter
import me.kodokenshi.tabnewskobweb.infra.setResponse

@Api("/v1/migrations")
fun status(ctx: ApiContext) {
  createRouter(ctx)
    .routing {
      get {
        ctx.setResponse(
          response = defaultMigrations().findPendingMigrations().map { it.toInfoJson() },
        )
      }
      post {
        val migratedMigrations = defaultMigrations().migrate().getMigratedMigrations()
        ctx.setResponse(
          response = migratedMigrations.map { it.toInfoJson() },
          status = if (migratedMigrations.isEmpty()) HttpStatusCode.OK else HttpStatusCode.Created,
        )
      }
    }
}
