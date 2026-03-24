import java.util.Properties
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.20"
    id("com.google.devtools.ksp") version "2.3.6"
    id("com.google.gms.google-services")
}

android {
    namespace = "com.rozetka.gigaavito"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }
    buildFeatures {
        buildConfig = true
    }
    signingConfigs {
        val properties = Properties()
        val propertiesFile = rootProject.file("local.properties")
        if (propertiesFile.exists()) {
            properties.load(propertiesFile.inputStream())
        }

        create("release") {
            storeFile = file(properties.getProperty("RELEASE_STORE_FILE") ?: "keystore.jks")
            storePassword = properties.getProperty("RELEASE_STORE_PASSWORD") ?: ""
            keyAlias = properties.getProperty("RELEASE_KEY_ALIAS") ?: ""
            keyPassword = properties.getProperty("RELEASE_KEY_PASSWORD") ?: ""
        }
    }
    defaultConfig {
        applicationId = "com.rozetka.gigaavito"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        val properties = Properties()
        val propertiesFile = rootProject.file("local.properties")
        if (propertiesFile.exists()) {
            properties.load(propertiesFile.inputStream())
        }
        val authKey = properties.getProperty("GIGACHAT_AUTH_KEY") ?: ""
        val baseUrl = properties.getProperty("GIGACHAT_BASE_URL") ?: ""
        val authUrl = properties.getProperty("GIGACHAT_AUTH_URL") ?: ""
        buildConfigField("String", "GIGACHAT_AUTH_KEY", authKey)
        buildConfigField("String", "GIGACHAT_BASE_URL", baseUrl)
        buildConfigField("String", "GIGACHAT_AUTH_URL", authUrl)
    }

    buildTypes {

            release {
                isMinifyEnabled = true
                signingConfig = signingConfigs.getByName("release")

                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        debug {

            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.bundles.coil)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.koin.androidx.compose)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.compose.markdown)
}