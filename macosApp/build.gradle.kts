import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    jvmToolchain(libs.versions.jvm.toolchain.get().toInt())

    macosArm64 {
        binaries {
            executable {
                entryPoint = "coredevices.coreapp.main"
                baseName = "CoreApp"
                freeCompilerArgs += listOf("-Xexpect-actual-classes")
            }
        }
    }

    sourceSets {
        macosMain.dependencies {
            implementation(project(":composeApp"))

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(libs.kotlinx.io.core)
            implementation(libs.ktor.client.darwin)
            implementation(libs.coroutines)
        }
    }
}

compose.resources {
    packageOfResClass = "coreapp.macosapp.generated.resources"
}

// Packages the Kotlin/Native executable into a double-clickable Pebble.app.
// Everything the task action touches is resolved while configuring, so the action only
// captures plain files and strings (the build uses the configuration cache).
val binaryPath = layout.buildDirectory.file("bin/macosArm64/debugExecutable/CoreApp.kexe")
val appPath = layout.buildDirectory.dir("macosApp/Pebble.app")
val infoPlistPath = layout.projectDirectory.file("src/macosMain/resources/Info.plist")
val entitlementsPath = layout.projectDirectory.file("src/macosMain/resources/CoreApp.entitlements")
val iconPath = layout.projectDirectory.file("src/macosMain/resources/AppIcon.icns")

// Compose Resources are assembled per module; the app needs all of them merged.
val resourceModules = listOf(":util", ":pebble", ":composeApp", ":macosApp")
val assembledResourceDirs = resourceModules.map { path ->
    project(path).layout.buildDirectory
        .dir("generated/compose/resourceGenerator/assembledResources/macosArm64Main/composeResources")
        .get().asFile
}

val bundleApp by tasks.registering {
    dependsOn("linkDebugExecutableMacosArm64")
    resourceModules.forEach { dependsOn("$it:assembleMacosArm64MainResources") }

    val binary = binaryPath.get().asFile
    val app = appPath.get().asFile
    val infoPlist = infoPlistPath.asFile
    val entitlements = entitlementsPath.asFile
    val icon = iconPath.asFile
    val resourceDirs = assembledResourceDirs

    inputs.files(binary, infoPlist, entitlements, icon)
    inputs.files(resourceDirs)
    outputs.dir(app)

    doLast {
        app.deleteRecursively()
        val macos = File(app, "Contents/MacOS").apply { mkdirs() }
        val resources = File(app, "Contents/Resources").apply { mkdirs() }
        // The Compose macOS resource reader looks up
        // `Contents/Resources/compose-resources/composeResources/<namespace>/...`
        // (NSBundle.resourcePath + "/compose-resources/" + the path, which itself starts
        // with "composeResources/").
        val composeResources = File(resources, "compose-resources/composeResources").apply { mkdirs() }
        binary.copyTo(File(macos, "Pebble"), overwrite = true)
        File(macos, "Pebble").setExecutable(true)
        infoPlist.copyTo(File(app, "Contents/Info.plist"), overwrite = true)
        icon.copyTo(File(resources, "AppIcon.icns"), overwrite = true)
        resourceDirs.forEach { src ->
            if (src.exists()) src.copyRecursively(composeResources, overwrite = true)
        }
        // Ad-hoc signing is enough for local use; Developer ID + notarization come with distribution.
        val codesign = ProcessBuilder(
            "codesign", "--force", "--sign", "-", "--timestamp=none",
            "--entitlements", entitlements.absolutePath,
            app.absolutePath,
        ).redirectErrorStream(true).start()
        val output = codesign.inputStream.bufferedReader().readText()
        check(codesign.waitFor() == 0) { "codesign failed: $output" }
        println("Bundled ${app.absolutePath}")
    }
}
