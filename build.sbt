import Dependencies._

organization in ThisBuild	:= "net.caladesiframework"

scalaVersion in ThisBuild       := "2.10.1"

version in ThisBuild		:= "0.7.0-SNAPSHOT"

homepage in ThisBuild		:= Some(url("http://caladesiframework.net"))

licenses in ThisBuild		+= ("Apache License, Version 2.0", url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

startYear in ThisBuild		:= Some(2012)

credentials in ThisBuild        += Credentials(Path.userHome / ".ivy2" / ".credentials")

publishMavenStyle in ThisBuild  := true

resolvers in ThisBuild          ++= Seq("oss-snapshots"     at "http://oss.sonatype.org/content/repositories/snapshots",
                                        "oss-releases"        at "http://oss.sonatype.org/content/repositories/releases")

publishTo in ThisBuild <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) 
    Some("snapshots" at nexus + "content/repositories/snapshots") 
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomIncludeRepository in ThisBuild := { x => false }

pomExtra in ThisBuild             := (
  <scm>
    <url>git@github.com:alebon/caladesi-framework.git</url>
    <connection>scm:git:git@github.com:alebon/caladesi-framework.git</connection>
  </scm>
  <developers>
    <developer>
      <id>abondarenko</id>
      <name>Alexej Bondarenko</name>
      <url>http://caladesiframework.net</url>
    </developer>
  </developers>
)

libraryDependencies in ThisBuild += "org.scalatest" %% "scalatest" % "1.9.1" % "test"

libraryDependencies += "junit" % "junit" % "4.11" % "test"
