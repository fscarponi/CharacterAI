plugins {
    kotlin("multiplatform") version "1.9.0"
    kotlin("plugin.serialization") version "1.9.0"
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.github.lamba92.docker") version "1.0.0-RC.2"
    id("org.jetbrains.compose") version "1.4.3"
}

group = "it.fscarponi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
    gradlePluginPortal()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))

    // Compose Web
    implementation("org.jetbrains.compose.web:web-core:1.5.11")
    implementation("org.jetbrains.compose.runtime:runtime:1.5.11")
    implementation("org.jetbrains.compose.html:html-core:1.5.11")
    implementation("org.jetbrains.compose.web:web-widgets:1.5.11")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("ch.qos.logback:logback-classic:1.4.12")

    // Ktor client
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")

    // JSON serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // Retrofit for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-jackson:2.9.0")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Jackson
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.12")

    // SQLite and Exposed
    implementation("org.jetbrains.exposed:exposed-core:0.45.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.45.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.45.0")
    implementation("org.xerial:sqlite-jdbc:3.44.1.0")
    implementation("com.zaxxer:HikariCP:5.0.1")

    // Telegram Bot
    implementation("org.telegram:telegrambots:6.8.0")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
            webpackTask {
                cssSupport {
                    enabled.set(true)
                }
                output.libraryTarget = "umd"
            }
            runTask {
                cssSupport {
                    enabled.set(true)
                }
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable()
    }
    jvm {
        jvmToolchain(17)
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation(kotlin("stdlib"))
                implementation(kotlin("stdlib-common"))
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(compose.web.core)
                implementation(compose.web.svg)
                implementation(compose.runtime)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.7.3")
            }
        }

        val jvmMain by getting {
            dependencies {
                // Existing JVM dependencies
                implementation("io.ktor:ktor-client-core:2.3.7")
                implementation("io.ktor:ktor-client-cio:2.3.7")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
                implementation("com.squareup.retrofit2:retrofit:2.9.0")
                implementation("com.squareup.retrofit2:converter-jackson:2.9.0")
                implementation("com.squareup.okhttp3:okhttp:4.12.0")
                implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3")
                implementation("com.fasterxml.jackson.core:jackson-databind:2.15.3")
                implementation("org.slf4j:slf4j-api:2.0.9")
                implementation("ch.qos.logback:logback-classic:1.4.12")
                implementation("org.jetbrains.exposed:exposed-core:0.45.0")
                implementation("org.jetbrains.exposed:exposed-dao:0.45.0")
                implementation("org.jetbrains.exposed:exposed-jdbc:0.45.0")
                implementation("org.xerial:sqlite-jdbc:3.44.1.0")
                implementation("com.zaxxer:HikariCP:5.0.1")
                implementation("org.telegram:telegrambots:6.8.0")
                implementation("org.telegram:telegrambots-meta:6.8.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jvm:1.7.3")
                implementation(kotlin("stdlib"))
                implementation(kotlin("stdlib-jdk8"))
                implementation(kotlin("reflect"))
            }
        }
    }
}

application {
    mainClass.set("it.fscarponi.MainKt")
}


docker {
    registries {
        githubContainerRegistry("fscarponi")
    }

    images {
        main {
            val version = System.getenv("RELEASE_TAG")?.removePrefix("v")
            imageName = "characterai_bot" // default
            imageVersion = version ?: project.version.toString()
            isLatestTag = false // default, if true, the image will have an additional tag`latest`
        }
    }

}
