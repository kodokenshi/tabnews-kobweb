package me.kodokenshi.tabnewskobweb.tests

import kotlinx.coroutines.test.runTest
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

fun test(name: String, op: Test.() -> Unit) = op(Test(name))
fun runTest(name: String, op: suspend Test.() -> Unit) = runTest { op(Test(name)) }

class Test(private val name: String) {
	
	companion object {
		
		private const val RESET = "\u001B[0m"
		private const val FAIL = "\u001B[31m\u001B[1m"
		private const val OK = "\u001B[32m"
		private const val TEXT = "\u001B[37m"
		
	}
	
	fun <T> expect(block: () -> T) = Assertion(block())
	fun <T> expect(value: T) = Assertion(value)
	
	inner class Assertion<T>(private val actual: T) {
		
		fun toBe(expected: T, message: String? = "O valor obtido não é o esperado!") {
			assertEquals(expected, actual, fail())
			ok()
		}
		
		fun toNotBe(expected: T) {
			assertNotEquals(expected, actual, fail())
			ok()
		}
		
		private fun fail() = "$FAIL[FAIL]: $TEXT$name$RESET"
		private fun ok() = println("$OK[ OK ]: $TEXT$name")
		
	}
	
}