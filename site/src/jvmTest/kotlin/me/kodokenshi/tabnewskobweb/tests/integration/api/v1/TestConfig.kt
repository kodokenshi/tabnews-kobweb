package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1

import me.kodokenshi.tabnewskobweb.infra.env

object TestConfig {
  val IS_VERBOSE by lazy { System.getProperty("verbose", "false").toBoolean() }
  val IS_DEV_ENV by lazy { env.get("ENVIRONMENT", "prod").equals("dev", true) }
}
