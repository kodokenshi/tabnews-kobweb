package me.kodokenshi.tabnewskobweb.json

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun errorJson(reason: String) = buildJson("error", reason)

fun json(op: Json.() -> Unit) = Json().apply(op)

fun json(vararg map: String) =
  Json().apply {
    val iterator = map.iterator()
    while (iterator.hasNext()) {
      val key = iterator.next()
      val value =
        if (iterator.hasNext()) {
          iterator.next()
        } else {
          throw IndexOutOfBoundsException("Missing value for key $key.")
        }
		
      put(key, value)
    }
  }

fun buildJson(op: Json.() -> Unit) = json(op).toString()

fun buildJson(vararg map: String) = json(*map).toString()

fun String.parseJson() = Json.parse(this)

class Json(
  root: Map<String, JsonElement> = mapOf(),
) {
  companion object {
    fun parse(map: Map<String, JsonElement>) = Json(map)

    fun parse(string: String): Json? =
      try {
        if (string.isBlank()) {
          Json()
        } else {
          Json(
            kotlinx.serialization.json.Json
              .parseToJsonElement(string)
              .jsonObject,
          )
        }
      } catch (_: Exception) {
        null
      }

    fun parseList(string: String): List<Json>? =
      try {
        kotlinx.serialization.json.Json
          .parseToJsonElement(string)
          .jsonArray
          .map { parse(it.toString())!! }
      } catch (_: Exception) {
        null
      }
  }

  private val root = root.toMutableMap()

  //
  private fun put(
    key: String,
    value: JsonElement,
  ) {
    root[key] = value
  }

  fun put(
    key: String,
    json: Json,
  ) = put(key, json.toObject())

  fun put(
    key: String,
    json: Json.() -> Unit,
  ) = put(key, json(json).toObject())

  fun put(
    key: String,
    value: String?,
  ) = put(key, JsonPrimitive(value))

  fun put(
    key: String,
    value: Number?,
  ) = put(key, JsonPrimitive(value))
  //

  //
  fun putNested(
    key: String,
    value: JsonElement,
  ) {
    val path = key.split('.')
    var currentMap = this.root
		
    path.forEachIndexed { index, segment ->
			
      if (index == path.lastIndex) {
        currentMap[segment] = value
      } else {
        val nextElement = currentMap[segment]
        val nextJsonObject =
          if (nextElement is JsonObject) {
            nextElement.toMutableMap()
          } else {
            mutableMapOf()
          }
				
        currentMap[segment] = JsonObject(nextJsonObject)
        currentMap = nextJsonObject
      }
    }
  }

  fun putNested(
    key: String,
    json: Json,
  ) = putNested(key, json.toObject())

  fun putNested(
    key: String,
    json: Json.() -> Unit,
  ) = putNested(key, json(json).toObject())

  fun putNested(
    key: String,
    value: String?,
  ) = putNested(key, JsonPrimitive(value))
  //

  //
  private fun getElement(key: String) = root[key]

  fun getString(key: String) = getElement(key)?.jsonPrimitive?.content

  fun getJson(key: String): Json? = parse(getElement(key) as? JsonObject ?: return null)
  //

  //
  fun getNested(key: String): JsonElement? {
    val path = key.split('.')
    var currentMap: Map<String, JsonElement> = root
		
    path.forEachIndexed { index, segment ->
			
      if (index == path.lastIndex) return currentMap[segment]
      currentMap = (currentMap[segment] as? JsonObject) ?: return null
    }
		
    return null
  }

  fun getNestedString(key: String) = getNested(key)?.jsonPrimitive?.content

  fun getNestedInt(key: String) = getNested(key)?.jsonPrimitive?.int
  //

  private fun toObject() = JsonObject(root)

  override fun toString() = toObject().toString()
}
