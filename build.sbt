import android.Keys._
import android.Dependencies.aar

android.Plugin.androidBuild

name := "calendarlookup"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.0" % "test",
  "net.whily" %% "scasci" % "0.0.1-SNAPSHOT",
  "net.whily" %% "scaland" % "0.0.1-SNAPSHOT",
  "net.whily" %% "chinesecalendar" % "0.1.0-SNAPSHOT")

platformTarget in Android := "android-22"

proguardCache in Android += "net.whily.scasci"
proguardCache in Android += "net.whily.scaland"

proguardOptions in Android ++= Seq(
  "-dontobfuscate",
  "-dontoptimize",
  "-keepattributes Signature,InnerClasses,EnclosingMethod",
  "-dontwarn scala.collection.**",
  "-dontwarn sun.misc.Unsafe",
  "-keep class net.whily.android.calendarlookup.** { *; }",
  "-keep class net.whily.android.chinesecalendar.** { *; }",  
  "-keep class scala.collection.SeqLike { public java.lang.String toString(); }")

scalacOptions in Compile ++= Seq("-feature", "-deprecation", "-Xexperimental")

javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.6", "-target", "1.6")

javacOptions in Compile  += "-deprecation"

run <<= run in Android

install <<= install in Android

