plugins {
    id("com.android.library")
    `maven-publish`
}

group = "io.github.stormblue"
version = "2.0.0"

android {
    namespace = "io.github.stormblue.ringprogress"
    compileSdk = 37
    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    publishing { singleVariant("release") { withSourcesJar() } }
}

dependencies {
    androidTestImplementation("androidx.test:runner:1.7.0")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = project.group.toString()
            artifactId = "ringprogress"
            version = project.version.toString()
            pom {
                name = "RingProgress"
                description = "A small, allocation-free drawing Android ring progress view."
                url = "https://github.com/StormBlue/MarqueeApplication"
                licenses { license { name = "Apache License 2.0"; url = "https://www.apache.org/licenses/LICENSE-2.0" } }
                scm { url = "https://github.com/StormBlue/MarqueeApplication" }
            }
        }
    }
}
afterEvaluate { publishing.publications.named<MavenPublication>("release") { from(components["release"]) } }
