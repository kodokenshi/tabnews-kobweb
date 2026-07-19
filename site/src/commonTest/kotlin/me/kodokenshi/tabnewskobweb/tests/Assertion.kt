package me.kodokenshi.tabnewskobweb.tests

import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.test.runTest

fun test(name: String, op: Test.() -> Unit) = op(Test(name))
fun runTest(name: String, op: suspend Test.() -> Unit) = runTest { op(Test(name)) }

class Test(private val name: String) {
	
	companion object {
		
		private const val RESET = "\u001B[0m"
		private const val FAIL = "\u001B[31m\u001B[1m"
		private const val OK = "\u001B[32m"
		private const val TEXT = "\u001B[37m"
		
	}
	
	suspend fun fetch(url: String) = HttpClient().use { it.get(url) }
	
	fun <T> expect(block: () -> T) = Assertion(block())
	fun <T> expect(value: T) = Assertion(value)
	
	inner class Assertion<T>(private val actual: T) {
		
		fun toBe(expected: T) {
			
			if (actual != expected) {
				
				fail(expected)
				throw AssertionError()
				
			}
			
			ok()
			
		}
		
		private fun fail(expected: T) {
			println(
				"""
				$FAIL[FAIL]: $TEXT$name
				$FAIL   >    ${RESET}Expected: $OK$expected
				$FAIL   >    ${RESET}Actual  : $FAIL$actual
			""".trimIndent()
			)
		}
		
		private fun ok() = println("$OK[PASS]: $TEXT$name")
		
	}
	
}