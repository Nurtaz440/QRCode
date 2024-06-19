plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.nurtaz.dev.qrcode"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.nurtaz.dev.qrcode"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Use this dependency to use the dynamically downloaded model in Google Play Services
    implementation ("com.google.android.gms:play-services-mlkit-barcode-scanning:18.3.0")


    implementation ("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation ("androidx.camera:camera-lifecycle:1.3.4")
    implementation ("androidx.camera:camera-video:1.3.4")

    implementation ("androidx.camera:camera-view:1.3.4")
    implementation ("androidx.camera:camera-extensions:1.3.4")

    implementation("androidx.concurrent:concurrent-futures:1.2.0")
    // Use this dependency to bundle the model with your app
    implementation ("com.google.mlkit:barcode-scanning:17.2.0")
}