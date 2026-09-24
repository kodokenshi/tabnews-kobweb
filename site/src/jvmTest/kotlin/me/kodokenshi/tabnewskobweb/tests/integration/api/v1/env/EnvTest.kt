package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.env

import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.TestConfig
import me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1.testContext
import me.kodokenshi.tabnewskobweb.tests.test.assertion.toBe
import org.junit.jupiter.api.Test

class EnvTest {
  @Test
  suspend fun test() =
    testContext(this::class) {
      describe("Check if in dev environment") {
        expect(TestConfig.IS_DEV_ENV).toBe(true)
      }
    }
}
