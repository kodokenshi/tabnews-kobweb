package me.kodokenshi.tabnewskobweb.api.v1.status

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.bodyOf
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Api("/v1/status")
suspend fun status(ctx: ApiContext) {
	
	ctx.res.body = bodyOf(buildJsonObject { put("chave", "valor") }.toString(), "application/json")
	
}