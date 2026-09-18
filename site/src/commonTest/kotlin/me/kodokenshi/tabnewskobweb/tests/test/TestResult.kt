package me.kodokenshi.tabnewskobweb.tests.test

import kotlin.time.Duration

data class TestResult(
  val measuredTime: Duration,
  val pass: Boolean,
  val description: String,
)
