plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("kotlinx-serialization")
    id("maven-publish")
}

android {
    namespace = "com.vipulog.ebookreader"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.webkit:webkit:1.7.0")

    implementation("org.nanohttpd:nanohttpd:2.3.1")
}

val libVersion: String by rootProject.extra

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.vipulog"
            artifactId = "ebookreader"
            version = libVersion

            afterEvaluate {
                from(components["release"])
            }
        }
    }
}
