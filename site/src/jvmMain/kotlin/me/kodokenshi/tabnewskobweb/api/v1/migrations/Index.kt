package me.kodokenshi.tabnewskobweb.api.v1.migrations

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.bodyOf
import me.kodokenshi.tabnewskobweb.json.buildJson

@Api("/v1/migrations")
suspend fun status(ctx: ApiContext) {
	
	try {
		
		ctx.res.body =
			bodyOf(
				arrayOf(buildJson {
					put("test", 1)
				}).contentToString(),
				"application/json"
			)
		
	} catch (t: Throwable) {
		
		t.printStackTrace()
		throw t
		
	}
	
}