import io.github.zap.build.gradle.convention.*

plugins {
    id("io.github.zap.build.gradle.convention.shadow-mc-plugin") version "1.1.0"
}

dependencies {
    paperApi("1.16.5-R0.1-SNAPSHOT")
}

publishToZGpr()