package app.roborock.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val ROBOROCK = Compatibility(
        name = "Roborock",
        packageName = "com.roborock.smart",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x1E88E5,   // 0x00RRGGBB — alpha byte MUST be 0x00
        targets = listOf(
            AppTarget(version = "4.74.04")
        )
    )
}
