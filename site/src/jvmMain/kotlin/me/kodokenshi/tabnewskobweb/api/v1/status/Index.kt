package me.kodokenshi.tabnewskobweb.api.v1.status

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.bodyOf

@Api("/v1/status")
suspend fun status(ctx: ApiContext) {
	
	ctx.res.body = bodyOf("{ \"response\": \"os alunos do curso.dev são pessoas acima da média\" }", "text/json")
	
}