import com.diffplug.gradle.spotless.SpotlessExtension
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import java.io.ByteArrayOutputStream

val arrowKtVersion: String by project
val config4kVersion: String by project
val coroutinesVersion: String by project
val gradleWrapperVersion: String by project
val jacksonVersion: String by project
val jacocoToolVersion: String by project
val jvmTargetVersion: String by project
val kotestVersion: String by project
val kotlinLanguageVersion: String by project
val kotlinVersion: String by project
val ktlintVersion: String by project
val ktorVersion: String by project
val log4jVersion: String by project
val resilience4jVersion: String by project
val slf4jVersion: String by project

plugins {
    id("com.diffplug.spotless")
    id("io.gitlab.arturbosch.detekt")
    jacoco
    kotlin("jvm")
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")

    implementation("io.arrow-kt:arrow-core:$arrowKtVersion")
    implementation("io.arrow-kt:arrow-fx-coroutines:$arrowKtVersion")

    implementation("io.github.config4k:config4k:$config4kVersion")
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:$resilience4jVersion")
    implementation("io.github.resilience4j:resilience4j-kotlin:$resilience4jVersion")

    implementation("io.ktor:ktor-client-cio-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-http-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:$coroutinesVersion")

    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-log4j12:$slf4jVersion")

    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
    runtimeOnly("org.apache.logging.log4j:log4j-core:$log4jVersion")

    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
}

configurations {
    all {
        resolutionStrategy.eachDependency {
            when (this.requested.group) {
                "io.ktor" -> this.useVersion(ktorVersion)
                "org.apache.logging.log4j" -> this.useVersion(log4jVersion)
                "org.jetbrains.kotlin" -> this.useVersion(kotlinVersion)
            }
        }
    }

    configure<SpotlessExtension> {
        kotlin {
            trimTrailingWhitespace()
            endWithNewline()
            ktlint(ktlintVersion)
                .setUseExperimental(true)
                .editorConfigOverride(mapOf("indent_size" to "4", "continuation_indent_size" to "4"))
        }
        kotlinGradle {
            target("*.kts")
            trimTrailingWhitespace()
            endWithNewline()
            ktlint(ktlintVersion)
                .setUseExperimental(true)
                .editorConfigOverride(mapOf("indent_size" to "4", "continuation_indent_size" to "4"))
        }
    }

    configure<DetektExtension> {
        parallel = true
        config = files("${rootProject.projectDir}/detekt.yml")
        buildUponDefaultConfig = true
        disableDefaultRuleSets = false
        debug = false
        ignoreFailures = false
    }

    configure<JacocoPluginExtension> {
        toolVersion = jacocoToolVersion
    }
}

tasks {
    withType<Wrapper> {
        gradleVersion = gradleWrapperVersion
        distributionType = Wrapper.DistributionType.ALL
    }

    compileKotlin {
        kotlinOptions.jvmTarget = jvmTargetVersion
        incremental = true
        kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
        kotlinOptions.languageVersion = kotlinLanguageVersion
    }

    compileTestKotlin {
        kotlinOptions.jvmTarget = jvmTargetVersion
        incremental = true
        kotlinOptions.freeCompilerArgs = listOf("-Xjsr305=strict")
        kotlinOptions.languageVersion = kotlinLanguageVersion
    }

    withType<Test> {
        useJUnitPlatform()

        finalizedBy("jacocoTestReport")

        systemProperties = mapOf(
            "junit.jupiter.extensions.autodetection.enabled" to "true",
            "junit.jupiter.execution.parallel.enabled" to "true",
            "junit.jupiter.execution.parallel.mode.default" to "concurrent",
            "junit.jupiter.execution.parallel.mode.classes.default" to "concurrent",
            "junit.platform.output.capture.stdout" to "true",
            "junit.platform.output.capture.stderr" to "true"
        )

        testLogging {
            events("passed", "skipped", "failed")
            showExceptions = true
            exceptionFormat = TestExceptionFormat.FULL
            // Enable to show output from standard out:
            // showStandardStreams = true
        }
    }

    withType<Detekt> {
        reports {
            html.required.set(true)
            xml.required.set(true)
        }
    }
}

fun execCommand(command: List<String>, ignoreError: Boolean = false): String {
    val buffer = ByteArrayOutputStream()

    project.exec {
        commandLine(command)
        standardOutput = buffer
        isIgnoreExitValue = ignoreError
    }

    return String(buffer.toByteArray()).trim()
}
