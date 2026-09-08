package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.status

import io.ktor.client.statement.*
import io.ktor.http.*
import me.kodokenshi.tabnewskobweb.json.Json
import me.kodokenshi.tabnewskobweb.tests.testContext
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class GetTest {
	
	@Test
	fun test() = testContext("/api/v1/status", logPassedTests = false) {
		
		val response = fetch("http://localhost:8080/api/v1/status")
		val responseBody = response.bodyAsText()
		
		test("GET should return HttpStatusCode.OK")
			.expect(response.status).toBe(HttpStatusCode.OK)
		
		val body = test("responseBody should be Json")
			.expect(Json.parse(responseBody)).toBePresent()
		
		val updatedAt = test("\"updated_at\" should be present")
			.expect(body.getString("updated_at")).toBePresent()
		test("\"updated_at\" should be ISOString")
			.expect(Instant.parseOrNull(updatedAt).toString()).toBe(updatedAt)
		
		test("\"dependencies.database.version\" should be 16.0")
			.expect(body.getNestedString("dependencies.database.version")).toBe("16.0")
		test("\"dependencies.database.max_connections\" should be 100")
			.expect(body.getNestedInt("dependencies.database.max_connections")).toBe(100)
		test("\"dependencies.database.opened_connections\" should be 1")
			.expect(body.getNestedInt("dependencies.database.opened_connections")).toBe(1)
		
	}
	
}