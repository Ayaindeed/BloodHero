plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.bloodhero"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.bloodhero"
        minSdk = 29
        targetSdk = 36
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
}

dependencies {
    // Core Android
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.fragment)
    
    // UI Components
    implementation(libs.cardview)
    implementation(libs.recyclerview)
    implementation(libs.viewpager2)
    implementation(libs.circleimageview)
    
    // Splash Screen
    implementation(libs.splashscreen)
    
    // Animation
    implementation(libs.lottie)
    
    // Image Loading
    implementation(libs.glide)
    
    // Preferences
    implementation(libs.preference)
    
    // Maps (OpenStreetMap)
    implementation(libs.osmdroid)
    
    // Charts
    implementation(libs.mpandroidchart)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}