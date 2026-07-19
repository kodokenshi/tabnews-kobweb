package me.kodokenshi.tabnewskobweb.tests.integration.api.v1.status

import io.ktor.http.*
import me.kodokenshi.tabnewskobweb.tests.runTest
import kotlin.test.Test

class GetTest {
	
	@Test
	fun getApiStatus() = runTest("GET to /api/v1/status should return HttpStatusCode.OK") {
		
		expect(fetch("http://localhost:8080/api/v1/status").status).toBe(HttpStatusCode.OK)
		
	}
	
}