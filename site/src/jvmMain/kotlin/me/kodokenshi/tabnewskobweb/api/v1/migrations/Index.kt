package me.kodokenshi.tabnewskobweb.api.v1.migrations

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import io.ktor.http.HttpStatusCode
import me.kodokenshi.tabnewskobweb.database.Database
import me.kodokenshi.tabnewskobweb.database.migrations.Migrations
import me.kodokenshi.tabnewskobweb.infra.createRouter
import me.kodokenshi.tabnewskobweb.infra.setResponse

@Api("/v1/migrations")
fun status(ctx: ApiContext) {
  createRouter(ctx)
    .routing {
      get {
        runMigrations(ctx, true)
      }
      post {
        runMigrations(ctx, false)
      }
    }
}

private fun runMigrations(
  ctx: ApiContext,
  dryRun: Boolean,
) {
  val migrations = migrations()
  migrations.migrate(dryRun)
	
  if (dryRun) {
    ctx.setResponse(
      response = migrations.findPendingMigrations().map { it.toInfoJson() },
    )
  } else {
    val migratedMigrations = migrations.getMigratedMigrations()
		
    ctx.setResponse(
      response = migratedMigrations.map { it.toInfoJson() }.toString(),
      status = if (migratedMigrations.isNotEmpty()) HttpStatusCode.Created else HttpStatusCode.OK,
    )
  }
}

private fun migrations() =
  Migrations(
    driver = Database.POSTGRES_DRIVER,
    databaseURL = Database.POSTGRES_URL,
    databaseUser = Database.POSTGRES_USER,
    databasePassword = Database.POSTGRES_PASSWORD,
    migrationsPath = "infra/migrations",
  )
