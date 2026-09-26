package me.kodokenshi.tabnewskobweb.api.v1.users.username

import com.varabyte.kobweb.api.Api
import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.text
import me.kodokenshi.tabnewskobweb.infra.createRouter
import me.kodokenshi.tabnewskobweb.infra.setResponse
import me.kodokenshi.tabnewskobweb.json.toJsonArrayOrNull
import me.kodokenshi.tabnewskobweb.models.User

@Api("/v1/users/{username}")
suspend fun users(ctx: ApiContext) {
  createRouter(ctx)
    .routing {
      get {
        val username = ctx.req.params["username"]
        val userFound = User.findOneByUsername(username)
        ctx.setResponse(userFound)
      }
      patch {
        val username = ctx.req.params["username"]
        val updatedUser =
          User.update(
            username,
            ctx.req.body
              ?.text()
              ?.toJsonArrayOrNull()
              ?.firstOrNull()
              ?.toWriter(),
          )
        ctx.setResponse(updatedUser)
      }
    }
}
