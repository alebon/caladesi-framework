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

  lazy val scalaVersion = "2.9.1"
  lazy val slf4jVersion = "1.6.4"
  lazy val liftVersion  = "2.4"

  // Compile scope:
  // Scope available in all classpath, transitive by default.
  lazy val scalaLib  	    	  = "org.scala-lang" % "scala-library"	           % scalaVersion
  lazy val slf4j_api              = "org.slf4j"      % "slf4j-api"                 % slf4jVersion
  lazy val lift_common            = "net.liftweb"    % "lift-common_$scalaVersion" % liftVersion

  // Provided scope:

  // Runtime scope:
  // Scope provided in runtime, available only in runtime and test classpath, not compile classpath, non-transitive by default.

  // Test scope:
  // Scope available only in test classpath, non-transitive by default.
  lazy val mockito_all = "org.mockito"     % "mockito-all"                % "1.8.5"      % "test"
  lazy val scalacheck  = "org.scalacheck"  % "scalacheck_$scalaVersion"   % "1.9"        % "test"
  lazy val specs2      = "org.specs2"      % "specs2_$scalaVersion"   	  % "1.11"       % "test"

}
