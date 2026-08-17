plugins {
    id("com.android.application") version "9.3.1" apply false
}

val externalBuildRoot = file("C:/Users/skheg/.gradle-builds/MovInBuddy")

allprojects {
    layout.buildDirectory.set(externalBuildRoot.resolve(if (path == ":") "root" else project.name))
}
