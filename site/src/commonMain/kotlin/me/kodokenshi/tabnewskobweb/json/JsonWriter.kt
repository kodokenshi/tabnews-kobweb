package me.kodokenshi.tabnewskobweb.json

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.jvm.JvmName

fun json(op: JsonWriter.() -> Unit) = JsonWriter(mutableMapOf()).apply(op)

fun jsonBuild(op: JsonWriter.() -> Unit) = json(op).toString()

open class JsonWriter(
  private val root: MutableMap<String, JsonElement>,
) : JsonReader(root) {
  //
	
  companion object {
    private val classSerializers = mutableListOf<JsonWriterClassSerializer<*>>()

    fun <T> registerClassSerializer(
      isInstanceOf: (Any) -> Boolean,
      toJsonElement: (T) -> JsonElement,
      fromJsonElement: (JsonElement) -> T,
    ) {
      classSerializers.add(
        object : JsonWriterClassSerializer<T> {
          override fun isInstanceOf(value: Any) = isInstanceOf(value)

          override fun toJsonElement(value: T) = toJsonElement(value)

          override fun fromJsonElement(jsonElement: JsonElement) = fromJsonElement(jsonElement)
        },
      )
    }

    fun registerClassSerializer(customSerializer: JsonWriterClassSerializer<*>) {
      classSerializers.add(customSerializer)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> customSerialize(value: T) =
      if (value == null) {
        null
      } else {
        (
          classSerializers.firstOrNull {
            it.isInstanceOf(value)
          } as? JsonWriterClassSerializer<T>
        )?.toJsonElement(value)
      }

    @Suppress("UNCHECKED_CAST")
    fun <T> customDeserialize(value: JsonElement): T? =
      (
        classSerializers.firstOrNull {
          it.isInstanceOf(value)
        } as? JsonWriterClassSerializer<T>
      )?.fromJsonElement(value)

    fun parse(string: String) = JsonWriter(Json.parseToJsonElement(string).jsonObject.toMutableMap())
  }
	
  //

  infix fun write(writer: JsonWriter.() -> Unit) = writer(this)

  infix fun spread(json: JsonWriter) {
    root.putAll(json.root)
  }

  infix fun spread(json: JsonReader) = spread(json.toWriter())
	
  //

  infix fun String.eq(value: Any?) {
    root[this] = value.toJsonElement()
  }

  fun set(
    key: String,
    value: Any?,
  ) = key eq value

  @JvmName("stringSet")
  infix fun String.set(value: Any?) = set(this, value)

  infix fun String.to(value: Any?) = eq(value)

  infix operator fun String.invoke(nest: JsonWriter.() -> Unit) = eq(JsonWriter(mutableMapOf()).apply(nest))

  //

  private fun Any?.toJsonElement(): JsonElement =
		
    when (this) {
      null -> {
        JsonNull
      }

      is JsonReader -> {
        this.toObject()
      }

      is JsonArrayReader -> {
        this.toArray()
      }
			
      is JsonElement -> {
        this
      }
			
      is Boolean -> {
        JsonPrimitive(this)
      }
			
      is Number -> {
        JsonPrimitive(this)
      }
			
      is String -> {
        JsonPrimitive(this)
      }
			
      is Map<*, *> -> {
        JsonObject(
          this.entries.associate { (key, value) ->
            Pair(key.toString(), (value.toJsonElement()))
          },
        )
      }
			
      is List<*> -> {
        JsonArray(this.map { it.toJsonElement() })
      }
			
      else -> {
        try {
          customSerialize(this) ?: Json.encodeToJsonElement(this)
        } catch (e: Exception) {
          throw IllegalArgumentException(
            "Tipo não suportado ou objeto não serializável: ${this::class.simpleName}. " +
              "Certifique-se de usar tipos primitivos, Map, List ou anotar a classe com @Serializable.",
            e,
          )
        }
      }
    }
}
