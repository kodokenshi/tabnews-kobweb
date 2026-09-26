package me.kodokenshi.tabnewskobweb.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

fun jsonArray(op: JsonArrayWriter.() -> Unit) = JsonArrayWriter(mutableListOf()).apply(op)

fun buildJsonArray(op: JsonArrayWriter.() -> Unit) = jsonArray(op).toString()

open class JsonArrayWriter(
  private val array: MutableList<JsonWriter>,
) : JsonArrayReader(array) {
  //
	
  companion object {
    fun parse(string: String) =
      JsonArrayWriter(
        Json
          .parseToJsonElement(string)
          .let {
            if (it is JsonArray) {
              it.jsonArray
                .map {
                  JsonWriter(it.jsonObject.toMutableMap())
                }
            } else {
              listOf(JsonWriter(it.jsonObject.toMutableMap()))
            }
          }.toMutableList(),
      )
  }
	
  //

  fun jsonObject(writer: JsonWriter.() -> Unit) {
    array.add(JsonWriter(mutableMapOf()).apply(writer))
  }
	
  //
}
