package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.users.username

import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import me.kodokenshi.tabnewskobweb.json.parseJson
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.database.clearDatabase
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.database.waitForMigrations
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.testContext
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.users.createUser
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.users.extractUuidVersion
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.users.findUser
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.users.parsePostgresTimestamp
import me.kodokenshi.tabnewskobweb.tests.services.waitForAllServices
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toBe
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toNotBeNull
import org.junit.jupiter.api.Test

class GetTest {
  @Test
  suspend fun test() =
    testContext(this::class) {
      beforeAll {
        waitForAllServices()
        clearDatabase()
        waitForMigrations()
      }
      describe("GET /api/v1/users/[username]") {
        describe("Anonymous user") {
          describe("With exact case match") {
            expect(
              createUser(
                username = "exactUser",
                email = "exact.user@email.com",
                password = "senha123",
              ).status,
            ).toBe(HttpStatusCode.Created)
	
            val response = findUser("exactUser")
            expect(response.status).toBe(HttpStatusCode.OK)
	
            val body = expect(response.bodyAsText().parseJson()).toNotBeNull()
            expect(extractUuidVersion(body.getString("id"))).toBe(4)
            expect(body.getString("username")).toBe("exactUser")
            expect(body.getString("email")).toBe("exact.user@email.com")
            expect(parsePostgresTimestamp(body.getString("created_at").orEmpty())).toNotBeNull()
            expect(parsePostgresTimestamp(body.getString("updated_at").orEmpty())).toNotBeNull()
          }
	
          describe("With case mismatch") {
            expect(
              createUser(
                username = "mismatchUser",
                email = "mismatch.user@email.com",
                password = "senha123",
              ).status,
            ).toBe(HttpStatusCode.Created)
						
            val response = findUser("MisMATCHusER")
            expect(response.status).toBe(HttpStatusCode.OK)
	
            val body = expect(response.bodyAsText().parseJson()).toNotBeNull()
            expect(extractUuidVersion(body.getString("id"))).toBe(4)
            expect(body.getString("username")).toBe("mismatchUser")
            expect(body.getString("email")).toBe("mismatch.user@email.com")
            expect(parsePostgresTimestamp(body.getString("created_at").orEmpty())).toNotBeNull()
            expect(parsePostgresTimestamp(body.getString("updated_at").orEmpty())).toNotBeNull()
          }
	
          describe("With nonexistent 'username'") {
            val response = findUser("nonexistentuser")
            expect(response.status).toBe(HttpStatusCode.NotFound)
	
            val body = expect(response.bodyAsText().parseJson()).toNotBeNull()
            expect(body.getString("name")).toBe("NotFoundError")
            expect(body.getString("message")).toBe("O username informado não foi encontrado.")
            expect(body.getString("action")).toBe("Verifique se o username está correto.")
            expect(body.getInt("status_code")).toBe(HttpStatusCode.NotFound.value)
          }
	
          describe("With invalid 'username'") {
            val response = findUser("useruseruseruseruseruseruseruser")
            expect(response.status).toBe(HttpStatusCode.BadRequest)
          }
        }
      }
    }
}
