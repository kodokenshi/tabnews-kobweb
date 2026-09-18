package me.kodokenshi.tabnewskobweb.tests.test.assertion

import me.kodokenshi.tabnewskobweb.tests.test.AssertionException
import me.kodokenshi.tabnewskobweb.tests.test.AssertionScope

fun <T> AssertionScope<List<T>>.toBeEmpty() {
  if (actual.isNotEmpty()) throw AssertionException("empty", actual.size)
}

fun <T> AssertionScope<List<T>>.toNotBeEmpty() {
  if (actual.isEmpty()) throw AssertionException("not empty", actual.size)
}
