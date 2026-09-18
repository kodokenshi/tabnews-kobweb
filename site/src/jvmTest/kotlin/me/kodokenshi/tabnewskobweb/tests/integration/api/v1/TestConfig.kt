package me.kodokenshi.tabnewskobweb.me.kodokenshi.tabnewskobweb.tests.integration.api.v1

object TestConfig {
  val IS_VERBOSE by lazy { System.getProperty("verbose", "false").toBoolean() }
}
