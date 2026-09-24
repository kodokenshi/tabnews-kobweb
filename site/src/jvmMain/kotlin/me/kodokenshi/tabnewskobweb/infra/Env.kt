package me.kodokenshi.tabnewskobweb.infra

import io.github.cdimascio.dotenv.dotenv

val env by lazy {
  dotenv {
    directory = "../"
    filename = ".env.development"
    ignoreIfMissing = true
  }
}
