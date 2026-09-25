package me.kodokenshi.tabnewskobweb.tests.test

import me.kodokenshi.tabnewskobweb.tests.test.exception.TestFailedException
import kotlin.reflect.KClass
import kotlin.time.Duration
import kotlin.time.measureTime

suspend fun commonTestContext(
  owner: KClass<*>,
  isVerbose: Boolean,
  op: suspend TestContextScope.() -> Unit,
) {
  if (!Test(owner, isVerbose, op).run()) {
    throw TestFailedException()
  }
}

class Test(
  private val owner: KClass<*>,
  private val isVerbose: Boolean,
  private val op: suspend TestContextScope.() -> Unit,
) : TestContextScope {
  private var isRunning = false
  private val tests = LinkedHashSet<TestScopeDsl>()
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
    tests.add(TestScopeDsl(name, testTimeout, isVerbose, op))
  }

  suspend fun run(): Boolean {
    if (isRunning) return false
    isRunning = true

    op()

    var overrideFail = false
    val measuredTime =
      measureTime {
        beforeAll?.let {
          val test = TestScopeDsl("beforeAll", it.timeout, isVerbose) { it.op() }
          test.run()
          if (!test.isPass()) {
            overrideFail = true
            return@measureTime
          }
        }
        tests.forEach { it.run() }
      }

    val pass = tests.all { it.isPass() } && !overrideFail

    if (isVerbose) println(".")
    println(
      buildString {
        append(". ")
        append(if (pass) OK_BACKGROUND else FAIL_BACKGROUND)
        append(if (pass) " PASS " else " FAIL ")
        append("$RESET $WHITE_TEXT")
        append(
          owner.qualifiedName?.substringAfter(".tests.")?.replace(".", "/")?.let {
            val index = it.lastIndexOf('/') + 1
            it.replaceRange(index, index, RESET)
          },
        )
        append(WHITE_TEXT)
        append(" (${formatDuration(measuredTime)})")
        append(RESET)
      },
    )

    if (!pass || isVerbose) {
      tests.forEach { it.logResult() }
    } else {
      tests.filter { it.tookTooLong() }.forEach { it.logResult() }
    }

    isRunning = false
    return pass
  }

  companion object {
    const val WHITE_TEXT = "\u001B[37m"
    const val WHITE_BACKGROUND = "\u001B[30;47m\u001B[1m"
    const val BLACK_TEXT = "\u001B[30m"
    const val RESET = "\u001B[0m"
    const val OK_BACKGROUND = "\u001B[30;42m\u001B[1m"
    const val OK_TEXT = "\u001B[32m\u001B[1m"
    const val FAIL_BACKGROUND = "\u001B[30;41m\u001B[1m"
    const val FAIL_TEXT = "\u001B[31m\u001B[1m"

    const val ALERT_TEST_DURATION = 5000

    fun formatDuration(duration: Duration): String =
      duration.toComponents { seconds, nanoseconds ->
        val millis = nanoseconds / 1_000_000
        val alert = (seconds * 1000) >= ALERT_TEST_DURATION
        buildString {
          if (alert) append(FAIL_BACKGROUND)
          append("$seconds.${millis.toString().padStart(3, '0')} s")
          if (alert) append(RESET)
        }
      }
  }
}
