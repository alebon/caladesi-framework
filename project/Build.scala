/*
 * Copyright 2013 Caladesi Framework
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
                    .aggregate(common, field, repository, record, web, orientdbcommon, orientdbdocument, orientdbgraph, neo4jgraph)
                    .settings(publishArtifact := false)

  lazy val common =
    Project(id = "caladesi-common",
      base = file("caladesi-common"),
      settings = Project.defaultSettings)
      .settings(description := "Caladesi Framework Common Utilities",
      libraryDependencies ++= Seq(slf4j_api, scalacheck, specs2, junit, logback))

  lazy val field =
    Project(id = "caladesi-field",
      base = file("caladesi-field"),
      settings = Project.defaultSettings)
      .settings(description := "Caladesi Framework Field",
      libraryDependencies ++= Seq(slf4j_api, scalacheck, specs2, junit, logback))

  lazy val repository =
    Project(id = "caladesi-repository",
      base = file("caladesi-repository"),
      settings = Project.defaultSettings)
      .settings(description := "Caladesi Framework Repository",
      libraryDependencies ++= Seq(slf4j_api, scalacheck, specs2, junit, logback))

  lazy val record =
    Project(id = "caladesi-record",
      base = file("caladesi-record"),
      settings = Project.defaultSettings)
      .settings(description := "Caladesi Framework Record",
      libraryDependencies ++= Seq(slf4j_api, scalacheck, specs2, junit, logback))

  lazy val web =
     Project(id = "caladesi-web",
             base = file("caladesi-web"),
             settings = Project.defaultSettings)
       .settings(description := "Caladesi Framework Web Utilities",
                 libraryDependencies ++= Seq(slf4j_api, scalacheck, specs2, junit))

  lazy val orientdbcommon =
    Project(id = "caladesi-orientdb-common",
      base = file("caladesi-orientdb-common"),
      settings = Project.defaultSettings)
      .settings(description := "Caladesi Framework OrientDB Common",
      libraryDependencies ++= Seq(orient_commons, orientdb_core, orientdb_client, slf4j_api, scalacheck, specs2, junit))
      .dependsOn(common, field, repository, record)

  lazy val orientdbdocument =
    Project(id = "caladesi-orientdb-document",
      base = file("caladesi-orientdb-document"),
      settings = Project.defaultSettings)
      .settings(description := "Caladesi Framework OrientDB Document",
      libraryDependencies ++= Seq(slf4j_api, scalacheck, specs2, junit))
      .dependsOn(common, field, repository, record, orientdbcommon)

  lazy val orientdbgraph =
    Project(id = "caladesi-orientdb-graph",
            base = file("caladesi-orientdb-graph"),
            settings = Project.defaultSettings)
      .settings(description := "Caladesi Framework OrientDB Graph",
                libraryDependencies ++= Seq(orient_commons, orientdb_core, orientdb_client, slf4j_api, scalacheck, specs2, junit))
      .dependsOn(common)

  lazy val neo4jgraph =
    Project(id = "caladesi-neo4j-graph",
            base = file("caladesi-neo4j-graph"),
            settings = Project.defaultSettings)
      .settings(description := "Caladesi Framework Neo4j Graph",
                libraryDependencies ++= Seq(neo4j_all, slf4j_api, scalacheck, specs2, junit))
      .dependsOn(common)
}
