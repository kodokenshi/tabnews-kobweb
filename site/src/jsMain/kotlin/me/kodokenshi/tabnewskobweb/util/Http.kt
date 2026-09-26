package me.kodokenshi.tabnewskobweb.util

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import me.kodokenshi.tabnewskobweb.json.toJsonArrayOrNull

val client by lazy { HttpClient() }

suspend fun fetchAPI(path: String) = client.get(path).bodyAsText().toJsonArrayOrNull()
