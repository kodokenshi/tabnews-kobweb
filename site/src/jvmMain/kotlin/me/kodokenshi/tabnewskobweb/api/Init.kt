package me.kodokenshi.tabnewskobweb.api

import com.varabyte.kobweb.api.init.InitApi
import com.varabyte.kobweb.api.init.InitApiContext
import me.kodokenshi.tabnewskobweb.database.Database
import me.kodokenshi.tabnewskobweb.database.migrations.Migrations

@InitApi
fun init(ignored: InitApiContext) {
  if (System.getenv("KOBWEB_BUILD_TYPE") == "prod") return
	
  Migrations(
    driver = Database.POSTGRES_DRIVER,
    databaseURL = Database.POSTGRES_URL,
    databaseUser = Database.POSTGRES_USER,
    databasePassword = Database.POSTGRES_PASSWORD,
    migrationsPath = "infra/migrations",
  ).migrate()
}
