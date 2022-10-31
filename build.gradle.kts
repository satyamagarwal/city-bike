import com.diffplug.gradle.spotless.SpotlessExtension
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

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
    id("com.github.johnrengelman.shadow")
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
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-cors:$ktorVersion")
    implementation("io.ktor:ktor-server-double-receive:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:$coroutinesVersion")

    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("org.slf4j:slf4j-log4j12:$slf4jVersion")

    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
    runtimeOnly("org.apache.logging.log4j:log4j-core:$log4jVersion")

    testImplementation("io.ktor:ktor-client-mock-jvm:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host-jvm:$ktorVersion")

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
    register<Exec>("installNodeModules") {
        workingDir = file("city-bike-client")
        commandLine = listOf("pnpm", "install")
    }

    val buildClient: TaskProvider<Exec> by registering(Exec::class) {
        workingDir = file("city-bike-client")
        commandLine = listOf("pnpm", "build")
    }

    val deleteDistFolder: TaskProvider<Delete> by registering(Delete::class) {
        delete(file("src/main/resources/dist"))
    }

    register<Copy>("copyDistFolder") {
        dependsOn(buildClient, deleteDistFolder)

        from(file("city-bike-client/dist"))
        into(file("src/main/resources/dist"))
    }

    withType<ShadowJar> {
        transform(Log4j2PluginsCacheFileTransformer())
    }

    withType<Jar> {
        manifest {
            attributes["Main-Class"] = "city.bike.status.EntryMainKt"
            attributes["Multi-Release"] = "true"
        }
    }

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
