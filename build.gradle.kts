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
    implementation(compose.desktop.windows_x64)
    implementation(compose.material3)
    implementation(files("libraries/json-20231013.jar"))
    implementation(files("libraries/jSerialComm-2.10.4.jar"))
    implementation(platform("org.kotlincrypto.hash:bom:0.4.0"))
    implementation("org.kotlincrypto.hash:sha3")
    implementation("org.bouncycastle:bcprov-jdk15on:1.68")
    implementation ("org.bouncycastle:bcpkix-jdk15on:1.68")
//    implementation("org.jetbrains.kotlin:kotlin-reflect")

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
            copyright = "© 2024 Ku-Ting, Chou. All rights reserved."
            packageVersion = "1.0.1"

            windows {
                iconFile.set(project.file("icons/LaDS_icon.ico"))
                //add dlls to the distribution
                appResourcesRootDir.set(project.layout.projectDirectory.dir("resources")) //把外面那層的resources資料夾的內容放到exe裡面
                shortcut = true //在桌面上建立捷徑
                upgradeUuid = "5f8dcd2d-810b-405c-ad62-ccb90cdcdfff" //安裝時的uuid
                dirChooser = true  //在安裝時是否要選擇安裝路徑
//                console = true  打開時會有cmd視窗
                menuGroup = "LaDS"  //在開始選單裡面的資料夾名稱

            }
        }
        jvmArgs += "-splash:app/resources/loading.gif"
    }
}
