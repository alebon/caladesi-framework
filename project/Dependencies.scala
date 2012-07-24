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

  lazy val scalaVersion  = "2.9.1"
  lazy val slf4jVersion  = "1.6.4"
  lazy val liftVersion   = "2.4"
  lazy val orientVersion = "1.1.0"

  // Compile scope:
  // Scope available in all classpath, transitive by default.
  lazy val scalaLib             = "org.scala-lang" % "scala-library"	           % scalaVersion
  lazy val slf4j_api            = "org.slf4j"      % "slf4j-api"                 % slf4jVersion
  lazy val lift_common          = "net.liftweb"    % "lift-common_$scalaVersion" % liftVersion

  // OrientDB
  lazy val orient_commons       = "com.orientechnologies" % "orient-commons" % orientVersion
  lazy val orientdb_core        = "com.orientechnologies" % "orientdb-core" % orientVersion
  lazy val orientdb_client      = "com.orientechnologies" % "orientdb-client" % orientVersion

  // Blueprints
  //lazy val orientdb_core        = "com.tinkerpop.blueprints" % "orientdb-core" % orientVersion


  // Provided scope:

  // Runtime scope:
  // Scope provided in runtime, available only in runtime and test classpath, not compile classpath, non-transitive by default.

  // Test scope:
  // Scope available only in test classpath, non-transitive by default.
  lazy val mockito_all = "org.mockito"     % "mockito-all"        % "1.8.5"      % "test"
  //lazy val scalatest   = "org.scalatest "  % "scalatest_2.9.1"    % "1.8"      % "test"
  lazy val scalacheck  = "org.scalacheck"  % "scalacheck_2.9.1"   % "1.9"        % "test"
  lazy val specs2      = "org.specs2"      % "specs2_2.9.1"   	  % "1.11"       % "test"
  lazy val junit       = "junit"           % "junit"              % "4.5"        % "test"

}
