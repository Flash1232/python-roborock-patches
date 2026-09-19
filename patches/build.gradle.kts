group = "app.roborock"

patches {
    about {
        name = "Python-roborock Patches"
        description = "roborock_local_server patches for the Roborock app"
        source = "git@github.com:Flash1232/python-roborock-patches.git"
        author = "Flash1232"
        contact = "na"
        website = "https://github.com/Flash1232/python-roborock-patches"
	license = "GPLv3"
    }
}

// Separate configuration so gson is available at runtime for the
// generatePatchesList task but never bundled into the APK.
val patchListGeneratorClasspath = configurations.create("patchListGeneratorClasspath")

dependencies {
    compileOnly(libs.gson)
    patchListGeneratorClasspath(libs.gson)
}

tasks {
    register<JavaExec>("generatePatchesList") {
        description = "Build patch with patch list"

        dependsOn(build)

        classpath = sourceSets["main"].runtimeClasspath + patchListGeneratorClasspath
        mainClass.set("util.PatchListGeneratorKt")
    }

    // Used by gradle-semantic-release-plugin.
    publish {
        dependsOn("generatePatchesList")
    }
}
