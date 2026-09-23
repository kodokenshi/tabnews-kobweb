import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Properties
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.DurationUnit
import kotlin.time.measureTime
import kotlin.time.toDuration

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
      implementation("io.ktor:ktor-client-core:3.6.0")
      implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.8.0")
      implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.11.0")
      implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
      implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")
    }
    commonTest.dependencies {
      implementation(kotlin("test"))
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
    jvmTest.dependencies {
      implementation("org.junit.jupiter:junit-jupiter-api:6.1.3")
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
  addTestListener(
    object : TestListener {
     /* override fun beforeSuite(suite: TestDescriptor) {
        if (suite.parent != null) return
        println(".")
      }*/

      override fun beforeTest(suite: TestDescriptor) {
        if (suite.parent == null) return
        println(".")
      }

      override fun afterSuite(
        suite: TestDescriptor,
        result: TestResult,
      ) {
        if (suite.parent != null) return

        val okText = "\u001B[32m\u001B[1m"
        val failText = "\u001B[31m\u001B[1m"
        val reset = "\u001B[0m"

        println(
          buildString {
            append(".\n. Tests: ")
            append(
              buildString {
                val failed = result.failedTestCount
                val passed = result.successfulTestCount
                val total = result.testCount
                val skipped = result.skippedTestCount

                if (failed > 0) append("$failText$failed failed$reset")
                if (passed > 0) {
                  if (isNotBlank()) append(", ")
                  append("$okText$passed passed$reset")
                }

                if (isNotBlank()) append(", ")
                append("$total total")

                if (skipped > 0) append(", $skipped skipped")
              },
            )
            append(
              "\n. Time:  ${(result.endTime - result.startTime).toDuration(DurationUnit.MILLISECONDS).toComponents {
                seconds,
                nanoseconds,
                ->
                val millis = nanoseconds / 1_000_000
                "$seconds.${millis.toString().padStart(3, '0')} s"
              }}\n.",
            )
          },
        )
      }
    },
  )
  systemProperty("verbose", System.getProperty("verbose", "false"))
}

tasks.register("servicesWaitDatabase") {
  description = "Wait for Postgres accept new connections."
  doLast {
    runBlocking {
      println("🔴 Waiting Postgres accept new connections...")
      val process =
        ProcessBuilder("docker", "exec", "postgres-dev", "pg_isready", "--host", "localhost")
          .directory(project.rootDir)

      while (process.start().waitFor() != 0) delay(50.milliseconds)

      println("🟢 Postgres ready.")
    }
  }
}
tasks.register("runDev") {
  description = "Start services and server."
  doLast {
    runBlocking {
      try {
        prepareAndRunProcess("Start secondary services", arrayOf("site:servicesUp", "--no-daemon"))
        prepareAndRunProcess("Services wait database", arrayOf("site:servicesWaitDatabase", "--no-daemon"))
        prepareAndRunProcess("Start server", arrayOf("site:kobwebStart", "-t", "--no-daemon"), false)
      } finally {
        prepareAndRunProcess("Stop server", arrayOf("site:kobwebStop", "--no-daemon"))
        prepareAndRunProcess("Stop secondary services", arrayOf("site:servicesStop", "--no-daemon"))
      }
    }
  }
}
tasks.register("stopDev") {
  description = "Stop server and services."
  doLast {
    runBlocking {
      prepareAndRunProcess("Stop server", arrayOf("site:kobwebStop", "--no-daemon"))
      prepareAndRunProcess("Stop secondary services", arrayOf("site:servicesStop", "--no-daemon"))
    }
  }
}
tasks.register("tests") {
  description = "Execute tests"
  doLast {
    runBlocking {
      prepareAndRunProcess(
        "Start battery of tests",
        arrayOf(
          "site:allTests",
          "-Dverbose=${project.findProperty("verbose") ?: "false"}",
          "-x",
          ":site:jsBrowserTest",
          "--rerun-tasks",
          "--no-daemon",
        ),
        false,
      )
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
              "Services wait database" to arrayOf("site:servicesWaitDatabase"),
              "Start server" to arrayOf("site:kobwebStart"),
              "Start battery of tests" to
                arrayOf(
                  "site:allTests",
                  "-Dverbose=${project.findProperty("verbose") ?: "false"}",
                  "-x",
                  ":site:jsBrowserTest",
                  "--rerun-tasks",
                ),
            ).forEachIndexed { index, (name, process) ->
              val failed = !prepareAndRunProcess(name, process + "--no-daemon", index != 3)
              if (failed) anyFailed = true
            }
          } finally {
            prepareAndRunProcess("Stop server", arrayOf("site:kobwebStop", "--no-daemon"))
            prepareAndRunProcess("Stop secondary services", arrayOf("site:servicesStop", "--no-daemon"))
          }
        }
      }
		
    println("\u001B[37m| ------------------------------\u001B[0m")
    println(
      "\u001B[37m| Everything took: ${time.toComponents { seconds, nanoseconds ->
        val millis = nanoseconds / 1_000_000
        "$seconds.${millis.toString().padStart(3, '0')} s"
      }}\u001B[0m",
    )
    println("\u001B[37m| ------------------------------\u001B[0m")
		
    check(!anyFailed)
  }
}

