plugins {
    id("com.android.application")
}

android {
    namespace = "com.lotfabadi.filter"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.lotfabadi.filter"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.webkit:webkit:1.17.0")
}
