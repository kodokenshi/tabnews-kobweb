package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1

import me.kodokenshi.tabnewskobweb.infra.env
import me.kodokenshi.tabnewskobweb.tests.test.TestContextScope
import me.kodokenshi.tabnewskobweb.tests.test.commonTestContext
import kotlin.reflect.KClass

object TestConfig {
  val IS_VERBOSE by lazy { System.getProperty("verbose", "false").toBoolean() }
  val FILTER by lazy { System.getProperty("filter", "").split(",").map { it.trim() } }
  val IS_DEV_ENV by lazy { env.get("ENVIRONMENT", "prod").equals("dev", true) }
}

suspend fun testContext(
  owner: KClass<*>,
  op: suspend TestContextScope.() -> Unit,
) {
  if (TestConfig.FILTER.isNotEmpty()) {
    val clazz = owner.qualifiedName ?: return
		
    if (TestConfig.FILTER.none {
        clazz.contains(it, true)
      }
    ) {
      return
    }
  }
  commonTestContext(owner, TestConfig.IS_VERBOSE, op)
}
