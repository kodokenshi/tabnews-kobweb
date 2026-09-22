@file:Suppress("ktlint:standard:function-naming")

package me.kodokenshi.tabnewskobweb.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import kotlin.time.Duration

@Composable
fun AsyncOnce(op: suspend () -> Unit) = LaunchedEffect(Unit) { op() }

@Composable
fun AsyncInterval(
  interval: Duration,
  op: suspend () -> Unit,
) = AsyncOnce {
  while (true) {
    op()
    delay(interval)
  }
}
