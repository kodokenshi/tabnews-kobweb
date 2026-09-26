package me.kodokenshi.tabnewskobweb.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.jvm.JvmName

fun String.toJsonOrNull() =
  try {
    toJson()
  } catch (_: Exception) {
    null
  }

fun String.toJson() = JsonReader.parse(this)

open class JsonReader(
  private val root: Map<String, JsonElement>,
) {
  //
	
  companion object {
    fun parse(string: String) = JsonReader(Json.parseToJsonElement(string).jsonObject)
  }
	
  //

  fun isEmpty() = root.isEmpty()

  fun isNotEmpty() = root.isNotEmpty()

  fun size() = root.size

  fun contains(key: String) = root.containsKey(key)
	
  //

  infix fun <T> read(reader: JsonReader.() -> T) = reader(this)

  fun getElement(key: String) = root[key]

  fun getString(key: String) = getElement(key)?.jsonPrimitive?.contentOrNull

  @JvmName("getStringOrElse")
  fun getString(
    key: String,
    orElse: String,
  ) = getString(key) ?: orElse

  fun getInt(key: String) = getElement(key)?.jsonPrimitive?.intOrNull

  @JvmName("getIntOrElse")
  fun getInt(
    key: String,
    orElse: Int,
  ) = getInt(key) ?: orElse

  fun getJson(key: String) = getElement(key)?.jsonObject?.let { JsonReader(it) }

  @JvmName("getJsonReturning")
  fun <T> getJson(
    key: String,
    nested: JsonReader.() -> T?,
  ) = getJson(key)?.let { nested(it) }

  inline fun <reified T> getClass(key: String): T? {
    val encodedClass = getElement(key) ?: return null
    return try {
      JsonWriter.customDeserialize(encodedClass) ?: Json.decodeFromJsonElement<T>(encodedClass)
    } catch (e: Exception) {
      throw IllegalArgumentException(
        "Falha ao desserializar a chave '$key' para o tipo ${T::class.simpleName}. " +
          "Certifique-se de que a classe esteja anotada com @Serializable.",
        e,
      )
    }
  }
	
  //

  @JvmName("stringGetString")
  fun String.getString() = this@JsonReader.getString(this)

  @JvmName("stringGetStringOrElse")
  fun String.getString(orElse: String) = this@JsonReader.getString(this, orElse)

  @JvmName("stringGetInt")
  fun String.getInt() = this@JsonReader.getInt(this)

  @JvmName("intGetIntOrElse")
  fun String.getInt(orElse: Int) = this@JsonReader.getInt(this, orElse)

  @JvmName("stringGetJson")
  fun <T> String.getJson(nested: JsonReader.() -> T?) =
    getJson(this)?.let {
      nested(it)
    }

  @JvmName("stringGetJsonReturning")
  fun <T> String.getJson(
    orElse: T,
    nested: JsonReader.() -> T?,
  ) = getJson(this)?.let {
    nested(it)
  } ?: orElse
	
  //

  fun toWriter() = JsonWriter(root.toMutableMap())
	
  //

  fun toObject() = JsonObject(root)

  override fun toString() = toObject().toString()
}
