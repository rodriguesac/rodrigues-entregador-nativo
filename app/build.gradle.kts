plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.rodriguesacai.entregador"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rodriguesacai.entregador"
        minSdk = 26
        targetSdk = 35
        versionCode = 600
        versionName = "6.0.0-nativo-limpo"
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.android.gms:play-services-location:21.3.0")
}
