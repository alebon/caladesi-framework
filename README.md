#Welcome to the Caladesi Framework

The Caladesi Framework is written in Scala and is intended to be used in other Scala projects. It is inspired by the liftweb framework. 
Hence it uses some parts of it (lift-json, ...).


###Components

Please check the sub folders/projects for detailed information

- caladesi-common
- caladesi-field
- caladesi-record
- caladesi-repository
- caladesi-neo4j-graph
- caladesi-orientdb-common
- caladesi-orientdb-graph
- caladesi-orientdb-document
- caladesi-web

##Getting Started with Caladesi Framework
You can use the caladesi framework by adding the dependency to your project:

### SBT 0.12.3 (Simple Build Tool)
Modify your build.sbt

    libraryDependencies += "net.caladesiframework" %% "caladesi-web" % "0.5.4" % "compile"

###Maven:
Add the framework to your pom.xml:

```xml
<dependency>
  <groupId>net.caladesiframework</groupId>
  <artifactId>caladesi-web_${scala.version}</artifactId>
  <version>0.5.4</version>
</dependency>
```

###Gradle
```groovy
dependencies {
    // Caladesi Framework
    compile "net.caladesiframework:caladesi-web_$scalaVersion:0.5.4"
}
```

## Building Caladesi Framework
To build the caladesi framework from source, checkout this repository and use the included `caladesi` script.

    git clone https://github.com/alebon/caladesi-framework.git
    cd caladesi-framework
    ./caladesi

##License

The Caladesi Framework is open source software released under the Apache 2.0 license. You must be a committer to submit patches.
