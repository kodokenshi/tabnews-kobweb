package me.kodokenshi.tabnewskobweb.tests.test.assertion

import me.kodokenshi.tabnewskobweb.tests.test.AssertionException
import me.kodokenshi.tabnewskobweb.tests.test.AssertionScope

fun <T> AssertionScope<T>.toBe(value: T) {
  if (actual != value) {
    throw AssertionException(value, actual)
  }
}

fun <T> AssertionScope<T>.toNotBe(value: T) {
  if (actual == value) {
    throw AssertionException("not be", value)
  }
}

fun <T> AssertionScope<T>.toBePresent() = actual ?: throw AssertionException("not null", null)

fun <T> AssertionScope<T>.toBeMissing() {
  if (actual != null) throw AssertionException("null", actual)
}
