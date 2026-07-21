package me.kodokenshi.tabnewskobweb.tests

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.test.runTest

fun testContext(name: String, logPassedTests: Boolean = false, op: suspend TestContext.() -> Unit) =
	runTest {
		
		val context = TestContext(name, logPassedTests)
		op(context)
		context.checkFailed()
		
	}

class TestContext(private val name: String, var logPassedTests: Boolean) {
	
	init {
		if (logPassedTests) printHeader()
	}
	
	companion object {
		
		private val httpClient by lazy { HttpClient() }
		
		private const val RESET = "\u001B[0m"
		private const val FAIL = "\u001B[31m\u001B[1m"
		private const val OK = "\u001B[32m"
		private const val TEXT = "\u001B[37m"
		
	}
	
	private var failed = 0
	private var passed = 0
	
	suspend fun fetch(url: String) = httpClient.get(url)
	fun test(name: String, op: TestScope.() -> Unit) {
		
		try {
			
			op(TestScope(name))
			passed++
			
		} catch (e: Exception) {
			
			failed++
			
			if (e.message == "toBePresent") return
			
			printHeader()
			println(
				"""
					$FAIL [ERROR]: $TEXT$name - Unhandled exception
					$FAIL    >    ${RESET}${e.message}
				""".trimIndent()
			)
			
		} catch (t: Throwable) {
			
			failed++
			
			if (t.message == "AssertionError") return
			
			printHeader()
			println(
				"""
					$FAIL [ERROR]: $TEXT$name - Unhandled throwable
					$FAIL    >    ${RESET}${t.message}
				""".trimIndent()
			)
			
		}
		
	}
	
	inner class TestScope(private val name: String) {
		
		fun <T> expect(value: T) = Assertion(value)
		
		inner class Assertion<T>(var actual: T) {
			
			fun toBe(expected: T) = toBe_(expected)
			fun toNotBe(expected: T) = toNotBe_(expected)
			fun toBePresent() = toNotBe_(null, "to be present") ?: throw NullPointerException("toBePresent")
			
			private fun toBe_(expected: Any?, message: Any? = expected): T {
				
				if (actual != expected) fail(message)
				else ok()
				
				return actual
				
			}
			
			private fun toNotBe_(expected: Any?, message: Any? = expected): T {
				
				if (actual == expected) fail(message)
				else ok()
				
				return actual
				
			}
			
			private fun fail(expected: Any?) {
				
				printHeader()
				println(
					"""
					$FAIL [FAIL]: $TEXT$name
					$FAIL    >    ${RESET}Expected: $OK$expected
					$FAIL    >    ${RESET}Actual  : $FAIL$actual
				""".trimIndent()
				)
				
				throw AssertionError("AssertionError")
				
			}
			
			private fun ok() {
				if (logPassedTests) println(" $OK[PASS]: $TEXT$name ($actual)")
			}
			
		}
		
	}
	
	private var headerPrinted = false
	private fun printHeader() {
		if (headerPrinted) return
		headerPrinted = true
		println("----- Test Context: $name")
	}
	
	fun checkFailed() {
		
		if (logPassedTests || failed > 0) {
			
			println("\n Passed: $OK$passed")
			println(" Failed: $FAIL$failed")
			
			if (failed > 0) {
				
				println("-------------------")
				throw AssertionError()
				
			} else println("-------------------")
			
		}
		
	}
	
}
