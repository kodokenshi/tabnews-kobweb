@file:Suppress("ktlint:standard:filename", "detekt:MatchingDeclarationName")

package me.kodokenshi.tabnewskobweb.infra

import io.ktor.http.HttpStatusCode
import me.kodokenshi.tabnewskobweb.json.buildJson

open class InternalServerError(
  message: String = "Um erro interno não esperado aconteceu.",
  cause: Throwable,
  val action: String = "Entre em contato com o suporte.",
  val statusCode: HttpStatusCode = HttpStatusCode.InternalServerError,
) : Throwable(message, cause) {
  override fun toString() =
    buildJson {
      put("name", this@InternalServerError.javaClass.simpleName)
      put("message", message)
      put("action", action)
      put("status_code", statusCode.value)
    }
}
