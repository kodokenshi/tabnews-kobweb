package me.kodokenshi.tabnewskobweb.api

import com.varabyte.kobweb.api.init.InitApi
import com.varabyte.kobweb.api.init.InitApiContext
import me.kodokenshi.tabnewskobweb.database.migrations.defaultMigrations
import me.kodokenshi.tabnewskobweb.infra.InternalServerError

@InitApi
fun init(ctx: InitApiContext) {
  if (System.getenv("KOBWEB_BUILD_TYPE") == "prod") return
	
  try {
    defaultMigrations().migrate()
  } catch (e: Throwable) {
    ctx.logger.error(InternalServerError(cause = e).toString())
  }
}
