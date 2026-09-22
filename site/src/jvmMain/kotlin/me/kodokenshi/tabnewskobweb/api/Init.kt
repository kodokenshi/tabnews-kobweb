package me.kodokenshi.tabnewskobweb.api

import com.varabyte.kobweb.api.init.InitApi
import com.varabyte.kobweb.api.init.InitApiContext
import me.kodokenshi.tabnewskobweb.database.Database
import me.kodokenshi.tabnewskobweb.database.migrations.Migrations
import me.kodokenshi.tabnewskobweb.infra.InternalServerError

@InitApi
fun init(ctx: InitApiContext) {
  if (System.getenv("KOBWEB_BUILD_TYPE") == "prod") return
	
  try {
    Migrations(
      driver = Database.POSTGRES_DRIVER,
      databaseURL = Database.POSTGRES_URL,
      databaseUser = Database.POSTGRES_USER,
      databasePassword = Database.POSTGRES_PASSWORD,
      migrationsPath = "infra/migrations",
    ).migrate()
  } catch (e: Throwable) {
    ctx.logger.error(InternalServerError(cause = e).toString())
  }
}
