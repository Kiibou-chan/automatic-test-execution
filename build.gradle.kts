plugins {
    kotlin("jvm") version "1.8.20"
}

group = "space.kiibou"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    implementation("org.junit.platform:junit-platform-launcher:1.2.0")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}