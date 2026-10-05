/*
 * =====================================================================
 *  EJERCICIO 2 — Flight Search: Room + DataStore (proyecto final de la unidad)
 *  Archivo destino: app/build.gradle.kts (Module :app) + gradle.properties
 * =====================================================================
 *  Proyecto Android Studio : Flight Search — PROYECTO NUEVO (New Project > Phone and Tablet > Empty Activity)
 *  Package name            : com.example.flightsearch
 *  SDK                     : Minimum SDK API 24 (valor por defecto del asistente) · emulador API 26 o superior
 *  Recursos                : flight_search.db (en recursos_FlightSearch.zip) va en app/src/main/assets/database/. No usa imágenes.
 *  Qué hacer               : NO reemplaces el archivo: agrega los 2 bloques donde se indica y
 *                            la línea de gradle.properties. Luego "Sync Now".
 * =====================================================================
 */
// =====================================================================
//  NO reemplaces tu archivo: AGREGA estas líneas en app/build.gradle.kts (Module :app)
// =====================================================================

// 1) Dentro del bloque plugins { } que ya existe, al final:
plugins {
    // KSP: genera el código de Room. La parte "2.2.10" debe ser IGUAL a la línea
    // kotlin = "..." de gradle/libs.versions.toml (si tu versión es otra, avísame).
    id("com.google.devtools.ksp") version "2.2.10-2.0.2"
}

// 2) Dentro del bloque dependencies { } que ya existe, al final:
dependencies {
    // ViewModel para Compose (viewModel(factory = ...))
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Room: base de datos (runtime + soporte Kotlin/Flow + generador de código con KSP)
    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    ksp("androidx.room:room-compiler:2.8.5")

    // Preferences DataStore: guarda el texto de búsqueda
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // Íconos básicos (Search, Clear, Star). La versión la pone el BOM de Compose.
    implementation("androidx.compose.material:material-icons-core")
}

// 3) En gradle.properties (Project Properties), agrega esta línea al final:
//    android.disallowKotlinSourceSets=false
//    (KSP todavía usa una forma antigua de registrar el código generado y el
//     Kotlin integrado de AGP 9 la bloquea; esta línea lo permite)
