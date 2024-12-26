plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.secure_notes"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.secure_notes"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
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
    packaging {
        resources {
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/LICENSE.md"
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")

    implementation("de.svenkubiak:jBCrypt:0.4.1")


    implementation("com.squareup.retrofit2:retrofit:2.9.0")


    implementation("com.squareup.retrofit2:converter-gson:2.9.0")


    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
}