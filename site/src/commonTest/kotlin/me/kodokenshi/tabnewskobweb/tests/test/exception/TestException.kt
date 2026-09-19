package me.kodokenshi.tabnewskobweb.tests.test.exception

import me.kodokenshi.tabnewskobweb.tests.test.Test
import kotlin.time.Duration

sealed class TestException(
  message: String,
) : Exception(message)

class TestTimeoutException(
  timeout: Duration,
) : TestException(
    "${Test.FAIL_TEXT}Exceeded timeout of ${Test.formatDuration(timeout)} ${Test.FAIL_TEXT}for a test.${Test.RESET}",
  )

class TestFailedException : TestException("Test failed")

class TestAssertionException(
  private val expected: Any?,
  private val received: Any?,
) : TestException("") {
  override val message =
    buildString {
      append("${Test.WHITE_BACKGROUND} at ")
      append(
        stackTraceToString()
          .split("\n")
          .getOrNull(2)
          ?.substringAfterLast("(")
          ?.substringBeforeLast(")"),
      )
      append(" ${Test.RESET}\n")
      append("Expected: ${Test.OK_TEXT}$expected${Test.RESET}")
      append("\n")
      append("Received: ${Test.FAIL_TEXT}$received${Test.RESET}")
    }
}
