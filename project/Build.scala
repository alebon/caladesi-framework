/*
 * Copyright 2012 Caladesi Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt._
import Keys._
import Dependencies._


object BuildDef extends Build {

  val scalaToolsReleases = "Scala-Tools Releases Repository" at "http://scala-tools.org/repo-releases/"
  val orientRepositories = "Orient Technologies Maven2 Repository" at "https://oss.sonatype.org/content/repositories/releases/com/orientechnologies/"

  lazy val root = Project(id = "caladesi-framework",
                          base = file("."))
                    .aggregate(common, web, orientdb, neo4j)
                    .settings(publishArtifact := false)

  lazy val common =
    Project(id = "caladesi-common",
      base = file("caladesi-common"),
      settings = Project.defaultSettings)
      .settings(description := "Caladesi Framework Common Utilities‚",
      libraryDependencies ++= Seq(slf4j_api, scalacheck, specs2, junit))

  lazy val web =
     Project(id = "caladesi-web",
             base = file("caladesi-web"),
             settings = Project.defaultSettings)
       .settings(description := "Caladesi Framework Web Utilities",
                 libraryDependencies ++= Seq(slf4j_api, scalacheck, specs2, junit))

  lazy val orientdb =
    Project(id = "caladesi-orientdb",
            base = file("caladesi-orientdb"),
            settings = Project.defaultSettings)
      .settings(description := "Caladesi Framework OrientDB",
                libraryDependencies ++= Seq(orient_commons, orientdb_core, orientdb_client, slf4j_api, scalacheck, specs2, junit))
      .dependsOn(common)

  lazy val neo4j =
    Project(id = "caladesi-neo4j",
            base = file("caladesi-neo4j"),
            settings = Project.defaultSettings)
      .settings(description := "Caladesi Framework Neo4j",
                libraryDependencies ++= Seq(neo4j_all, slf4j_api, scalacheck, specs2, junit))
      .dependsOn(common)
}
