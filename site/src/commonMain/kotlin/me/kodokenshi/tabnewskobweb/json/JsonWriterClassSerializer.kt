package me.kodokenshi.tabnewskobweb.json

import kotlinx.serialization.json.JsonElement

interface JsonWriterClassSerializer<T> {
  fun isInstanceOf(value: Any): Boolean

  fun toJsonElement(value: T): JsonElement

  fun fromJsonElement(jsonElement: JsonElement): T
}
