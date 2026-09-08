package me.kodokenshi.tabnewskobweb.tests

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.test.runTest

fun testContext(name: String, logPassedTests: Boolean = false, op: suspend TestContext.() -> Unit) = runTest {
	
	val testContext = TestContext(logPassedTests)
	
	println("----- Test Context: $name")
	var threwException = false
	try {
		
		op(testContext)
		
	} catch (e: Exception) {
		
		if (e.message != "assertion") {
			println(
				"""
					${TestContext.FAIL} [ERROR]: ${TestContext.TEXT}$name
					${TestContext.FAIL}    >    ${TestContext.RESET}${e.message}
				""".trimIndent()
			)
			threwException = true
		}
		
	} catch (t: Throwable) {
		
		if (t.message != "assertion") {
			println(
				"""
					${TestContext.FAIL} [ERROR]: ${TestContext.TEXT}$name
					${TestContext.FAIL}    >    ${TestContext.RESET}${t.message}
				""".trimIndent()
			)
			threwException = true
		}
		
	}
	
	if (logPassedTests) println()
	println(" Passed: ${TestContext.OK}${testContext.passed}")
	println(" Failed: ${TestContext.FAIL}${testContext.failed}")
	
	if (threwException || testContext.failed > 0) {
		
		println("-------------------")
		throw TestContext.TestException()
		
	}
	
	println("-------------------")
	
}

class TestContext(private val logPassedTests: Boolean = false) {
	
	var failed = 0; private set
	var passed = 0; private set
	
	companion object {
		
		private val httpClient by lazy { HttpClient() }
		
		const val RESET = "\u001B[0m"
		const val FAIL = "\u001B[31m\u001B[1m"
		const val OK = "\u001B[32m"
		const val TEXT = "\u001B[37m"
		
	}
	
	enum class FetchMethod {
		GET, POST
	}
	
	suspend fun fetch(url: String, fetchMethod: FetchMethod = FetchMethod.GET) =
		when (fetchMethod) {
			FetchMethod.GET -> httpClient.get(url)
			FetchMethod.POST -> httpClient.post(url)
		}
	
	fun test(name: String) = TestScope(name)
	
	inner class TestScope(private val name: String) {
		
		fun <T> expect(value: T) = Assertion(value)
		
		inner class Assertion<T>(private val actual: T) {
			
			//
			fun toBe(expected: T) = toBe_(expected)
			fun toBeGreaterThan(number: Number): Boolean {
				
				if (actual !is Number) {
					fail("not a number")
					return false
				}
				
				val pass = actual.toDouble() > number.toDouble()
				
				if (pass) ok()
				else fail("not greater than $number")
				
				return pass
				
			}
			//
			fun toNotBe(expected: T) = toNotBe_(expected)
			//
			fun toBeMissing() = toBe_(null)
			fun toBePresent() = if (toNotBe_(null)) actual!! else throw AssertionException("assertion")
			//
			private fun toBe_(expected: Any?, message: Any? = expected): Boolean {
				
				if (actual != expected) {
					
					fail(message)
					return false
					
				}
				
				ok()
				return true
				
				
			}
			private fun toNotBe_(expected: Any?, message: Any? = expected): Boolean {
				
				if (actual == expected) {
					
					fail("not $message")
					return false
					
				}
				
				ok()
				return true
				
			}
			
			private fun fail(expected: Any?) {
				
				println(
					"""
					$FAIL [FAIL]: $TEXT$name
					$FAIL    >    ${RESET}Expected: $OK$expected
					$FAIL    >    ${RESET}Actual  : $FAIL$actual
				""".trimIndent()
				)
				failed++
				
			}
			private fun ok() {
				
				if (logPassedTests) println(" $OK[PASS]: $TEXT$name ($actual)")
				passed++
				
			}
			
		}
		
	}
	
	class AssertionException(message: String? = null) : Exception(message)
	class TestException(message: String? = null) : Exception(message)
	
}