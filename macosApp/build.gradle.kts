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

// Packages the Kotlin/Native executable into a double-clickable CoreApp.app.
val appDir = layout.buildDirectory.dir("macosApp/CoreApp.app")

val bundleApp by tasks.registering {
    dependsOn("linkDebugExecutableMacosArm64")
    // Compose Resources are assembled per module; the app needs all of them merged.
    val resourceModules = listOf(":util", ":pebble", ":composeApp", ":macosApp")
    resourceModules.forEach { dependsOn("$it:assembleMacosArm64MainResources") }
    val assembledDirs = resourceModules.map { path ->
        project(path).layout.buildDirectory.dir(
            "generated/compose/resourceGenerator/assembledResources/macosArm64Main/composeResources"
        )
    }
    val app = appDir
    val binaryFile = layout.buildDirectory.file(
        "bin/macosArm64/debugExecutable/CoreApp.kexe"
    )
    val infoPlist = layout.projectDirectory.file("src/macosMain/resources/Info.plist")
    val entitlements = layout.projectDirectory.file("src/macosMain/resources/CoreApp.entitlements")
    inputs.file(binaryFile)
    inputs.file(infoPlist)
    inputs.files(assembledDirs)
    outputs.dir(app)

    doLast {
        val root = app.get().asFile
        root.deleteRecursively()
        val macos = File(root, "Contents/MacOS").apply { mkdirs() }
        val resources = File(root, "Contents/Resources").apply { mkdirs() }
        // The Compose macOS resource reader looks up
        // `Contents/Resources/compose-resources/composeResources/<namespace>/...`
        // (NSBundle.resourcePath + "/compose-resources/" + the path, which itself starts
        // with "composeResources/").
        val composeResources = File(resources, "compose-resources/composeResources").apply { mkdirs() }
        binaryFile.get().asFile.copyTo(File(macos, "CoreApp"), overwrite = true)
        File(macos, "CoreApp").setExecutable(true)
        infoPlist.asFile.copyTo(File(root, "Contents/Info.plist"), overwrite = true)
        assembledDirs.forEach { dir ->
            val src = dir.get().asFile
            if (src.exists()) src.copyRecursively(composeResources, overwrite = true)
        }
        // Ad-hoc signing is enough for local use; Developer ID + notarization come with distribution.
        providers.exec {
            commandLine(
                "codesign", "--force", "--sign", "-", "--timestamp=none",
                "--entitlements", entitlements.asFile.absolutePath,
                root.absolutePath,
            )
        }.result.get()
        logger.lifecycle("Bundled ${root.absolutePath}")
    }
}
