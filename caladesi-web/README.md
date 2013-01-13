#Caladesi Framework Web Component

The Caladesi Framework Web Component is written in Scala and is intended to be used in other Scala projects. It is
lib of useful web tools.

##Slugify

The Slugify helper transforms a given string into a clean URL slug.

```scala
val slug = Slugify.string("A small Red TRUCK")

// Will print "a-small-red-truck"
println(slug)
```

##Getting Started with Caladesi Framework Web Component
You can use the Web Component by adding the dependency to your project:

### SBT 0.11.3 (Simple Build Tool)
Modify your build.sbt

    libraryDependencies += "net.caladesiframework" % "caladesi-web_2.9.1" % "0.4.0-SNAPHOT" % "compile"

###Maven:
Add the framework to your pom.xml:

```xml
<dependency>
  <groupId>net.caladesiframework</groupId>
  <artifactId>caladesi-web_2.9.1</artifactId>
  <version>0.4.0-SNAPHOT</version>
</dependency>
```

###Gradle
```groovy
dependencies {
    // Caladesi Framework
    compile "net.caladesiframework:caladesi-web_2.9.1:0.4.0-SNAPHOT"
}
```

##License

The Caladesi Framework is open source software released under the Apache 2.0 license.
