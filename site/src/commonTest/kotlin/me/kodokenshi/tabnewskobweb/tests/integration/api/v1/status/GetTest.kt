package me.kodokenshi.tabnewskobweb.tests.integration.api.v1.status

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import me.kodokenshi.tabnewskobweb.tests.runTest
import kotlin.test.Test

class GetTest {
	
	@Test
	fun getApiStatus() = runTest("GET to /api/v1/status should return 200") {
		
		HttpClient().use {
			expect(it.get("http://localhost:8080/api/v1/status").status).toBe(HttpStatusCode.OK)
		}
		
	}
	
}