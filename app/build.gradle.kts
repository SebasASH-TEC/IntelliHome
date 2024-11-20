plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.company.intellihome"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.company.intellihome"
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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    androidTestImplementation(libs.fragment.testing)
    testImplementation(libs.junit)
    androidTestImplementation(libs.mockito)
    androidTestImplementation(libs.android.test.rules)
    androidTestImplementation(libs.android.test.core)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.constraintlayout)
    implementation(libs.google.services)
    implementation(libs.facebook.sdk)
    implementation(libs.tink)
    implementation(libs.osmdroid)
    implementation(libs.preference)
    implementation(libs.play.services.location)
    implementation(libs.biometric)
    implementation(libs.rounded)
}