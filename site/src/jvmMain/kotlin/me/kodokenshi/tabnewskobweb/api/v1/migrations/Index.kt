package me.kodokenshi.tabnewskobweb.api.v1.migrations

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.HttpMethod
import com.varabyte.kobweb.api.http.bodyOf
import io.ktor.http.HttpStatusCode
import me.kodokenshi.tabnewskobweb.database.Database
import me.kodokenshi.tabnewskobweb.database.migrations.Migrations
import me.kodokenshi.tabnewskobweb.json.errorJson

@Api("/v1/migrations")
fun status(ctx: ApiContext) {
  if (ctx.req.method !in setOf(HttpMethod.GET, HttpMethod.POST)) {
    ctx.res.body = bodyOf(errorJson("Method \"${ctx.req.method}\" not allowed"))
    ctx.res.status = HttpStatusCode.MethodNotAllowed.value
    return
  }
	
  val isDryRun = ctx.req.method == HttpMethod.GET
	
  val migrations = migrations()
  migrations.migrate(isDryRun)
	
  if (isDryRun) {
    ctx.res.body = bodyOf(migrations.findPendingMigrations().map { it.toInfoJson() }.toString())
  } else {
    val migratedMigrations = migrations.getMigratedMigrations()
		
    ctx.res.body = bodyOf(migratedMigrations.map { it.toInfoJson() }.toString())
    if (migratedMigrations.isNotEmpty()) ctx.res.status = HttpStatusCode.Created.value
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
