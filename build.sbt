name := "bump-to-exchange"

import android.Keys._
android.Plugin.androidBuild
javacOptions ++= Seq("-source", "1.7", "-target", "1.7")
scalaVersion := "2.11.7"
scalacOptions in Compile ++= Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8")

libraryDependencies += "org.scaloid" %% "scaloid" % "4.0"
libraryDependencies += "com.github.max-leuthaeuser" % "scroll_2.11" % "0.9.1"
libraryDependencies += "de.hdrmrks.bte" % "bumptoexchange_2.11" % "0.1.1"


proguardCache in Android ++= Seq("org.scaloid")
proguardCache in Android ++= Seq("de.hdrmrks")

proguardCache in Android ++= Seq("com.github.max-leuthaeuser")

proguardOptions in Android ++= Seq("-dontobfuscate", "-dontoptimize", "-keepattributes Signature", "-printseeds target/seeds.txt", "-printusage target/usage.txt"
  , "-dontwarn scala.collection.**" // required from Scala 2.11.4
  , "-dontwarn org.scaloid.**" // this can be omitted if current Android Build target is android-16
  , "-dontobfuscate",
  "-dontoptimize",
  "-keepattributes Signature,InnerClasses,EnclosingMethod",
  "-dontwarn **",
  "-dontnote **",
  // for reflections
  "-keep class scala.AnyVal",
  "-keep class scala.Array",
  "-keep class scala.Boolean",
  "-keep class scala.Byte",
  "-keep class scala.Char",
  "-keep class scala.Double",
  "-keep class scala.Float",
  "-keep class scala.Int",
  "-keep class scala.Long",
  "-keep class scala.Short",
  "-keep class scala.Unit",
  "-keep class scala.reflect.**",
  "-keepclassmembers class * { ** item; ** bytes(); }"

)
run <<= run in Android
install <<= install in Android

retrolambdaEnable in Android := false // turning it on significantly increases the build time
