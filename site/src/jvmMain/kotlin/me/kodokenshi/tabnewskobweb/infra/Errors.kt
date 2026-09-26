package me.kodokenshi.tabnewskobweb.infra

import io.ktor.http.HttpStatusCode
import me.kodokenshi.tabnewskobweb.json.jsonBuild

open class InternalServerError(
  message: String = "Um erro interno não esperado aconteceu.",
  cause: Throwable? = null,
  val action: String = "Entre em contato com o suporte.",
  val statusCode: HttpStatusCode = HttpStatusCode.InternalServerError,
) : Throwable(message, cause) {
  override fun toString() =
    jsonBuild {
      "name" eq this@InternalServerError.javaClass.simpleName
      "message" eq message
      "action" eq action
      "status_code" eq statusCode.value
    }
}

class MethodNotAllowedError :
  InternalServerError(
    message = "Método não permitido para este endpoint.",
    action = "Verifique se o método HTTP enviado é válido para este endpoint.",
    statusCode = HttpStatusCode.MethodNotAllowed,
  )

class ServiceError(
  message: String = "Serviço indisponível no momento.",
  cause: Throwable?,
) : InternalServerError(
    message = message,
    action = "Verifique se o serviço está disponível.",
    statusCode = HttpStatusCode.ServiceUnavailable,
    cause = cause,
  )

class ValidationError(
  message: String = "Um erro de validação ocorreu.",
  action: String = "Ajuste os dados enviados e tente novamente.",
) : InternalServerError(
    message = message,
    action = action,
    statusCode = HttpStatusCode.BadRequest,
  )

class NotFoundError(
  message: String = "Não foi possível encontrar este recurso.",
  action: String = "Verifique os parâmetros enviados e tente novamente.",
) : InternalServerError(
    message = message,
    action = action,
    statusCode = HttpStatusCode.NotFound,
  )
