package me.kodokenshi.tabnewskobweb.tests.test.assertion

import me.kodokenshi.tabnewskobweb.json.JsonArrayReader
import me.kodokenshi.tabnewskobweb.tests.test.AssertionScope
import me.kodokenshi.tabnewskobweb.tests.test.exception.TestAssertionException
import kotlin.jvm.JvmName

@JvmName("listToBeEmpty")
fun <T> AssertionScope<List<T>>.toBeEmpty() {
  if (actual.isNotEmpty()) throw TestAssertionException("empty", actual.size)
}

@JvmName("jsonArrayReaderToBeEmpty")
fun AssertionScope<JsonArrayReader>.toBeEmpty() {
  if (actual.isNotEmpty()) throw TestAssertionException("empty", actual.size())
}

@JvmName("listToNotBeEmpty")
fun <T> AssertionScope<List<T>>.toNotBeEmpty() {
  if (actual.isEmpty()) throw TestAssertionException("not empty", actual.size)
}

@JvmName("jsonArrayReaderToNotBeEmpty")
fun AssertionScope<JsonArrayReader>.toNotBeEmpty() {
  if (actual.isEmpty()) throw TestAssertionException("not empty", actual.size())
}
