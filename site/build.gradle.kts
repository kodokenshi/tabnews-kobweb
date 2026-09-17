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
  id("io.github.ben-manes.versions") version "0.63.1"
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
      implementation("io.ktor:ktor-client-core:3.6.0")
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
      implementation("io.ktor:ktor-client-js:3.6.0")
    }
		
    jvmMain.dependencies {
      implementation("org.junit.jupiter:junit-jupiter-api:6.1.3")
			
      compileOnly(libs.kobweb.api) // Provided by Kobweb backend at runtime
      implementation("io.ktor:ktor-client-cio:3.6.0")
			
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

tasks.register("runDev") {
  description = "Start services and server."
  doLast {
    runBlocking {
      try {
        prepareAndRunProcess("Start secondary services", arrayOf("site:servicesUp"))
        prepareAndRunProcess("Start server", arrayOf("site:kobwebStart", "-t"))
      } finally {
        prepareAndRunProcess("Stop server", arrayOf("site:kobwebStop"))
        prepareAndRunProcess("Stop secondary services", arrayOf("site:servicesStop"))
      }
    }
  }
}
tasks.register("stopDev") {
  description = "Stop server and services."
  doLast {
    runBlocking {
      prepareAndRunProcess("Stop server", arrayOf("site:kobwebStop"))
      prepareAndRunProcess("Stop secondary services", arrayOf("site:servicesStop"))
    }
  }
}
tasks.register("runTests") {
  description = "Start services and server, execute tests, then stop on complete."
	
  doLast {
		
    var anyFailed = false
		
    val time =
      measureTime {
        runBlocking {
          try {
            listOf(
              "Start secondary services" to arrayOf("site:servicesUp"),
              "Start server" to arrayOf("site:kobwebStart"),
              "Start battery of tests" to arrayOf("site:allTests", "-x", ":site:jsBrowserTest", "--rerun-tasks"),
            ).forEach { (name, process) ->
              val failed = !prepareAndRunProcess(name, process)
              if (failed) anyFailed = true
            }
          } finally {
            prepareAndRunProcess("Stop server", arrayOf("site:kobwebStop"))
            prepareAndRunProcess("Stop secondary services", arrayOf("site:servicesStop"))
          }
        }
      }
		
    println("\u001B[37m| ------------------------------\u001B[0m")
    println(
      "\u001B[37m| Everything took: ${time.toComponents { seconds, nanoseconds ->
        val millis = nanoseconds / 1_000_000
        "$seconds sec, ${millis.toString().padStart(3, '0')} ms"
      }}\u001B[0m",
    )
    println("\u001B[37m| ------------------------------\u001B[0m")
		
    check(!anyFailed)
  }
}

val servicesUp =
  tasks.register<Exec>("servicesUp") {
    description = "Start services."
    commandLine("docker", "compose", "-f", "../infra/compose.yaml", "up", "-d")
  }
val servicesStop =
  tasks.register<Exec>("servicesStop") {
    description = "Stop services temporarily."
    commandLine("docker", "compose", "-f", "../infra/compose.yaml", "stop")
  }
val servicesDown =
  tasks.register<Exec>("servicesDown") {
    description = "Stop services."
    commandLine("docker", "compose", "-f", "../infra/compose.yaml", "down")
  }

tasks.register<Exec>("sqllintCheck") {
  description = "Run '.sql' files lint check."
	
  val sqlFiles =
    fileTree(rootDir) {
      include("**/*.sql")
      exclude("**/build/**", "**/.gradle/**")
    }.files
	
  executable = "sqlfluff"
  args = mutableListOf("lint") + sqlFiles.map { it.absolutePath }
	
  inputs.files(sqlFiles)
	
  isIgnoreExitValue = false
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
tasks.register("migration") {
  // ./gradlew migration -Pname=""
	
  group = "database"
  description = "Create a new migration file based on current timestamp."
	
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

// ---------------------------------------------------------------------------------------------------------------------

private val isWindows = System.getProperty("os.name").lowercase().contains("win")
private val gradlewCommand = if (isWindows) "gradlew.bat" else "./gradlew"

private suspend fun runProcess(args: Array<String>) =
  coroutineScope {
    println("\u001B[37mRunning '${args.joinToString(" ")}'...\u001B[0m")
		
    var ret = -1
    val time =
      measureTime {
        val process =
          ProcessBuilder(listOf(gradlewCommand) + args)
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
      "\u001B[37mThis process took: ${time.toComponents { seconds, nanoseconds ->
        val millis = nanoseconds / 1_000_000
        "$seconds sec, ${millis.toString().padStart(3, '0')} ms"
      }}\u001B[0m",
    )
		
    ret
  }

/**Returns `true` if process exit code is `0`.*/
private suspend fun prepareAndRunProcess(
  name: String,
  process: Array<String>,
): Boolean {
  val exitCode = runProcess(process)
  println(
    "$name exited with: $exitCode".let {
      if (exitCode != 0) {
        "\u001B[31m\u001B[1m$it\u001B[0m"
      } else {
        "\u001B[32m$it\u001B[0m"
      }
    },
  )
  return exitCode == 0
}
