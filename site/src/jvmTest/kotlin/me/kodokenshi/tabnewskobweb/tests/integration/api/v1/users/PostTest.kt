package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.users

import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import me.kodokenshi.tabnewskobweb.json.parseJson
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.database.clearDatabase
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.database.waitForMigrations
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.testContext
import me.kodokenshi.tabnewskobweb.models.Password
import me.kodokenshi.tabnewskobweb.models.User
import me.kodokenshi.tabnewskobweb.tests.services.waitForAllServices
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toBe
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toNotBeNull
import org.junit.jupiter.api.Test

class PostTest {
  @Test
  suspend fun test() =
    testContext(this::class) {
      beforeAll {
        waitForAllServices()
        clearDatabase()
        waitForMigrations()
      }
      describe("POST /api/v1/users") {
        describe("Anonymous user") {
          describe("With unique and valid data") {
            val response =
              createUser(
                username = "validUser",
                email = "valid.user@email.com",
                password = "senha123",
              )
            expect(response.status).toBe(HttpStatusCode.Created)
	
            val body = expect(response.bodyAsText().parseJson()).toNotBeNull()
            expect(extractUuidVersion(body.getString("id"))).toBe(4)
            expect(body.getString("username")).toBe("validUser")
            expect(body.getString("email")).toBe("valid.user@email.com")
            expect(parsePostgresTimestamp(body.getString("created_at").orEmpty())).toNotBeNull()
            expect(parsePostgresTimestamp(body.getString("updated_at").orEmpty())).toNotBeNull()
	
            val userInDatabase = User.findOneByUsername("validUser")
            val storedPassword = userInDatabase.getString("passwd")
            expect(Password.compare("senha123", storedPassword)).toBe(true)
            expect(Password.compare("Senha123", storedPassword)).toBe(false)
            expect(Password.compare("SenhaErrada", storedPassword)).toBe(false)
          }
	
          describe("With invalid 'username'") {
            expect(
              createUser(
                username = "",
                email = "invalid.user@email.com",
                password = "senha123",
              ).status,
            ).toBe(HttpStatusCode.BadRequest)
            expect(
              createUser(
                username = "useruseruseruseruseruseruseruser",
                email = "invalid.useruseruseruseruseruseruseruser@email.com",
                password = "senha123",
              ).status,
            ).toBe(HttpStatusCode.BadRequest)
          }
	
          describe("With invalid 'email'") {
            expect(
              createUser(
                username = "invalidEmail",
                email = "",
                password = "senha123",
              ).status,
            ).toBe(HttpStatusCode.BadRequest)
            expect(
              createUser(
                username = "invalidEmail",
                email = "@",
                password = "senha123",
              ).status,
            ).toBe(HttpStatusCode.BadRequest)
          }
	
          describe("With invalid 'password'") {
            expect(
              createUser(
                username = "invalidPassword",
                email = "invalid.password@email.com",
                password = "",
              ).status,
            ).toBe(HttpStatusCode.BadRequest)
          }
	
          describe("With duplicated 'username'") {
            expect(
              createUser(
                username = "duplicatedUser",
                email = "duplicated.user1@email.com",
                password = "senha123",
              ).status,
            ).toBe(HttpStatusCode.Created)
            val response =
              createUser(
                username = "duplicatedUser",
                email = "duplicated.user2@email.com",
                password = "senha123",
              )
            expect(response.status).toBe(HttpStatusCode.BadRequest)
	
            val body = expect(response.bodyAsText().parseJson()).toNotBeNull()
            expect(body.getString("name")).toBe("ValidationError")
            expect(body.getString("message")).toBe("O username informado já está sendo utilizado.")
            expect(body.getString("action")).toBe("Utilize outro username para realizar esta operação.")
            expect(body.getInt("status_code")).toBe(HttpStatusCode.BadRequest.value)
          }
	
          describe("With duplicated 'email'") {
            expect(
              createUser(
                username = "duplicatedEmail1",
                email = "duplicated.email@email.com",
                password = "senha123",
              ).status,
            ).toBe(HttpStatusCode.Created)
            val response =
              createUser(
                username = "duplicatedEmail2",
                email = "duplicated.email@email.com",
                password = "senha123",
              )
            expect(response.status).toBe(HttpStatusCode.BadRequest)
	
            val body = expect(response.bodyAsText().parseJson()).toNotBeNull()
            expect(body.getString("name")).toBe("ValidationError")
            expect(body.getString("message")).toBe("O email informado já está sendo utilizado.")
            expect(body.getString("action")).toBe("Utilize outro email para realizar esta operação.")
            expect(body.getInt("status_code")).toBe(HttpStatusCode.BadRequest.value)
          }
        }
      }
    }
}
