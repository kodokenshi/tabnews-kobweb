package me.kodokenshi.tabnewskobweb.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

fun String.toJsonArrayOrNull() =
  try {
    toJsonArray()
  } catch (_: Exception) {
    null
  }

fun String.toJsonArray() = JsonArrayReader.parse(this)

open class JsonArrayReader(
  private val array: List<JsonReader>,
) {
  //
	
  companion object {
    fun parse(string: String) =
      JsonArrayReader(
        Json
          .parseToJsonElement(string)
          .let {
            if (it is JsonArray) {
              it.jsonArray
                .map {
                  JsonReader(it.jsonObject)
                }
            } else {
              listOf(JsonReader(it.jsonObject))
            }
          },
      )
  }

  //
  fun isEmpty() = array.isEmpty()

  fun isNotEmpty() = array.isNotEmpty()

  fun size() = array.size

  fun first(reader: JsonReader.() -> Unit = {}) = array.first().apply(reader)

  fun firstOrNull(reader: JsonReader.() -> Unit = {}) = array.firstOrNull()?.apply(reader)

  fun last(reader: JsonReader.() -> Unit = {}) = array.last().apply(reader)

  fun lastOrNull(reader: JsonReader.() -> Unit = {}) = array.lastOrNull()?.apply(reader)

  fun forEach(reader: JsonReader.() -> Unit = {}) = array.forEach(reader)

  fun at(
    index: Int,
    reader: JsonReader.() -> Unit = {},
  ) = array[index].apply(reader)

  fun atOrNull(
    index: Int,
    reader: JsonReader.() -> Unit = {},
  ) = array.getOrNull(index)?.apply(reader)
	
  //

  fun toArray() = JsonArray(array.map { it.toObject() })

  override fun toString() = toArray().toString()
}
