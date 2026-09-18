package me.kodokenshi.tabnewskobweb.tests.test

data class AssertionScope<T>(
  val actual: T,
)

class AssertionException(
  private val expected: Any?,
  private val received: Any?,
) : Exception() {
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
