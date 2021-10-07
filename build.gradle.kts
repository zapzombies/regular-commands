import io.github.zap.build.gradle.convention.*

plugins {
    id("io.github.zap.build.gradle.convention.lib") version "1.1.0-SNAPSHOT-1633613339"
}

dependencies {
    paperApi("1.16.5-R0.1-SNAPSHOT")
}

publishToZGpr()