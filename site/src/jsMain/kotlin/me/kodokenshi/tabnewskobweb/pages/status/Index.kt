@file:Suppress("ktlint:standard:function-naming")

package me.kodokenshi.tabnewskobweb.pages.status

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.compose.ui.modifiers.gap
import com.varabyte.kobweb.compose.ui.modifiers.padding
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.text.SpanText
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import me.kodokenshi.tabnewskobweb.component.AsyncInterval
import me.kodokenshi.tabnewskobweb.util.PTBR
import me.kodokenshi.tabnewskobweb.util.fetchAPI
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.H1
import org.jetbrains.compose.web.dom.H2
import org.jetbrains.compose.web.dom.Text
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@Page
@Composable
fun StatusPage() {
  var updatedAt by remember { mutableStateOf("...") }
  var databaseVersion by remember { mutableStateOf("...") }
  var databaseOpenedConnections by remember { mutableStateOf("...") }
  var databaseMaxConnections by remember { mutableStateOf("...") }
	
  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .padding(all = 10.px),
  ) {
    Column(
      modifier =
        Modifier
          .gap(5.px),
    ) {
      H1 {
        Text("Status")
      }
      UpdatedAt(updatedAt)
      H2 {
        Text("Database")
      }
      SpanText("Versão: $databaseVersion")
      SpanText("Conexões abertas: $databaseOpenedConnections")
      SpanText("Conexões máximas: $databaseMaxConnections")
    }
  }
	
  AsyncInterval(2.seconds) {
    val status = fetchAPI("/api/v1/status")
		
    updatedAt = status?.getString("updated_at") ?: updatedAt
    databaseVersion = status?.getNestedString("dependencies.database.version") ?: databaseVersion
    databaseOpenedConnections =
      status?.getNestedString("dependencies.database.opened_connections") ?: databaseOpenedConnections
    databaseMaxConnections = status?.getNestedString("dependencies.database.max_connections") ?: databaseMaxConnections
  }
}

@Composable
private fun UpdatedAt(instant: String) {
  val instant =
    Instant
      .parseOrNull(instant)
      ?.toLocalDateTime(TimeZone.currentSystemDefault())
      ?.format(LocalDateTime.Formats.PTBR)
      ?: "..."
  SpanText("Última atualização: $instant")
}
