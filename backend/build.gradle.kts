plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.serialization") version "2.0.20"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor server
    implementation("io.ktor:ktor-server-core:2.3.12")
    implementation("io.ktor:ktor-server-netty:2.3.12")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.12")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.12")
    implementation("io.ktor:ktor-server-status-pages:2.3.12")
    implementation("io.ktor:ktor-server-call-logging:2.3.12")

    // Official Anthropic SDK (Kotlin uses the Java SDK)
    implementation("com.anthropic:anthropic-java:2.34.0")

    implementation("ch.qos.logback:logback-classic:1.5.6")

    testImplementation(kotlin("test"))
}

application {
    mainClass.set("com.dailystrength.backend.ApplicationKt")
}

kotlin {
    jvmToolchain(17)
}
