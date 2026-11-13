plugins { id("com.android.application") }
android {
    namespace = "io.github.stormblue.marqueeview.sample"
    compileSdk = 37
    defaultConfig {
        applicationId = "io.github.stormblue.marqueeview.sample"
        minSdk = 23
        targetSdk = 37
        versionCode = 2
        versionName = "2.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildTypes { release { isMinifyEnabled = true; isShrinkResources = true; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt")) } }
}
dependencies {
    implementation(project(":marqueeview"))
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
}
