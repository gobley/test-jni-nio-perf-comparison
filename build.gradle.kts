import gobley.gradle.GobleyHost
import gobley.gradle.Variant
import gobley.gradle.cargo.dsl.jvm

plugins {
    kotlin("jvm") version "2.2.0"
    id("dev.gobley.cargo") version "0.3.5"
    application
}

group = "dev.gobley.test.jninioperfcomparison"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass = "dev.gobley.test.jninioperfcomparison.MainKt"
}

cargo {
    jvmVariant = Variant.Release
    builds.jvm {
        resourcePrefix = "jvm"
        embedRustLibrary = (GobleyHost.current.rustTarget == rustTarget)
    }
}