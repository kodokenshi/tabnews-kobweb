import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Properties
import kotlin.time.measureTime

buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath("org.postgresql:postgresql:42.7.13")
  }
}

plugins {
  id("io.gitlab.arturbosch.detekt") version "1.23.8"
  id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.kobweb.application)
  alias(libs.plugins.kobwebx.markdown)
}

ktlint {
  android.set(false)
  outputToConsole.set(true)
  coloredOutput.set(true)
  ignoreFailures.set(false)
  enableExperimentalRules.set(false)
  filter {
    exclude { exclude ->
      sequenceOf("build", "generated").any {
        exclude.file.absolutePath.contains("${File.separator}$it${File.separator}")
      }
    }
  }
}
detekt {
  buildUponDefaultConfig = true
  config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
  autoCorrect = true
  source.setFrom(
    files(
      "src/commonMain/kotlin",
      "src/commonTest/kotlin",
      "src/jsMain/kotlin",
      "src/jsTest/kotlin",
      "src/jvmMain/kotlin",
      "src/jvmTest/kotlin",
    ),
  )
}

fun getEnvVar(
  key: String,
  defaultValue: String = "",
): String {
  val envFile = File(projectDir, ".env.development")
  if (envFile.exists()) {
    val properties = Properties()
    envFile.inputStream().use { properties.load(it) }
    val value = properties.getProperty(key)
    if (value != null) return value
  }
  return System.getenv(key) ?: defaultValue
}

group = "me.kodokenshi.tabnewskobweb"
version = "1.0-SNAPSHOT"

kobweb {
  app {
  }
}

kotlin {
	
  js {
    browser {
      testTask {
        useKarma {
          useChromeHeadless()
        }
      }
    }
  }
	
  configAsKobwebApplication("tabnewskobweb", includeServer = true)
	
  sourceSets {
		
    commonMain.dependencies {
			
      implementation(kotlin("test"))
      implementation("io.ktor:ktor-client-core:3.5.0")
      implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
      implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
      implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")
    }
		
    jsMain.dependencies {
			
      implementation(libs.compose.runtime)
      implementation(libs.compose.html.core)
      implementation(libs.kobweb.core)
      implementation(libs.kobweb.silk)
      // This default template uses built-in SVG icons, but what's available is limited.
      // Uncomment the following if you want access to a large set of font-awesome icons:
      // implementation(libs.silk.icons.fa)
      implementation(libs.kobwebx.markdown)
      implementation("io.ktor:ktor-client-js:3.5.0")
    }
		
    jvmMain.dependencies {
			
      implementation("org.junit.jupiter:junit-jupiter-api:6.1.3")
			
      compileOnly(libs.kobweb.api) // Provided by Kobweb backend at runtime
      implementation("io.ktor:ktor-client-cio:3.5.0")
			
      implementation("org.jetbrains.exposed:exposed-core:1.5.0")
      implementation("org.jetbrains.exposed:exposed-dao:1.5.0")
      implementation("org.jetbrains.exposed:exposed-jdbc:1.5.0")
// 			implementation("org.jetbrains.exposed:exposed-migration-core:1.5.0")
// 			implementation("org.jetbrains.exposed:exposed-migration-jdbc:1.5.0")
			
      implementation("org.postgresql:postgresql:42.7.13")
// 			implementation("com.zaxxer:HikariCP:7.1.0")
    }
  }
}

tasks.withType<Test> {
	
  useJUnitPlatform()
  testLogging {
		
    showStandardStreams = true
    showExceptions = true
    showCauses = true
    showStackTraces = true
  }
}

val servicesStop =
  tasks.register<Exec>("servicesStop") {
	
    description = "Pausa temporariamente os serviços secundários"
    commandLine("docker", "compose", "-f", "../infra/compose.yaml", "stop")
  }
val servicesDown =
  tasks.register<Exec>("servicesDown") {
	
    description = "Derruba os serviços secundários"
    commandLine("docker", "compose", "-f", "../infra/compose.yaml", "down")
  }
val servicesUp =
  tasks.register<Exec>("servicesUp") {
	
    description = "Sobe os serviços secundários"
    commandLine("docker", "compose", "-f", "../infra/compose.yaml", "up", "-d")
  }
