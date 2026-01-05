plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt") // ← CRÍTICO: Necesario para Room (sintaxis correcta Kotlin DSL)
    id("kotlin-parcelize") // ← Útil para pasar objetos entre pantallas
}

android {
    namespace = "com.unaiIgorRuben.diacero"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.unaiIgorRuben.diacero"
        minSdk = 24  // Android 7.0 (cubre el 95% de dispositivos)
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // ===== ACTIVAR VIEW BINDING =====
    // Genera clases para acceder a las vistas sin findViewById
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // ===== ANDROID CORE =====
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ===== MATERIAL DESIGN 3 =====
    // Incluye: CardView, FloatingActionButton, Toolbar, etc.
    implementation("com.google.android.material:material:1.11.0")

    // ===== ROOM (BASE DE DATOS) =====
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion") // Para coroutines
    kapt("androidx.room:room-compiler:$roomVersion") // Genera código automáticamente

    // ===== LIFECYCLE & VIEWMODEL =====
    val lifecycleVersion = "2.7.0"
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")

    // ===== NAVIGATION COMPONENT =====
    // Para navegar entre Fragments de forma segura
    val navVersion = "2.7.6"
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")

    // ===== COROUTINES =====
    // Para operaciones asíncronas (base de datos, red, etc.)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // ===== PARA FUTURAS FUNCIONALIDADES =====

    // WorkManager (para el Modo Enfoque automático)
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // CameraX (para capturar fotos)
    val cameraxVersion = "1.3.1"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // Glide (para mostrar imágenes)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // ===== TESTING =====
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.room:room-testing:$roomVersion")
}

// ===== CONFIGURACIÓN DE KAPT =====
// Mejora el rendimiento de la compilación
kapt {
    correctErrorTypes = true
    useBuildCache = true
}