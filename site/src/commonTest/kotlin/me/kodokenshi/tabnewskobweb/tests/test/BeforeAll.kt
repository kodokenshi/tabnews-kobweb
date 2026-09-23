package me.kodokenshi.tabnewskobweb.tests.test

import kotlin.time.Duration

data class BeforeAll(
  val timeout: Duration,
  val op: suspend () -> Unit,
)
