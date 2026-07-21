package me.kodokenshi.tabnewskobweb.api.v1.status

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.bodyOf
import me.kodokenshi.tabnewskobweb.database.Database
import me.kodokenshi.tabnewskobweb.json.buildJson
import java.time.Instant
import java.time.temporal.ChronoUnit

@Api("/v1/status")
suspend fun status(ctx: ApiContext) {
	
	try {
		
		val updatedAt = Instant.now().truncatedTo(ChronoUnit.MILLIS).toString()
		val databaseVersion = Database.version()
		val databaseMaxConnections = Database.maxConnections()
		val databaseOpenedConnections = Database.openedConnections()
		
		ctx.res.body =
			bodyOf(
				buildJson {
					put("updated_at", updatedAt)
					putNested("dependencies.database") {
						put("version", databaseVersion)
						put("max_connections", databaseMaxConnections)
						put("opened_connections", databaseOpenedConnections)
					}
				},
				"application/json"
			)
		
	} catch (t: Throwable) {
		
		val msg = "Error: ${t.message}\nStackTrace:\n${t.stackTraceToString()}"
		
		ctx.res.body = bodyOf(msg)
		ctx.res.status = 500
		
		t.printStackTrace()
		
		throw t
		
	}
	
}
