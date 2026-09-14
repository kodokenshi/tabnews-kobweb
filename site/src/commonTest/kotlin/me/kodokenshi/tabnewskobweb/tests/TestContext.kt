package me.kodokenshi.tabnewskobweb.tests

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

suspend fun testContext(
	name: String,
	testTimeoutInMillis: Long = TestContext.defaultTestTimeoutInMillis,
	logPassedTests: Boolean = false,
	hide: Boolean = false,
	op: suspend TestContext.() -> Unit,
) {
	
	val start = Clock.System.now()
	val testContext = TestContext(testTimeoutInMillis, logPassedTests)
	
	if (!hide) println("----- Test Context: $name")
	var threwException = false
	
	try {
		testContext.measureTime(testTimeoutInMillis) {
			op(testContext)
		}
	} catch (t: Throwable) {
		
		if (t.message != "assertion") {
			println(
				"""
        ${TestContext.FAIL}[ERROR]: ${TestContext.TEXT}$name${TestContext.RESET}
        ${TestContext.FAIL}   >    ${TestContext.RESET}${t.message}${TestContext.RESET}
        """.trimIndent()
			)
		}
		
		threwException = true
		
	}
	
	val measuredTime = Clock.System.now() - start
	val report = measuredTime.inWholeMilliseconds >= TestContext.REPORT_TIME
	
	if (report) {
		if (hide) println("----- Test Context: $name")
		println("${TestContext.FAIL}This test took ${TestContext.formatDuration(measuredTime)} to complete.${TestContext.RESET}")
		if (hide) println("-------------------")
	}
	
	if (hide) {
		if (threwException) throw TestContext.TestException()
		return
	}
	
	if (logPassedTests) println()
	println("Passed: ${TestContext.OK}${testContext.passed}${TestContext.RESET}")
	println("Failed: ${TestContext.FAIL}${testContext.failed}${TestContext.RESET}")
	
	if (threwException || testContext.failed > 0) {
		println("-------------------")
		throw TestContext.TestException()
	}
	
	println("-------------------")
	
}

class TestContext(
	var testTimeoutInMillis: Long = defaultTestTimeoutInMillis,
	private val logPassedTests: Boolean = false
) {
	
	var failed = 0; private set
	var passed = 0; private set
	
	companion object {
		
		const val REPORT_TIME = 5000L
		var defaultTestTimeoutInMillis = 60000L
		
		private val httpClient by lazy { HttpClient() }
		
		const val RESET = "\u001B[0m"
		const val FAIL = "\u001B[31m\u001B[1m"
		const val OK = "\u001B[32m"
		const val TEXT = "\u001B[37m"
		
		fun formatDuration(duration: Duration): String = duration.toComponents { seconds, nanoseconds ->
			val millis = nanoseconds / 1_000_000
			"$seconds.${millis.toString().padStart(3, '0')} s"
		}
		
	}
	
	enum class FetchMethod { GET, POST }
	
	suspend fun fetch(
		url: String,
		fetchMethod: FetchMethod = FetchMethod.GET,
		timeoutInMillis: Long = testTimeoutInMillis
	) = measureTime(timeoutInMillis) {
		
		when (fetchMethod) {
			FetchMethod.GET -> httpClient.get(url)
			FetchMethod.POST -> httpClient.post(url)
		}
		
	}
	
	fun test(name: String, testTimeoutInMillis: Long = this.testTimeoutInMillis) = TestScope(testTimeoutInMillis, name)
	
	inner class TestScope(
		private val testTimeoutInMillis: Long,
		private val name: String
	) {
		
		private val startedAt = Clock.System.now()
		
		suspend fun <T> expect(value: T) = measureTime(testTimeoutInMillis) { Assertion(value) }
		
		inner class Assertion<T>(private val actual: T) {
			
			suspend fun toBe(expected: T) = toBe_(expected)
			suspend fun toBeGreaterThan(number: Number): Boolean {
				
				if (actual !is Number) {
					fail("not a number")
					return false
				}
				
				val pass = actual.toDouble() > number.toDouble()
				if (pass) ok() else fail("not greater than $number")
				return pass
				
			}
			suspend fun toNotBe(expected: T) = toNotBe_(expected)
			suspend fun toBeMissing() = toBe_(null)
			suspend fun toBePresent() = if (toNotBe_(null)) actual!! else throw AssertionException("assertion")
			
			private suspend fun toBe_(expected: Any?, message: Any? = expected) = measureTime(testTimeoutInMillis) {
				
				if (actual != expected) {
					fail(message)
					return@measureTime false
				}
				ok()
				true
				
			}
			private suspend fun toNotBe_(expected: Any?, message: Any? = expected) = measureTime(testTimeoutInMillis) {
				
				if (actual == expected) {
					fail("not $message")
					return@measureTime false
				}
				ok()
				true
				
			}
			
			private fun finalizeTest() = Clock.System.now() - startedAt
			private fun fail(expected: Any?) {
				
				val measuredTime = finalizeTest()
				println(
					"""
          $FAIL[FAIL]: $TEXT$name${RESET}${if (measuredTime.inWholeMilliseconds >= REPORT_TIME) " $FAIL(${formatDuration(measuredTime)})$RESET" else ""}
          $FAIL   >    ${RESET}Expected: $OK$expected${RESET}
          $FAIL   >    ${RESET}Actual  : $FAIL$actual${RESET}
          """.trimIndent()
				)
				failed++
				
			}
			private fun ok() {
				
				val measuredTime = finalizeTest()
				val report = measuredTime.inWholeMilliseconds >= REPORT_TIME
				
				if (logPassedTests || report) {
					println("$OK[PASS]: $TEXT$name ($actual)${RESET}${if (report) " $FAIL(${formatDuration(measuredTime)})$RESET" else ""}")
				}
				passed++
				
			}
			
		}
		
	}
	
	class AssertionException(message: String? = null) : Exception(message)
	class TestException(message: String? = null) : Exception(message)
	
	internal suspend fun <T> measureTime(
		timeoutMillis: Long,
		action: suspend () -> T
	): T {
		
		val result = withTimeoutOrNull(timeoutMillis.milliseconds) { action() }
		return result ?: throw Exception("Exceeded timeout of $timeoutMillis ms for a test.")
		
	}
	
}
