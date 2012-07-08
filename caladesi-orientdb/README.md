#Caladesi Framework OrientDB Component

The Caladesi Framework OrientDB Component is written in Scala and is intended to be used in other Scala projects. It is
an abstraction layer for the fabulous OrientDB.

##The Idea

The main goal is to create a simple Scala API for OrientDB. There will be an ActiveRecord approach as well as a
Repository implementation.

The following (Repository) examples are meant to be a draft:

```scala
class ShipEntity extends OrientGraphEntity with UuidPk {

  object name extends StringField(this, 150) with IndexedFulltext
}

class ContainerEntity extends OrientGraphEntity with UuidPk {

  object color extends StringField(this, 100)

}
```

```scala
class ShipRepository extends OrientGraphRepository[ShipEntity] {
  // Override methods here
}
```

Usage in your BL code:

```scala
val single = shipRepository.findBy(ShipEntity._uuid, UUID.fromString("048b080c-8ca1-429e-a640-138d928a8ecd"))

// All green container assigned to ship with uuid 048b080c-8ca1-429e-a640-138d928a8ecd
val list = containerRepository.findAll(ContainerEntity~>ShipEntity)
            .filter(ShipEntity._uuid, UUID.fromString("048b080c-8ca1-429e-a640-138d928a8ecd",
                    ContainerEntity.color, "green"))
```

##Getting Started with Caladesi Framework OrientDB
You can use the OrientDB Component by adding the dependency to your project:

### SBT 0.11.3 (Simple Build Tool)
Modify your build.sbt

    libraryDependencies += "net.caladesiframework" % "caladesi-orientdb_2.9.1" % "0.1.0" % "compile"

###Maven:
Add the framework to your pom.xml:

```xml
<dependency>
  <groupId>net.caladesiframework</groupId>
  <artifactId>caladesi-orientdb_${scala.version}</artifactId>
  <version>0.1.0</version>
</dependency>
```

###Gralde
```groovy
dependencies {
    // Caladesi Framework
    compile "net.caladesiframework:caladesi-orientdb_$scalaVersion:0.1.0"
}
```

##Licence

The Caladesi Framework is open source software released under the Apache 2.0 license. You must be a committer to submit patches.
