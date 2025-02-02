plugins {
    kotlin("multiplatform") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("org.jetbrains.compose") version "1.7.3"
}

group = "it.fscarponi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/wasm/experimental")
    google()
}

kotlin {
    @OptIn(
        org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl::class,
        org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalDistributionDsl::class
    )
    wasmJs {
        moduleName = "telegramWebApp"
        browser {
            commonWebpackConfig {
                outputFileName = "telegramWebApp.js"
                devServer = (devServer ?: org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig.DevServer()).copy(
                    static = (devServer?.static ?: mutableListOf()).apply { 
                        add(project.projectDir.resolve("src/wasmJsMain/resources").path)
                    }
                )
            }
            distribution {
                outputDirectory = project.projectDir.resolve("build/dist/wasm")
            }
        }
        binaries.executable()
    }

    js(IR) {
        browser {
            testTask {
                useKarma {
                    useChrome()
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
            }
        }

        val wasmJsMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(npm("@twa-dev/sdk", "^6.9.0"))
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(compose.html.core)
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

    }
}

tasks.named("check") {
    dependsOn("wasmJsBrowserTest")
}
