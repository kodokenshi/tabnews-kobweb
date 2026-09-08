package me.kodokenshi.tabnewskobweb.api.v1.migrations

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.HttpMethod
import com.varabyte.kobweb.api.http.bodyOf
import io.ktor.http.*
import me.kodokenshi.tabnewskobweb.database.Database
import me.kodokenshi.tabnewskobweb.database.migrations.Migrations

@Api("/v1/migrations")
suspend fun status(ctx: ApiContext) {
	
	try {
	
		when (ctx.req.method) {
			HttpMethod.GET -> {
				
				val migrations = migrations()
				migrations.migrate(true)
				
				ctx.res.body = bodyOf(migrations.findPendingMigrations().map { it.toInfoJson() }.toString())
				
			}
			HttpMethod.POST -> {
				
				val migrations = migrations()
				migrations.migrate()
				
				val migratedMigrations = migrations.getMigratedMigrations()
				
				ctx.res.body = bodyOf(migratedMigrations.map { it.toInfoJson() }.toString())
				if (migratedMigrations.isNotEmpty()) ctx.res.status = HttpStatusCode.Created.value
				
			}
			else -> ctx.res.status = HttpStatusCode.MethodNotAllowed.value
			
		}
		
	} catch (t: Throwable) {
		
		t.printStackTrace()
		throw t
		
	}
	
}
private fun migrations() = Migrations(
	driver = Database.POSTGRES_DRIVER,
	databaseURL = Database.POSTGRES_URL,
	databaseUser = Database.POSTGRES_USER,
	databasePassword = Database.POSTGRES_PASSWORD,
	migrationsPath = "../infra/migrations"
)