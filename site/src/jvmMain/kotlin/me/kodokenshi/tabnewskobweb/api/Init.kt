package me.kodokenshi.tabnewskobweb.api

import com.varabyte.kobweb.api.env.isDev
import com.varabyte.kobweb.api.init.InitApi
import com.varabyte.kobweb.api.init.InitApiContext
import me.kodokenshi.tabnewskobweb.database.Database
import me.kodokenshi.tabnewskobweb.database.migrations.Migrations

@InitApi
fun init(ctx: InitApiContext) {
	
	if (!ctx.env.isDev) return
	
	var count = 1
	println("🔴 Waiting Postgres accept new connections")
	while (Runtime.getRuntime().exec(arrayOf("docker", "exec", "postgres-dev", "pg_isready", "--host", "localhost")).waitFor() != 0) {
		Thread.sleep(50)
		println("🔴 Waiting Postgres accept new connections${".".repeat(count)}")
		count++
	}
	println("🟢 Postgres ready.")
	
	Migrations(
		driver = Database.POSTGRES_DRIVER,
		databaseURL = Database.POSTGRES_URL,
		databaseUser = Database.POSTGRES_USER,
		databasePassword = Database.POSTGRES_PASSWORD,
		migrationsPath = "../infra/migrations"
	).migrate()
	
}