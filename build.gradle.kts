import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
    `java-library`
}

group = "tw.edu.ntu.lads.chouguting"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Note, if you develop a library, you should use compose.desktop.common.
    // compose.desktop.currentOs should be used in launcher-sourceSet
    // (in a separate module for demo project and in testMain).
    // With compose.desktop.common you will also lose @Preview functionality

    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(files("libraries/json-20231013.jar"))
    implementation(platform("org.kotlincrypto.hash:bom:0.4.0"))
    implementation("org.kotlincrypto.hash:sha3")
}

tasks.compileJava {
    // use the project's version or define one directly

}


compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "LadsCryptoTestUtility"
            packageVersion = "1.0.0"

            windows {
                iconFile.set(project.file("icons/LaDS_icon.ico"))
            }
        }
        jvmArgs += "-splash:app/resources/loading.gif"
    }
}
