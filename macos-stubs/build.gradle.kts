plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

// Stub implementations (no-ops) of third-party libraries that don't publish macOS targets.
// Used on macOS only: the root build swaps these coordinates for this project on every
// macOS configuration (see MACOS_STUBBED_MODULES in the root build.gradle.kts).
//
// The public API has to match the real libraries closely enough that commonMain compiles
// unchanged. `com/viktormykhailiv/kmp/health` is copied from health-kmp 1.7.0's common
// sources (Apache 2.0) so the many record/unit types exist; only the manager is stubbed.
kotlin {
    macosArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(libs.coroutines)
            implementation(libs.serialization)
            implementation(libs.compass.geocoder)
            implementation(libs.compass.autocomplete)
        }
    }
}
