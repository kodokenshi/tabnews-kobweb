package me.kodokenshi.tabnewskobweb.json

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlin.jvm.JvmName

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

fun String.parseJsonList() = Json.parseList(this) ?: Json.parse(this)?.let { listOf(it) }

fun Map<String, JsonElement>.parseJson() = Json.parse(this)

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
  private fun insert(
    key: String,
    value: JsonElement,
  ) {
    root[key] = value
  }

  fun put(
    key: String,
    json: Json,
  ) = insert(key, json.toObject())

  @JvmName("put0")
  fun put(
    key: String,
    json: List<Json>,
  ) = insert(key, JsonArray(json.map { it.toObject() }))

  fun put(
    key: String,
    json: Json.() -> Unit,
  ) = insert(key, json(json).toObject())

  fun put(
    key: String,
    value: Boolean?,
  ) = insert(key, JsonPrimitive(value))

  @JvmName("put1")
  fun put(
    key: String,
    value: List<Boolean>?,
  ) = insert(key, JsonArray(value?.map { JsonPrimitive(it) }.orEmpty()))

  fun put(
    key: String,
    value: Number?,
  ) = insert(key, JsonPrimitive(value))

  @JvmName("put2")
  fun put(
    key: String,
    value: List<Number>?,
  ) = insert(key, JsonArray(value?.map { JsonPrimitive(it) }.orEmpty()))

  fun put(
    key: String,
    value: String?,
  ) = insert(key, JsonPrimitive(value))

  @JvmName("put3")
  fun put(
    key: String,
    value: List<String>?,
  ) = insert(key, JsonArray(value?.map { JsonPrimitive(it) }.orEmpty()))

  fun put(
    key: String,
    value: JsonElement,
  ) = insert(key, value)

  @JvmName("put4")
  fun put(
    key: String,
    value: List<JsonElement>?,
  ) = insert(key, JsonArray(value.orEmpty()))

  fun putNull(key: String) = insert(key, JsonNull)

  fun remove(key: String) = root.remove(key)
  //

  //
  private fun putRootNested(
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
  ) = putRootNested(key, json.toObject())

  fun putNested(
    key: String,
    json: Json.() -> Unit,
  ) = putRootNested(key, json(json).toObject())

  fun putNested(
    key: String,
    value: Boolean?,
  ) = putRootNested(key, JsonPrimitive(value))

  @JvmName("putNested0")
  fun putNested(
    key: String,
    value: List<Boolean>?,
  ) = putRootNested(key, JsonArray(value?.map { JsonPrimitive(it) }.orEmpty()))

  fun putNested(
    key: String,
    value: Number?,
  ) = putRootNested(key, JsonPrimitive(value))

  @JvmName("putNested1")
  fun putNested(
    key: String,
    value: List<Number>?,
  ) = putRootNested(key, JsonArray(value?.map { JsonPrimitive(it) }.orEmpty()))

  fun putNested(
    key: String,
    value: String?,
  ) = putRootNested(key, JsonPrimitive(value))

  @JvmName("putNested2")
  fun putNested(
    key: String,
    value: List<String>?,
  ) = putRootNested(key, JsonArray(value?.map { JsonPrimitive(it) }.orEmpty()))

  fun putNested(
    key: String,
    value: JsonElement,
  ) = putRootNested(key, value)

  @JvmName("putNested3")
  fun putNested(
    key: String,
    value: List<JsonElement>?,
  ) = putRootNested(key, JsonArray(value.orEmpty()))

  fun putNestedNull(key: String) = putRootNested(key, JsonNull)
  //

  //
  private fun getRootElement(key: String) = root[key]

  fun getJson(key: String) = getRootElement(key)?.jsonObject?.parseJson()

  fun getJsonList(key: String) = getRootElement(key)?.jsonArray?.mapNotNull { it.jsonObject.parseJson() }

  fun getBoolean(key: String) = getRootElement(key)?.jsonPrimitive?.booleanOrNull

  fun getBooleanList(key: String) = getRootElement(key)?.jsonArray?.mapNotNull { it.jsonPrimitive.booleanOrNull }

  fun getInt(key: String) = getRootElement(key)?.jsonPrimitive?.intOrNull

  fun getIntList(key: String) = getRootElement(key)?.jsonArray?.mapNotNull { it.jsonPrimitive.intOrNull }

  fun getLong(key: String) = getRootElement(key)?.jsonPrimitive?.longOrNull

  fun getLongList(key: String) = getRootElement(key)?.jsonArray?.mapNotNull { it.jsonPrimitive.longOrNull }

  fun getDouble(key: String) = getRootElement(key)?.jsonPrimitive?.doubleOrNull

  fun getDoubleList(key: String) = getRootElement(key)?.jsonArray?.mapNotNull { it.jsonPrimitive.doubleOrNull }

  fun getFloat(key: String) = getRootElement(key)?.jsonPrimitive?.floatOrNull

  fun getFloatList(key: String) = getRootElement(key)?.jsonArray?.mapNotNull { it.jsonPrimitive.floatOrNull }

  fun getString(key: String) = getRootElement(key)?.jsonPrimitive?.contentOrNull

  fun getStringList(key: String) = getRootElement(key)?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }

  fun getElement(key: String) = getRootElement(key)

  fun getElementList(key: String) = getRootElement(key)?.jsonArray?.toList()
  //

  //
  private fun getRootNestedElement(key: String): JsonElement? {
    val path = key.split('.')
    var currentMap: Map<String, JsonElement> = root

    path.forEachIndexed { index, segment ->

      if (index == path.lastIndex) return currentMap[segment]
      currentMap = (currentMap[segment] as? JsonObject) ?: return null
    }

    return null
  }

  fun getNestedJson(key: String) = getRootNestedElement(key)?.jsonObject?.parseJson()

  fun getNestedJsonList(key: String) = getRootNestedElement(key)?.jsonArray?.mapNotNull { it.jsonObject.parseJson() }

  fun getNestedBoolean(key: String) = getRootNestedElement(key)?.jsonPrimitive?.booleanOrNull

  fun getNestedBooleanList(key: String) = getRootNestedElement(key)?.jsonArray?.mapNotNull { it.jsonPrimitive.booleanOrNull }

  fun getNestedInt(key: String) = getRootNestedElement(key)?.jsonPrimitive?.intOrNull

  fun getNestedIntList(key: String) = getRootNestedElement(key)?.jsonArray?.mapNotNull { it.jsonPrimitive.intOrNull }

  fun getNestedLong(key: String) = getRootNestedElement(key)?.jsonPrimitive?.longOrNull

  fun getNestedLongList(key: String) = getRootNestedElement(key)?.jsonArray?.mapNotNull { it.jsonPrimitive.longOrNull }

  fun getNestedDouble(key: String) = getRootNestedElement(key)?.jsonPrimitive?.doubleOrNull

  fun getNestedDoubleList(key: String) = getRootNestedElement(key)?.jsonArray?.mapNotNull { it.jsonPrimitive.doubleOrNull }

  fun getNestedFloat(key: String) = getRootNestedElement(key)?.jsonPrimitive?.floatOrNull

  fun getNestedFloatList(key: String) = getRootNestedElement(key)?.jsonArray?.mapNotNull { it.jsonPrimitive.floatOrNull }

  fun getNestedString(key: String) = getRootNestedElement(key)?.jsonPrimitive?.contentOrNull

  fun getNestedStringList(key: String) = getRootNestedElement(key)?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }

  fun getNestedElement(key: String) = getRootNestedElement(key)

  fun getNestedElementList(key: String) = getRootNestedElement(key)?.jsonArray?.toList()
  //

  private fun toObject() = JsonObject(root)

  override fun toString() = toObject().toString()
}