tasks.register("runDev") {
	
  description = "Inicia os serviços e o servidor."
  dependsOn(servicesUp, "kobwebStart")
}
tasks.register("stopDev") {
	
  description = "Derruba o servidor e os serviços"
  dependsOn("kobwebStop", servicesDown)
}
private val ignoredLines =
  sequenceOf(
    "STANDARD_OUT",
    "STANDARD_ERROR",
    "SLF4J(W)",
    "> Task",
    "[jvm] FAILED",
    "me.kodokenshi.tabnewskobweb.tests.TestContext\$TestException at TestContext.kt:",
    "There were failing tests",
    "FAILURE: Build failed",
    "Execution failed for task",
    "* Try:",
    "> Run with",
    "> Get more help at",
    "* What went wrong:",
    "BUILD SUCCESSFUL",
    "warning workspace-aggregator",
  )
tasks.register("runTests") {
	
  description = "Inicia os serviços e o servidor, então executa os testes e derruba tudo."
	
  doLast {
		
    var anyFailed = false
		
    val time =
      measureTime {
			
        runBlocking {
          val isWindows = System.getProperty("os.name").lowercase().contains("win")
          val gradlewCommand = if (isWindows) "gradlew.bat" else "./gradlew"

          suspend fun runProcess(vararg args: String) =
            coroutineScope {
              println("\u001B[37mRodando '${args.joinToString(" ")}'...\u001B[0m")
					
              var ret = -1
              val time =
                measureTime {
                  val process =
                    ProcessBuilder(gradlewCommand, *args)
                      .directory(project.rootDir)
                      .redirectErrorStream(true)
                      .start()
						
                  launch(Dispatchers.IO) {
                    process.inputStream.bufferedReader().use { reader ->
								
                      var line: String?
                      while (reader.readLine().also { line = it } != null) {
                        if (!line.isNullOrBlank() && ignoredLines.none { line.contains(it, true) }) {
                          println("[${args.first()}] ${line.trim()}")
                        }
                      }
                    }
                  }.join()
						
                  ret = process.waitFor()
                }
					
              println(
                "\u001B[37mEste processo levou: ${time.toComponents { seconds, nanoseconds ->
                  val millis = nanoseconds / 1_000_000
                  "$seconds sec, ${millis.toString().padStart(3, '0')} ms"
                }}\u001B[0m",
              )
					
              ret
            }

          suspend fun process(
            name: String,
            vararg process: String,
          ) {
            println(
              "$name saiu com: ${runProcess(*process)}".let {
                if (!it.endsWith("0")) {
                  "\u001B[31m\u001B[1m$it\u001B[0m".also { anyFailed = true }
                } else {
                  "\u001B[32m$it\u001B[0m"
                }
              },
            )
          }
				
          try {
					
            process("Subir serviços secundários", "site:servicesUp")
            process("Subir servidor", "site:kobwebStart")
            process("Subir bateria de testes", "site:allTests", "-x", ":site:jsBrowserTest", "--rerun-tasks")
          } finally {
					
            process("Derrubar servidor", "site:kobwebStop")
            process("Derrubar serviços secundários", "site:servicesStop")
          }
        }
      }
		
    println("\u001B[37m| ------------------------------\u001B[0m")
    println(
      "\u001B[37m| Tudo levou: ${time.toComponents { seconds, nanoseconds ->
        val millis = nanoseconds / 1_000_000
        "$seconds sec, ${millis.toString().padStart(3, '0')} ms"
      }}\u001B[0m",
    )
    println("\u001B[37m| ------------------------------\u001B[0m")
		
    if (anyFailed) throw Exception()
  }
}
tasks.register("migration") {
  // ./gradlew migration -Pname=""
	
  group = "database"
  description = "Cria um novo arquivo de migração SQL com base no timestamp atual."
	
  val nameProvider = providers.gradleProperty("name").orElse("migration")
  val rootDir = layout.settingsDirectory.asFile
	
  doLast {
		
    val migrationName = nameProvider.get()
		
    val sanitizedName = migrationName.lowercase().replace(Regex("[^a-z0-9_]"), "-")
		
    val fileName = "V${System.currentTimeMillis()}__$sanitizedName.sql"
		
    val migrationDir = File("$rootDir/infra", "migrations")
    if (!migrationDir.exists()) migrationDir.mkdirs()
		
    val migrationFile = File(migrationDir, fileName)
    if (!migrationFile.exists()) {
			
      migrationFile.createNewFile()
      println("Migration created: ${migrationFile.toURI()}")
    } else {
      println("Migration ${migrationFile.toURI()} already exists!")
    }
  }
}
