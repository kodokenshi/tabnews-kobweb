package me.kodokenshi.tabnewskobweb.tests.test.assertion

import me.kodokenshi.tabnewskobweb.tests.test.AssertionScope
import me.kodokenshi.tabnewskobweb.tests.test.exception.TestAssertionException

fun <T> AssertionScope<List<T>>.toBeEmpty() {
  if (actual.isNotEmpty()) throw TestAssertionException("empty", actual.size)
}

fun <T> AssertionScope<List<T>>.toNotBeEmpty() {
  if (actual.isEmpty()) throw TestAssertionException("not empty", actual.size)
}
