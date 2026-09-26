package me.kodokenshi.tabnewskobweb.api.v1.users

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.text
import io.ktor.http.HttpStatusCode
import me.kodokenshi.tabnewskobweb.infra.createRouter
import me.kodokenshi.tabnewskobweb.infra.setResponse
import me.kodokenshi.tabnewskobweb.json.toJsonArrayOrNull
import me.kodokenshi.tabnewskobweb.models.User

@Api("/v1/users")
suspend fun users(ctx: ApiContext) {
  createRouter(ctx)
    .routing {
      post {
        ctx.setResponse(
          response =
            User.create(
              ctx.req.body
                ?.text()
                ?.toJsonArrayOrNull()
                ?.firstOrNull()
                ?.toWriter(),
            ),
          status = HttpStatusCode.Created,
        )
      }
    }
}
