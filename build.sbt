import Dependencies._

organization in ThisBuild	:= "net.caladesiframework"

version in ThisBuild		:= "0.1.0-SNAPSHOT"

homepage in ThisBuild		:= Some(url("http://caladesiframework.net"))

licenses in ThisBuild		+= ("Apache License, Version 2.0", url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

startYear in ThisBuild		:= Some(2012)

libraryDependencies += "org.scalatest" % "scalatest_2.9.1" % "1.8" % "test"

libraryDependencies += "junit" % "junit" % "4.5" % "test"
