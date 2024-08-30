import com.android.build.api.dsl.LibraryExtension

plugins {
    val enableAndroid = System.getProperty("enable_android")
        ?.equals("true", ignoreCase = true) != false
            && JavaVersion.current() >= JavaVersion.VERSION_17

    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.library") apply enableAndroid
    id("org.jetbrains.compose")

    id("org.jetbrains.dokka")
    id("com.diffplug.spotless")
}

// vercel自带的Java 11，但是AGP要求17，所以添加开关
val enableAndroid = System.getProperty("enable_android")
    ?.equals("true", ignoreCase = true) != false
        && JavaVersion.current() >= JavaVersion.VERSION_17

kotlin {
    if (enableAndroid) {
        androidTarget {
            publishLibraryVariants("release")
            compilations.all {
                kotlinOptions {
                    jvmTarget = "11"
                }
            }
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    jvm()
    wasmJs {
        browser()
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {

            // Kotlin Coroutines
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

            // Jetpack Compose

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            api("org.jetbrains.skiko:skiko:0.8.1")
        }

        if (enableAndroid) {
            androidMain.dependencies { implementation("androidx.core:core-ktx:1.13.0") }
        }

        val skiaMain by creating {
            dependsOn(getByName("commonMain"))
        }

        val iosMain by getting {
            dependsOn(skiaMain)
        }

        val jvmMain by getting {
            dependsOn(skiaMain)
        }

        val wasmJsMain by getting {
            dependsOn(skiaMain)
        }
    }
}

if (enableAndroid) {
    (extensions.getByName("android") as LibraryExtension).apply {
        namespace = "com.hunch.genz"
        compileSdk = 34

        defaultConfig {
            minSdk = 21
            targetSdk = 34

            consumerProguardFiles("consumer-rules.pro")
        }

        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }


        buildTypes {
            getByName("release") {
                isMinifyEnabled = false
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
                )
            }
        }

        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_11
            targetCompatibility = JavaVersion.VERSION_11
        }

        buildFeatures {
            buildConfig = false
            compose = true
        }

        composeOptions {
            kotlinCompilerExtensionVersion = "1.5.8"
        }

        namespace = "dev.shreyaspatil.capturable"
    }

//    dependencies {
//
//
//        // Android core
//        implementation("androidx.core:core-ktx:1.13.0")
//
//        // Testing
//        testImplementation("junit:junit:4.13.2")
//        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
//    //    androidTestImplementation composeBom
//        androidTestImplementation("androidx.test.ext:junit:1.1.5")
//        androidTestImplementation("androidx.compose.ui:ui-test-junit4")
//        androidTestImplementation("androidx.compose.ui:ui-test-manifest")
//
//        // For using UI components in test
//        androidTestImplementation("androidx.compose.material:material")
//    }
}

tasks.dokkaHtml{
    outputDirectory.set(rootProject.mkdir("docs"))

    dokkaSourceSets {
        named("commonMain") {
            noAndroidSdkLink.set(false)
        }
    }
}

spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("$buildDir/**/*.kt")
        targetExclude("bin/**/*.kt")

        ktlint().editorConfigOverride(mapOf(
            "android" to "true",
        ))
        licenseHeaderFile(rootProject.file("spotless/copyright.kt"))
    }
}

apply {
    plugin("com.vanniktech.maven.publish")
}
