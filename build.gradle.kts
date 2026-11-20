import org.jetbrains.kotlin.gradle.targets.native.tasks.PodGenTask

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.nativeCocoaPods) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.buildKonfig) apply false
    alias(libs.plugins.kotlinx.atomicfu) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.androidLint) apply false
}

project.gradle.taskGraph.whenReady {
    allTasks.filter { it::class.simpleName?.contains("EmbedAndSign") == true }.forEach {
        logger.warn("Disabling embedding and signing task in project ${it.project.name}")
        it.enabled = false
    }
}

// Synthetic pod installs share one CocoaPods cache, which is not safe for concurrent writers.
abstract class PodInstallLock : BuildService<BuildServiceParameters.None>

val podInstallLock = gradle.sharedServices.registerIfAbsent("podInstallLock", PodInstallLock::class) {
    maxParallelUsages.set(1)
}

// group:module coordinates replaced by :macos-stubs on macOS
val MACOS_STUBBED_MODULES = listOf(
    "dev.gitlive:firebase-common",
    "dev.gitlive:firebase-auth",
    "dev.gitlive:firebase-firestore",
    "dev.gitlive:firebase-crashlytics",
    "com.viktormykhailiv:health-kmp",
    "io.github.mirzemehdi:kmpnotifier",
    "io.github.kevinnzou:compose-webview-multiplatform",
    "dev.jordond.compass:geocoder-mobile",
    "dev.jordond.compass:autocomplete-mobile",
    "io.github.coredevices.haversine:haversine",
)

subprojects {
    tasks.matching { it.name.startsWith("podInstallSynthetic") }.configureEach {
        usesService(podInstallLock)
    }
    // Xcode 27 rejects pod deployment targets below iOS 15; the generated Podfile only raises them to 12.
    tasks.withType<PodGenTask>().configureEach {
        val podfileProvider = podfile
        doLast {
            val file = podfileProvider.get()
            file.writeText(
                file.readText()
                    .replace("deployment_target_major < 12 ", "deployment_target_major < 15 ")
                    .replace("deployment_target_major == 12 ", "deployment_target_major == 15 ")
                    .replace("#{12}", "#{15}")
            )
        }
    }

    // Libraries with no macOS artifacts are swapped for no-op stubs on macOS only, so
    // commonMain keeps using their real APIs. See :macos-stubs.
    if (name != "macos-stubs") {
        configurations.configureEach {
            if (name.contains("macos", ignoreCase = true)) {
                resolutionStrategy.dependencySubstitution {
                    MACOS_STUBBED_MODULES.forEach { stubbed ->
                        substitute(module(stubbed)).using(project(":macos-stubs"))
                    }
                }
            }
        }
    }
}
