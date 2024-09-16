import java.util.Properties

rootProject.name = "Capturable"

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

val envPropFile = file("env.properties")
if (envPropFile.exists()) {
    val props = Properties().apply {
        envPropFile.reader().use { rd ->
            load(rd)
        }
    }
    props.forEach { (k, v) ->
        ext.set(k.toString(), v)
    }
}

//include(":app")
include(":capturable")