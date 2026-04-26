plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
    id("org.jetbrains.intellij.platform") version "2.3.0"
}

group = "com.psrank.edittrail"
version = "0.1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    }
}

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2025.1.3", useInstaller = false)
        pluginVerifier()
        zipSigner()
    }

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.3")
}

intellijPlatform {
    pluginConfiguration {
        id = "com.psrank.edittrail"
        name = "EditTrail"
        version = project.version.toString()

        ideaVersion {
            sinceBuild = "251"
        }
    }

    buildSearchableOptions = false
}

tasks {
    named("instrumentCode") {
        enabled = false
    }

    test {
        useJUnitPlatform()
    }
}
