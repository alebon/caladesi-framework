#Caladesi Framework OrientDB Document

The Caladesi Framework OrientDB Document Component is written in Scala and is intended to be used in other Scala projects. It is
an abstraction layer for the fabulous OrientDB.

##The OrientDB Record

The main goal of the record is to create a simple Scala API for OrientDB. You can perform CRUD operation on the
defined entities and use the caladesi query language for searching for entities.

The following examples should give you an idea of how to use the caladesi framework to define record entities for OrientDB:

###Defining the entities

```scala
class Ship extends OrientRecord[Ship] with UuidPk {

  // We need a meta record implementation for CRUD operations
  def meta = Ship

  // We want the ship to have a name that we can search for
  object name extends StringField(this) with FulltextIndexed

  // A string field that is indexed unique
  object publicIdentifier extends StringField(this) with ExactIndexed

  // Color of the ship (a simple string field)
  object color extends StringField(this)

  // Ships have a weight. You can define the name of the attribute in db by overriding the name method
  object weight extends DoubleField(this) {
    // That is the property name that is stored in DB later on
    override def name = "_w"
  }
}

object Ship extends Ship with OrientMetaRecord[Ship]

class Container extends OrientRecord[Container] with UuidPk {

  object color extends StringField(this)

}

object Container extends Container with OrientMetaRecord[Container]
```

Usage in your BL code:

```scala
// Create a new record
val ship = ShipEntity.createRecord

// Set properties of the record
ship.color.set("red")
ship.name.set("The Queen")
ship.publicIdentifier.set("the-queen-2912")

// Persist record
ship.save

// Find one ship entity where uuid equals "048b080c-8ca1-429e-a640-138d928a8ecd"
val shipOption = Ship.findById("048b080c-8ca1-429e-a640-138d928a8ecd")

// Match
shipOption match {
    case Some(ship) => // Do something with ship
    case _ => // Ship with this UUID not found
}
```

##Caladesi Query Language Sample
```scala
// Fetching green ships with pagination
val greenShips = Ship find where Ship.color eqs "green" skip 5 limit 5 ex

// Find single ship with color green and name Lola
val greenLola = (Ship find where Ship.color eqs "green" and Ship.name eqs "Lola" limit 1 ex).head

// Searching the fulltext indexed fields
val redShips = Ship.findIdx where Ship.name contains "red" limit 10 ex
```

##Getting Started with Caladesi Framework OrientDB
You can use the OrientDB Component by adding the dependency to your project.

### SBT 0.12.3 (Simple Build Tool)
Modify your build.sbt

    libraryDependencies += "net.caladesiframework" %% "caladesi-common" % "0.7.0-SNAPSHOT" % "compile"

    libraryDependencies += "net.caladesiframework" %% "caladesi-orientdb-document" % "0.7.0-SNAPSHOT" % "compile"

###Maven:
Add the framework to your pom.xml:

```xml
<dependency>
  <groupId>net.caladesiframework</groupId>
  <artifactId>caladesi-common_${scala.version}</artifactId>
  <version>0.7.0-SNAPSHOT</version>
</dependency>

<dependency>
  <groupId>net.caladesiframework</groupId>
  <artifactId>caladesi-orientdb-document_${scala.version}</artifactId>
  <version>0.7.0-SNAPSHOT</version>
</dependency>
```

###Gradle
```groovy
dependencies {
    // Caladesi Framework
    compile "net.caladesiframework:caladesi-common_2.10.1:0.7.0-SNAPSHOT",
        "net.caladesiframework:caladesi-orientdb-document_2.10.1:0.7.0-SNAPSHOT"
}
```

##License

The Caladesi Framework is open source software released under the Apache 2.0 license.
