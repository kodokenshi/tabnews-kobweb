package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.users.username

import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import me.kodokenshi.tabnewskobweb.json.parseJson
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.database.clearDatabase
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.database.waitForMigrations
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.testContext
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.users.createUser
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.users.extractUuidVersion
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.users.parsePostgresTimestamp
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.users.updateUser
import me.kodokenshi.tabnewskobweb.models.Password
import me.kodokenshi.tabnewskobweb.models.User
import me.kodokenshi.tabnewskobweb.tests.services.waitForAllServices
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toBe
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toNotBeNull
import org.junit.jupiter.api.Test

class PatchTest {
  @Test
  suspend fun test() =
    testContext(this::class) {
      beforeAll {
        waitForAllServices()
        clearDatabase()
        waitForMigrations()
      }
      describe("PATCH /api/v1/users/[username]") {
        describe("Anonymous user") {
          describe("With unique 'username'") {
            expect(
              createUser(
                username = "uniqueUser1",
                email = "unique.user1@email.com",
                password = "senha123",
              ).status,
            ).toBe(HttpStatusCode.Created)

            val response =
              updateUser(
                currentUsername = "uniqueUser1",
                username = "uniqueUser2",
              )
            expect(response.status).toBe(HttpStatusCode.OK)

            val body = expect(response.bodyAsText().parseJson()).toNotBeNull()
            expect(extractUuidVersion(body.getString("id"))).toBe(4)
            expect(body.getString("username")).toBe("uniqueUser2")
            expect(body.getString("email")).toBe("unique.user1@email.com")
            val createdAt = expect(parsePostgresTimestamp(body.getString("created_at").orEmpty())).toNotBeNull()
            val updatedAt = expect(parsePostgresTimestamp(body.getString("updated_at").orEmpty())).toNotBeNull()
            expect(updatedAt > createdAt).toBe(true)
          }
          describe("With unique 'email'") {
            expect(
              createUser(
                username = "uniqueEmail1",
                email = "unique.email1@email.com",
                password = "senha123",
              ).status,
            ).toBe(HttpStatusCode.Created)

            val response =
              updateUser(
                currentUsername = "uniqueEmail1",
                email = "unique.email2@email.com",
              )
            expect(response.status).toBe(HttpStatusCode.OK)

            val body = expect(response.bodyAsText().parseJson()).toNotBeNull()
            expect(extractUuidVersion(body.getString("id"))).toBe(4)
            expect(body.getString("username")).toBe("uniqueEmail1")
            expect(body.getString("email")).toBe("unique.email2@email.com")
            val createdAt = expect(parsePostgresTimestamp(body.getString("created_at").orEmpty())).toNotBeNull()
            val updatedAt = expect(parsePostgresTimestamp(body.getString("updated_at").orEmpty())).toNotBeNull()
            expect(updatedAt > createdAt).toBe(true)
          }
          describe("With new 'password'") {
            expect(
              createUser(
                username = "newPassword1",
                email = "new.password1@email.com",
                password = "senha123",
              ).status,
            ).toBe(HttpStatusCode.Created)

            val response =
              updateUser(
                currentUsername = "newPassword1",
                password = "123senha",
              )
            expect(response.status).toBe(HttpStatusCode.OK)

            val updatedUser = expect(response.bodyAsText().parseJson()).toNotBeNull()
            expect(extractUuidVersion(updatedUser.getString("id"))).toBe(4)
            expect(updatedUser.getString("username")).toBe("newPassword1")
            expect(updatedUser.getString("email")).toBe("new.password1@email.com")
            val createdAt = expect(parsePostgresTimestamp(updatedUser.getString("created_at").orEmpty())).toNotBeNull()
            val updatedAt = expect(parsePostgresTimestamp(updatedUser.getString("updated_at").orEmpty())).toNotBeNull()
            expect(updatedAt > createdAt).toBe(true)

            val updatedUserInDatabase = User.findOneByUsername("newPassword1")
            val newPassword = updatedUserInDatabase.getString("passwd")
            expect(Password.compare("123senha", newPassword)).toBe(true)
            expect(Password.compare("123senhA", newPassword)).toBe(false)
            expect(Password.compare("SenhaErrada", newPassword)).toBe(false)
          }
        }
      }
    }
}
