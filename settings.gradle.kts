import java.util.Properties

val properties = Properties()
if (file("local.properties").exists()) {
    file("local.properties").inputStream().use { properties.load(it) }
}

pluginManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        google()
        mavenCentral()
        gradlePluginPortal()
        maven { url = uri("https://jitpack.io") }
    }
}

dependencyResolutionManagement {
    repositories {
        if (properties.getProperty("USE_MAVEN_LOCAL") == "true") {
            mavenLocal()
        }
        maven(url = "https://jitpack.io")
        mavenCentral()
        google()
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        mavenLocal()
    }
}

rootProject.name = "libpebbleroot"

include(":libpebble3")
include(":blobdbgen")
include(":blobannotations")
include(":toolschemagen")
include(":toolschemaannotations")
include(":composeApp")
include(":androidApp")
include(":pebble")
include(":util")
include(":mcp")
include(":index-ai")
include(":resampler")
include(":cactus")
include(":cactus-native")
include(":libindex")
include(":experimental")
include(":krisp-stubs")
include(":macos-stubs")
include(":macosApp")
