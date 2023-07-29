// Top-level build file where you can add configuration options common to all sub-projects/modules.

val libVersion by extra { "0.1.3" }

plugins {
    val kotlinVersion = "1.8.22"

    id("com.android.application") version "8.0.2" apply false
    id("org.jetbrains.kotlin.android") version kotlinVersion apply false
    id("org.jetbrains.kotlin.plugin.serialization") version kotlinVersion apply false
}
