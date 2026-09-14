package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.migrations

import io.ktor.client.statement.*
import io.ktor.http.*
import me.kodokenshi.tabnewskobweb.json.Json
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.database.clearDatabase
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.services.waitForAllServices
import me.kodokenshi.tabnewskobweb.tests.TestContext
import me.kodokenshi.tabnewskobweb.tests.testContext
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GetTest {
	
	@BeforeAll
	suspend fun beforeAll() = waitForAllServices("GET /api/v1/migrations")
	
	@Test
	suspend fun test() = testContext("GET /api/v1/migrations") {
		
		test("clear database").expect(clearDatabase()).toBe(true)
		
		checkMigrations(false)
		checkMigrations(false)
		
	}
	
	private suspend fun TestContext.checkMigrations(expectEmpty: Boolean) {
		
		val response = fetch("http://localhost:8080/api/v1/migrations")
		val responseBody = response.bodyAsText()
		
		test("GET should return ${HttpStatusCode.OK}").expect(response.status).toBe(HttpStatusCode.OK)
		
		val migrationList = test("responseBody should be List<Json>").expect(Json.parseList(responseBody)).toBePresent()
		test("migrationList should be empty = $expectEmpty").expect(migrationList.isEmpty()).toBe(expectEmpty)
		
	}
	
}