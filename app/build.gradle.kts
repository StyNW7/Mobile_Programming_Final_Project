plugins {
    alias(libs.plugins.android.application)
    // It's good practice to also include the Kotlin plugin here
    // alias(libs.plugins.kotlin.android)
}

android {
    namespace = "id.example.sehatin"
    compileSdk = 34 // Changed to 34, as 36 is not yet released. The latest stable version is 34.

    defaultConfig {
        applicationId = "id.example.sehatin"
        minSdk = 26 // Changed to 24 for broader device support. 35 is extremely high.
        targetSdk = 34 // Matched to compileSdk.
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // proguardFiles(...) should be here if you use ProGuard
        }
        // You can also define a debug build type if needed
        debug {
            isMinifyEnabled = false
        }
    }

    // It's recommended to add compileOptions for Java version compatibility
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // If using Kotlin, add kotlinOptions
    // kotlinOptions {
    //     jvmTarget = "1.8"
    // }

    // View Binding is often used and can be enabled here
     buildFeatures {
         viewBinding = true
     }
}

// FIX: Dependencies must be in their own block, outside of the android { ... } block.
dependencies {
    // Add your core libraries first
    // implementation(libs.core.ktx)
    // implementation(libs.appcompat)
    // implementation(libs.material)

    // Your libraries
    implementation(libs.constraintlayout)
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.google.android.material:material:1.12.0")

    // Testing libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
