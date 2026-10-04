// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.google.services) apply false
}

tasks.register("prepareDistributionApk") {
  dependsOn(":app:assembleDebug")
  val releaseApk = layout.projectDirectory.file("app/build/outputs/apk/release/app-release.apk").asFile
  val debugApk = layout.projectDirectory.file("app/build/outputs/apk/debug/app-debug.apk").asFile
  val targetReleaseOut = layout.projectDirectory.file("app/build/outputs/apk/release/OMKAR-AUTOMATIC-VIDEO-MAKER.apk").asFile
  val targetDebugOut = layout.projectDirectory.file("app/build/outputs/apk/debug/OMKAR-AUTOMATIC-VIDEO-MAKER.apk").asFile
  val targetRootOut = layout.projectDirectory.file("app/build/outputs/apk/OMKAR-AUTOMATIC-VIDEO-MAKER.apk").asFile

  doLast {
    val sourceApk = when {
      releaseApk.exists() && releaseApk.length() > 0L && releaseApk.lastModified() >= debugApk.lastModified() -> releaseApk
      debugApk.exists() && debugApk.length() > 0L -> debugApk
      else -> throw GradleException("No built APK found to package as OMKAR-AUTOMATIC-VIDEO-MAKER.apk")
    }

    listOf(targetReleaseOut, targetDebugOut, targetRootOut).forEach { target ->
      target.parentFile?.mkdirs()
      if (target.exists()) {
        target.delete()
      }
      sourceApk.copyTo(target, overwrite = true)
      println("Packaged fresh APK: ${target.absolutePath} (${target.length()} bytes)")
    }
  }
}

