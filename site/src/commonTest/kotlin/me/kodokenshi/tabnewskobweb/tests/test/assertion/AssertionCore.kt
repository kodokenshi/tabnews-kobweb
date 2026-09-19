package me.kodokenshi.tabnewskobweb.tests.test.assertion

import me.kodokenshi.tabnewskobweb.tests.test.AssertionScope
import me.kodokenshi.tabnewskobweb.tests.test.exception.TestAssertionException

fun <T> AssertionScope<T>.toBe(value: T) {
  if (actual != value) {
    throw TestAssertionException(value, actual)
  }
}

fun <T> AssertionScope<T>.toNotBe(value: T) {
  if (actual == value) {
    throw TestAssertionException("not be", value)
  }
}

fun <T> AssertionScope<T>.toBePresent() = actual ?: throw TestAssertionException("not null", null)

fun <T> AssertionScope<T>.toBeMissing() {
  if (actual != null) throw TestAssertionException("null", actual)
}
