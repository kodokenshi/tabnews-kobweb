package me.kodokenshi.tabnewskobweb.tests.integration.api.v1.migrations

import io.ktor.client.statement.*
import io.ktor.http.*
import me.kodokenshi.tabnewskobweb.json.Json
import me.kodokenshi.tabnewskobweb.tests.testContext
import kotlin.test.Test

class GetTest {
	
	@Test
	fun getMigrationStatus() = testContext("/api/v1/migrations", logPassedTests = true) {
		
		val response = fetch("http://localhost:8080/api/v1/migrations")
		val responseBody = Json.parseArray(response.bodyAsText())
		
		test("GET should return HttpStatusCode.OK") {
			expect(response.status).toBe(HttpStatusCode.OK)
		}
		
	}
	
}