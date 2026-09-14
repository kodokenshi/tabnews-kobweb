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
class PostTest {
	
	@BeforeAll
	suspend fun beforeAll() = waitForAllServices("POST /api/v1/migrations")
	
	@Test
	suspend fun test() = testContext("POST /api/v1/migrations") {
		
		test("clear database").expect(clearDatabase()).toBe(true)
		
		checkMigrations(false, HttpStatusCode.Created)
		checkMigrations(true, HttpStatusCode.OK)
		
	}
	
	private suspend fun TestContext.checkMigrations(
		expectEmpty: Boolean,
		expectCode: HttpStatusCode
	) {
		
		val response = fetch("http://localhost:8080/api/v1/migrations", TestContext.FetchMethod.POST)
		val responseBody = response.bodyAsText()
		
		test("POST should return $expectCode").expect(response.status).toBe(expectCode)
		
		val migrationList = test("responseBody should be List<Json>").expect(Json.parseList(responseBody)).toBePresent()
		test("migrationList should be empty = $expectEmpty").expect(migrationList.isEmpty()).toBe(expectEmpty)
		
	}
	
}