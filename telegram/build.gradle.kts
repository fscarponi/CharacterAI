plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":"))
    implementation("org.telegram:telegrambots:6.8.0")
    implementation("org.telegram:telegrambots-meta:6.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jvm:1.7.3")
    implementation(kotlin("stdlib"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
}