val servicesUp =
  tasks.register<Exec>("servicesUp") {
    description = "Start services."
    commandLine("docker", "compose", "-f", "infra/compose.yaml", "up", "-d")
  }
val servicesStop =
  tasks.register<Exec>("servicesStop") {
    description = "Stop services temporarily."
    commandLine("docker", "compose", "-f", "infra/compose.yaml", "stop")
  }
val servicesDown =
  tasks.register<Exec>("servicesDown") {
    description = "Stop services."
    commandLine("docker", "compose", "-f", "infra/compose.yaml", "down")
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

tasks.register("migration") {
  // ./gradlew migration -Pname=""
	
  group = "database"
  description = "Create a new migration file based on current timestamp."
	
  val nameProvider = providers.gradleProperty("name").orElse("migration")
  val rootDir = layout.projectDirectory.asFile
	
  doLast {
		
    val userName = System.getProperty("user.name")
    val migrationName = nameProvider.get()
		
    val sanitizedName = migrationName.lowercase().replace(Regex("[^a-z0-9_]"), "-")
    val sanitizedUserName = userName.lowercase().replace(Regex("[^a-z0-9_]"), "-")
		
    val fileName = "${System.currentTimeMillis()}_${sanitizedUserName}_$sanitizedName.sql"
		
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

private val ignoredLines =
  sequenceOf(
    "STANDARD_OUT",
    "STANDARD_ERROR",
    "SLF4J(W)",
    "> Task",
    "[jvm] FAILED",
    "There were failing tests",
    "FAILURE: Build failed",
    "Execution failed for task",
    "* Try:",
    "> Run with",
    "> Get more help at",
    "* What went wrong:",
    "BUILD SUCCESSFUL",
    "warning workspace-aggregator",
    "me.kodokenshi.tabnewskobweb.tests.test.exception.TestFailedException at Test.kt",
    "To honour the JVM settings for this build a single-use Daemon",
    "Daemon will be stopped at the end of the build",
  )

private suspend fun runProcess(
  args: Array<String>,
  logOnlyIfFail: Boolean,
) = coroutineScope {
  println("\u001B[37mRunning '${args.joinToString(" ")}'...\u001B[0m")
  val log by lazy { StringBuilder() }
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
              "[${args.first()}] ${line.trim()}".let {
                if (logOnlyIfFail) {
                  log.append("$it\n")
                } else {
                  println(it)
                }
              }
            }
          }
        }
      }.join()
				
      ret = process.waitFor()
    }
		
  if (logOnlyIfFail && ret != 0) println(log)
  println(
    "\u001B[37mThis process took: ${time.toComponents { seconds, nanoseconds ->
      val millis = nanoseconds / 1_000_000
      "$seconds.${millis.toString().padStart(3, '0')} s"
    }}\u001B[0m",
  )
		
  ret
}

/**Returns `true` if process exit code is `0`.*/
private suspend fun prepareAndRunProcess(
  name: String,
  process: Array<String>,
  logOnlyIfFail: Boolean = true,
): Boolean {
  val exitCode = runProcess(process, logOnlyIfFail)
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
