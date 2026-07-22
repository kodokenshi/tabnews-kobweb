import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication
import java.util.*

buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath("org.postgresql:postgresql:42.7.13")
		classpath("org.flywaydb:flyway-database-postgresql:12.10.0")
	}
}

plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.kobweb.application)
	alias(libs.plugins.kobwebx.markdown)
	id("org.flywaydb.flyway") version "12.10.0"
}

fun getEnvVar(key: String, defaultValue: String = ""): String {
	
	val envFile = File(projectDir, ".env.development")
	if (envFile.exists()) {
		
		val properties = Properties()
		envFile.inputStream().use { properties.load(it) }
		val value = properties.getProperty(key)
		if (value != null) return value
		
	}
	return System.getenv(key) ?: defaultValue
	
}

flyway {
	
	url = getEnvVar("POSTGRES_URL", "jdbc:postgresql://POSTGRES_HOST:POSTGRES_PORT/POSTGRES_DB")
		.replace("POSTGRES_HOST", getEnvVar("POSTGRES_HOST", "localhost"))
		.replace("POSTGRES_PORT", getEnvVar("POSTGRES_PORT", "5432"))
		.replace("POSTGRES_DB", getEnvVar("POSTGRES_DB", "local_db"))
		.replace("POSTGRES_PASSWORD", getEnvVar("POSTGRES_PASSWORD", "local_password"))
		.replace("POSTGRES_USER", getEnvVar("POSTGRES_USER", "local_user"))
	user = getEnvVar("POSTGRES_USER", "local_user")
	password = getEnvVar("POSTGRES_PASSWORD", "local_password")
	locations = arrayOf("filesystem:${layout.settingsDirectory.asFile.absolutePath}/infra/migrations")
	
}

group = "me.kodokenshi.tabnewskobweb"
version = "1.0-SNAPSHOT"

kobweb {
	app {
		index {
			description.set("Powered by Kobweb")
		}
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
	
	// This example is frontend only. However, for a fullstack app, you can uncomment the includeServer parameter
	// and the `jvmMain` source set below.
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
			
			compileOnly(libs.kobweb.api) // Provided by Kobweb backend at runtime
			implementation("io.ktor:ktor-client-cio:3.5.0")
			
			implementation("org.jetbrains.exposed:exposed-core:1.3.1")
			implementation("org.jetbrains.exposed:exposed-dao:1.3.1")
			implementation("org.jetbrains.exposed:exposed-jdbc:1.3.1")
			
			implementation("org.postgresql:postgresql:42.7.13")
			implementation("com.zaxxer:HikariCP:7.1.0")
			
			implementation("org.flywaydb:flyway-core:12.10.0")
			implementation("org.flywaydb:flyway-database-postgresql:12.10.0")
			
		}
		
	}
}
tasks.register<Exec>("servicesStop") {
	
	description = "Pausa temporariamente os serviços secundários"
	commandLine("docker", "compose", "-f", "../infra/compose.yaml", "stop")
	
}
tasks.register<Exec>("servicesDown") {
	
	description = "Derruba os serviços secundários"
	commandLine("docker", "compose", "-f", "../infra/compose.yaml", "down")
	
}
tasks.register<Exec>("servicesUp") {
	
	description = "Sobe os serviços secundários"
	commandLine("docker", "compose", "-f", "../infra/compose.yaml", "up", "-d")
	
}
tasks.register("stopDev") {
	
	description = "Derruba o servidor e os serviços"
	dependsOn("servicesDown", "kobwebStop")
	
}

tasks.register("flywayCreate") {
	
	group = "database"
	description = "Cria um novo arquivo de migração SQL com base no timestamp atual."
	
	val nameProvider = providers.gradleProperty("name").orElse("migration")
	val rootDir = layout.settingsDirectory.asFile
	
	doLast {
		
		val migrationName = nameProvider.get()
		
		val sanitizedName = migrationName.lowercase().replace(Regex("[^a-z0-9_]"), "-")
		
		val fileName = "V${System.currentTimeMillis()}__${sanitizedName}.sql"
		
		val migrationDir = File("${rootDir}/infra", "migrations")
		if (!migrationDir.exists()) migrationDir.mkdirs()
		
		val migrationFile = File(migrationDir, fileName)
		if (!migrationFile.exists()) {
			
			migrationFile.createNewFile()
			println("Migration created: ${migrationFile.toURI()}")
			
		} else println("Migration ${migrationFile.toURI()} already exists!")
		
	}
	
}

