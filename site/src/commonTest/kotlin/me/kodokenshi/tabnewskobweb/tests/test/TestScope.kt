package me.kodokenshi.tabnewskobweb.tests.test

import kotlinx.coroutines.withTimeoutOrNull
import me.kodokenshi.tabnewskobweb.tests.test.exception.TestException
import me.kodokenshi.tabnewskobweb.tests.test.exception.TestFailedException
import me.kodokenshi.tabnewskobweb.tests.test.exception.TestTimeoutException
import kotlin.time.Duration
import kotlin.time.measureTime

class TestScopeDsl(
  val name: String,
  val testTimeout: Duration,
  val isVerbose: Boolean,
  val test: suspend TestScope.() -> Unit,
) : TestScope {
  private val childTests = LinkedHashSet<TestScopeDsl>()
  private var result: TestResult? = null
  private var beforeAll: BeforeAll? = null

  override suspend fun beforeAll(
    timeout: Duration,
    op: suspend () -> Unit,
  ) {
    beforeAll = BeforeAll(timeout, op)
  }

  override suspend fun describe(
    name: String,
    testTimeout: Duration,
    op: suspend TestScope.() -> Unit,
  ) {
    childTests.add(TestScopeDsl(name, testTimeout, isVerbose, op))
  }

  override fun <T> expect(value: T) = AssertionScope(value)

  suspend fun run() {
    result = runThis()
    childTests.forEach { it.run() }
  }

  fun isPass(): Boolean = result?.pass == true && childTests.all { it.isPass() }

  fun tookTooLong(): Boolean =
    result?.measuredTime?.inWholeMilliseconds?.let { it >= Test.ALERT_TEST_DURATION } == true ||
      childTests.any { it.tookTooLong() }

  fun logResult() {
    log("", name)
  }

  private fun log(
    margin: String,
    rootName: String,
  ) {
    val result = result
    if (result != null) {
      val pass = isPass()
      if (isVerbose || !pass || tookTooLong()) {
//        val margin = "$margin${if (rootName != name) ">" else ""}"
        println(
          buildString {
            append(". ")
            append(margin)
            append(if (pass) Test.OK_TEXT else Test.FAIL_TEXT)
            append(if (pass) " \uD83D\uDDF8" else " X")
            append("${Test.RESET} ${Test.WHITE_TEXT}")
            append(name)
            append(Test.WHITE_TEXT)
            append(" (${Test.formatDuration(result.measuredTime)})")
            append(Test.RESET)
          },
        )
        if (result.description.isNotBlank()) {
          println(
            buildString {
              val margin = " ".repeat(margin.length + 3)
              val lines = result.description.split("\n")
              val iterator = lines.iterator()
              while (iterator.hasNext()) {
                append(". ")
                append(margin)
                append(iterator.next())
                if (iterator.hasNext()) append("\n")
              }
            },
          )
        }
      }
    }
		
    childTests.forEach { it.log("$margin ", rootName) }
  }

  private suspend fun runThis(): TestResult {
    var description = ""
    var pass = false
    val measureTime =
      measureTime {
        try {
          withTimeoutOrNull(testTimeout) {
            beforeAll?.let {
              val test = TestScopeDsl("$name - beforeAll", it.timeout, isVerbose) { it.op() }
              test.run()
              if (test.result?.pass != true) throw TestFailedException()
            }
            test(this@TestScopeDsl)
          } ?: throw TestTimeoutException(testTimeout)
          pass = true
        } catch (e: Throwable) {
          pass = false
          description = if (e is TestException) e.message ?: "unspecified" else e.stackTraceToString()
        }
      }
    return TestResult(measureTime, pass, description)
  }
}

interface TestScope : TestContextScope {
  fun <T> expect(value: T): AssertionScope<T>
}
