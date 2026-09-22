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
  private var onNoMatch: (ApiContext) -> Unit = {
    ctx.setResponse(
      response = MethodNotAllowedError(),
      status = HttpStatusCode.MethodNotAllowed,
    )
  }
  private var onError: (ApiContext, Throwable) -> Unit = { ctx, cause ->
    val cause = cause as? InternalServerError ?: InternalServerError(cause = cause)
    ctx.setResponse(
      response = cause,
      status = cause.statusCode,
    )
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
  fun onNoMatch(op: (ApiContext) -> Unit): RouteController {
    this.onNoMatch = op
    return this
  }

  fun onError(op: (ctx: ApiContext, cause: Throwable) -> Unit): RouteController {
    this.onError = op
    return this
  }

  fun routing(op: RouteBuilder.() -> Unit) {
    if (isAborted) return

    try {
      val router = RouteBuilder().apply(op)
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
    var getHandler: ((ApiContext) -> Unit)? = null
    var postHandler: ((ApiContext) -> Unit)? = null
    var putHandler: ((ApiContext) -> Unit)? = null
    var deleteHandler: ((ApiContext) -> Unit)? = null
    var headHandler: ((ApiContext) -> Unit)? = null
    var optionsHandler: ((ApiContext) -> Unit)? = null
    var patchHandler: ((ApiContext) -> Unit)? = null
    var queryHandler: ((ApiContext) -> Unit)? = null

    fun get(handler: (ApiContext) -> Unit) {
      getHandler = handler
    }

    fun post(handler: (ApiContext) -> Unit) {
      postHandler = handler
    }

    fun put(handler: (ApiContext) -> Unit) {
      putHandler = handler
    }

    fun delete(handler: (ApiContext) -> Unit) {
      deleteHandler = handler
    }

    fun head(handler: (ApiContext) -> Unit) {
      headHandler = handler
    }

    fun options(handler: (ApiContext) -> Unit) {
      optionsHandler = handler
    }

    fun patch(handler: (ApiContext) -> Unit) {
      patchHandler = handler
    }

    fun query(handler: (ApiContext) -> Unit) {
      queryHandler = handler
    }
  }
}
