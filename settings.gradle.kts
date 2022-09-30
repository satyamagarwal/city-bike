rootProject.name = "city-bike"

pluginManagement {
    val detektVersion: String by settings
    val kotlinVersion: String by settings
    val shadowJarVersion: String by settings
    val spotlessVersion: String by settings

    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        id("com.diffplug.spotless") version spotlessVersion
        id("com.github.johnrengelman.shadow") version shadowJarVersion
        id("io.gitlab.arturbosch.detekt") version detektVersion
        kotlin("jvm") version kotlinVersion
    }
}
