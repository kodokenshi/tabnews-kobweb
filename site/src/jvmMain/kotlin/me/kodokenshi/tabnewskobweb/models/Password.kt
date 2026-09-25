package me.kodokenshi.tabnewskobweb.models

import me.kodokenshi.tabnewskobweb.infra.env
import org.mindrot.jbcrypt.BCrypt

object Password {
  private val ROUNDS by lazy {
    if (env.get("ENVIRONMENT", "prod").equals("dev", true)) 4 else 14
  }
  private val PEPPER by lazy { env.get("PASSWORD_PEPPER", "local_pepper") }

  fun hash(password: String): String {
    val salt = BCrypt.gensalt(ROUNDS)
    return BCrypt.hashpw(password + PEPPER, salt)
  }

  fun compare(
    providedPassword: String,
    storedPassword: String?,
  ): Boolean =
    !(providedPassword.isBlank() || storedPassword.isNullOrBlank()) &&
      BCrypt.checkpw(providedPassword + PEPPER, storedPassword)
}
