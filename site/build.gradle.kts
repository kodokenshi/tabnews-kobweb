import com.varabyte.kobweb.gradle.application.util.configAsKobwebApplication

plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.kobweb.application)
	alias(libs.plugins.kobwebx.markdown)
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
		nodejs {
			testTask {
				
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
			
		}
		
	}
}
