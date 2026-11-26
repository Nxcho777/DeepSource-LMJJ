import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    jacoco
}

fun serviceUrl(property: String, fallback: String = ""): String =
    providers.gradleProperty(property).orElse(providers.environmentVariable(property))
        .getOrElse(fallback).trim().let { if (it.isEmpty()) it else it.trimEnd('/') + "/" }

fun quoted(value: String): String = "\""+ value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "cl.duoc.deepsource"
    compileSdk = 35

    defaultConfig {
        applicationId = "cl.duoc.deepsource"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    sourceSets.getByName("androidTest").assets.srcDir("$projectDir/schemas")
    sourceSets.getByName("test").resources.srcDir("$projectDir/schemas")
    testOptions.unitTests.isIncludeAndroidResources = true

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true

    }

    kotlinOptions {
        jvmTarget = "17"
    }

    val releaseProjects = serviceUrl("DS_RELEASE_PROJECTS_URL")
    val releaseTasks = serviceUrl("DS_RELEASE_TASKS_URL")
    require(listOf(releaseProjects, releaseTasks).all { it.isEmpty() || it.startsWith("https://") }) {
        "Las URL release deben usar HTTPS."
    }
    val signingValues = listOf("DS_RELEASE_STORE_FILE", "DS_RELEASE_STORE_PASSWORD",
        "DS_RELEASE_KEY_ALIAS", "DS_RELEASE_KEY_PASSWORD").map { providers.environmentVariable(it).orNull }
    if (signingValues.all { !it.isNullOrBlank() }) {
        signingConfigs.create("ev4Release") {
            storeFile = file(signingValues[0]!!)
            storePassword = signingValues[1]
            keyAlias = signingValues[2]
            keyPassword = signingValues[3]
        }
    }
    buildTypes {
        debug {
            buildConfigField("String", "PROJECTS_URL", quoted(serviceUrl("DS_DEBUG_PROJECTS_URL", "http://10.0.2.2:8081/")))
            buildConfigField("String", "TASKS_URL", quoted(serviceUrl("DS_DEBUG_TASKS_URL", "http://10.0.2.2:8082/")))
        }
        release {
            signingConfig = signingConfigs.findByName("ev4Release")
            buildConfigField("String", "PROJECTS_URL", quoted(releaseProjects))
            buildConfigField("String", "TASKS_URL", quoted(releaseTasks))
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    ksp(libs.androidx.room.compiler)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.espresso.core)
    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.ui.test.junit4)
    implementation("androidx.core:core-splashscreen:1.0.1")
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

tasks.withType<Test>().matching { it.name == "testDebugUnitTest" }.configureEach {
    extensions.configure<JacocoTaskExtension> {
        // Robolectric's sandbox may load application classes without a code-source URL.
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

tasks.register<JacocoReport>("ev4UnitCoverage") {
    group = "verification"
    description = "Genera la cobertura JVM de todo el código Android debug."
    dependsOn("testDebugUnitTest")
    executionData(layout.buildDirectory.file("jacoco/testDebugUnitTest.exec"))
    classDirectories.setFrom(
        layout.buildDirectory.dir("tmp/kotlin-classes/debug"),
        layout.buildDirectory.dir("intermediates/javac/debug/compileDebugJavaWithJavac/classes")
    )
    sourceDirectories.setFrom(files("src/main/java"))
    reports {
        xml.required.set(true)
        html.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/coverage/test/debug/report.xml"))
        html.outputLocation.set(layout.buildDirectory.dir("reports/coverage/test/debug"))
    }
}
