import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.room)
}

// Cargar propiedades locales de forma segura
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

val rawAgentBaseUrl = localProperties.getProperty("agent.baseUrl") ?: ""
val normalizedBaseUrl = if (rawAgentBaseUrl.isNotBlank() && !rawAgentBaseUrl.endsWith("/")) {
    "$rawAgentBaseUrl/"
} else {
    rawAgentBaseUrl
}
val debugHost = localProperties.getProperty("agent.debugHost") ?: "10.0.2.2"

// Tarea Gradle para generar network_security_config.xml en debug
val generateDebugNetworkSecurityConfig = tasks.register("generateDebugNetworkSecurityConfig") {
    val outputDir = layout.buildDirectory.dir("generated/res/xml/debug")
    outputs.dir(outputDir)
    doLast {
        val dir = outputDir.get().asFile
        dir.mkdirs()
        val xmlFile = File(dir, "network_security_config.xml")
        xmlFile.writeText(
            """<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">10.0.2.2</domain>
        <domain includeSubdomains="true">$debugHost</domain>
    </domain-config>
    <base-config cleartextTrafficPermitted="false" />
</network-security-config>
""".trimIndent()
        )
    }
}

android {
    namespace = "com.lavidanoesunbanano"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lavidanoesunbanano"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "BASE_URL", "\"$normalizedBaseUrl\"")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            manifestPlaceholders["networkSecurityConfig"] = "@xml/network_security_config"
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders["networkSecurityConfig"] = "@xml/network_security_config_release"
            
            // Verificación de HTTPS en release
            if (normalizedBaseUrl.isNotBlank() && !normalizedBaseUrl.startsWith("https://")) {
                throw GradleException("En release, agent.baseUrl DEBE usar el protocolo HTTPS. Actual: $normalizedBaseUrl")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }
    
    sourceSets.getByName("debug").res.srcDir(layout.buildDirectory.dir("generated/res/xml/debug"))
}

tasks.named("preDebugBuild").configure {
    dependsOn(generateDebugNetworkSecurityConfig)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Retrofit & Serialization
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
