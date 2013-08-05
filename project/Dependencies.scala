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

object Dependencies {

  lazy val scalaDepVersion   = "2.10.1"
  lazy val slf4jVersion   = "1.7.2"
  lazy val liftVersion    = "2.5"
  lazy val orientVersion  = "1.5"
  lazy val neo4jVersion   = "1.9"
  lazy val logbackVersion = "1.0.6"
  lazy val elasticVersion = "0.90.2"

  // Compile scope:
  // Scope available in all classpath, transitive by default.
  lazy val scalaLib             = "org.scala-lang" % "scala-library"	           % scalaDepVersion
  lazy val slf4j_api            = "org.slf4j"      % "slf4j-api"                 % slf4jVersion
  lazy val lift_common          = "net.liftweb"    %% "lift-common"         % liftVersion

  // OrientDB
  lazy val orient_commons       = "com.orientechnologies" % "orient-commons" % orientVersion
  lazy val orientdb_core        = "com.orientechnologies" % "orientdb-core" % orientVersion
  lazy val orientdb_client      = "com.orientechnologies" % "orientdb-client" % orientVersion

  // Neo4j
  lazy val neo4j_all		= "org.neo4j" % "neo4j" % neo4jVersion

  // Elastic search
  lazy val elastic = "org.elasticsearch" % "elasticsearch" % elasticVersion

  // Provided scope:
  lazy val logback = "ch.qos.logback"    % "logback-classic"     % logbackVersion % "provided"

  // Provided scope:

  // Runtime scope:
  // Scope provided in runtime, available only in runtime and test classpath, not compile classpath, non-transitive by default.

  // Test scope:
  // Scope available only in test classpath, non-transitive by default.
  lazy val mockito_all = "org.mockito"     % "mockito-all"        % "1.8.5"      % "test"
//  lazy val scalacheck  = "org.scalacheck"  %% "scalacheck"   % "1.10.1"        % "test"
  lazy val specs2      = "org.specs2"      %% "specs2"   	  % "2.0"       % "test"
  lazy val junit       = "junit"           % "junit"              % "4.11"        % "test"
}
