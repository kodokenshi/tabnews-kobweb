package me.kodokenshi.tabnewskobweb.tests.test

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface TestContextScope {
  suspend fun describe(
    name: String,
    testTimeout: Duration = 60.seconds,
    op: suspend TestScope.() -> Unit,
  )
}
