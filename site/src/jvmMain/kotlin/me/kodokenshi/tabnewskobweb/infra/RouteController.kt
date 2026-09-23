package me.kodokenshi.tabnewskobweb.infra

import com.varabyte.kobweb.api.ApiContext
import com.varabyte.kobweb.api.http.HttpMethod
import com.varabyte.kobweb.api.http.bodyOf
import io.ktor.http.HttpStatusCode

fun createRouter(ctx: ApiContext) = RouteController(ctx)

fun ApiContext.setResponse(
  response: Any? = null,
  contentType: String = "application/json",
  status: HttpStatusCode = HttpStatusCode.OK,
) {
  if (response != null) res.body = bodyOf(response.toString(), contentType)
  res.status = status.value
}

class RouteController(
  val ctx: ApiContext,
) {
  private var onNoMatch: suspend (ApiContext) -> Unit = {
    ctx.setResponse(
      response = MethodNotAllowedError(),
      status = HttpStatusCode.MethodNotAllowed,
    )
  }
  private var onError: suspend (ApiContext, Throwable) -> Unit = { ctx, cause ->
    val cause = cause as? InternalServerError ?: InternalServerError(cause = cause)
    ctx.setResponse(
      response = cause,
      status = cause.statusCode,
    )
    cause.printStackTrace()
  }
  private var isAborted = false

	/*fun requireAuth(tokenHeader: String = "Authorization"): HandlerChain {
        if (isAborted) return this
        val authHeader = ctx.req.headers[tokenHeader]
        if (authHeader.isNullOrBlank()) {
            ctx.res.status = 401
            // escrever diretamente no fluxo de saída ou definir status
            isAborted = true
        }
        return this
    }*/
  fun onNoMatch(op: suspend (ApiContext) -> Unit): RouteController {
    this.onNoMatch = op
    return this
  }

  fun onError(op: suspend (ctx: ApiContext, cause: Throwable) -> Unit): RouteController {
    this.onError = op
    return this
  }

  suspend fun routing(op: suspend RouteBuilder.() -> Unit) {
    if (isAborted) return

    try {
      val router = RouteBuilder()
      op(router)
      val matchingHandler =
        when (ctx.req.method) {
          HttpMethod.GET -> router.getHandler
          HttpMethod.POST -> router.postHandler
          HttpMethod.PUT -> router.putHandler
          HttpMethod.DELETE -> router.deleteHandler
          HttpMethod.HEAD -> router.headHandler
          HttpMethod.OPTIONS -> router.optionsHandler
          HttpMethod.PATCH -> router.patchHandler
          HttpMethod.QUERY -> router.queryHandler
        }
			
      if (matchingHandler != null) {
        matchingHandler(ctx)
      } else {
        onNoMatch(ctx)
      }
    } catch (cause: Throwable) {
      onError(ctx, cause)
    }
  }

  class RouteBuilder {
    var getHandler: (suspend (ApiContext) -> Unit)? = null
    var postHandler: (suspend (ApiContext) -> Unit)? = null
    var putHandler: (suspend (ApiContext) -> Unit)? = null
    var deleteHandler: (suspend (ApiContext) -> Unit)? = null
    var headHandler: (suspend (ApiContext) -> Unit)? = null
    var optionsHandler: (suspend (ApiContext) -> Unit)? = null
    var patchHandler: (suspend (ApiContext) -> Unit)? = null
    var queryHandler: (suspend (ApiContext) -> Unit)? = null

    fun get(handler: suspend (ApiContext) -> Unit) {
      getHandler = handler
    }

    fun post(handler: suspend (ApiContext) -> Unit) {
      postHandler = handler
    }

    fun put(handler: suspend (ApiContext) -> Unit) {
      putHandler = handler
    }

    fun delete(handler: suspend (ApiContext) -> Unit) {
      deleteHandler = handler
    }

    fun head(handler: suspend (ApiContext) -> Unit) {
      headHandler = handler
    }

    fun options(handler: suspend (ApiContext) -> Unit) {
      optionsHandler = handler
    }

    fun patch(handler: suspend (ApiContext) -> Unit) {
      patchHandler = handler
    }

    fun query(handler: suspend (ApiContext) -> Unit) {
      queryHandler = handler
    }
  }
}
