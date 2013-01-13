#Caladesi Framework OrientDB Component

The Caladesi Framework OrientDB Component is written in Scala and is intended to be used in other Scala projects. It is
an abstraction layer for the fabulous OrientDB.

##The OrientDB Entity Repository

The main goal of the repository is to create a simple Scala API for OrientDB. You can perform CRUD operation on the
defined entities and use the caladesi query language for searching for entities. Furthermore - to make use of a graph
database - you can define relationships between entities and traverse them.

The following examples should give you an idea of how to use the caladesi framework to define graph based repositories:

###Defining the entities

```scala
class ShipEntity extends OrientGraphEntity with UuidPk {

  // We want the ship to have a name that we can search for
  object name extends StringField(this) with FulltextIndexed

  object uniqueTag extends StringField(this) with ExactIndexed

  // Color of the ship
  object color extends StringField(this)

  // Ships have a weight and we name the inner property "weight"
  object weight extends DoubleField(this) {
    // That is the property name that is stored in DB later on
    override def name = "weight"
  }
}

class ContainerEntity extends OrientGraphEntity with UuidPk {

  object color extends StringField(this)

}
```

###Defining the Repository

```scala
class ShipRepository extends OrientGraphRepository[ShipEntity] {
  // This part will be removed in future versions
  override def dbName = "db"
  override def dbType = "remote"
  override def dbUser = "admin"
  override def dbPassword = "admin"

  // Override this to use own names or use same repositories with different entity sets
  override def repositoryEntityClass = "OShipEntityCustom"
}
```

The default configuration tells the repository to use the class name of the entity as the class inside orientdb. To
override this (maybe you want to define two different repositories for the same entity class) just override the method
"repositoryEntityClass".

Usage in your BL code:

```scala
val shipRepository = new ShipRepository()
shipRepository.init

// Find one ship entity where uuid equals "048b080c-8ca1-429e-a640-138d928a8ecd"
val single = (shipRepository.find where ShipEntity._uuid eqs UUID.fromString("048b080c-8ca1-429e-a640-138d928a8ecd" limit 1 ex)).head

// All green container assigned to ship with uuid 048b080c-8ca1-429e-a640-138d928a8ecd
val list = containerRepository.findAll(ContainerEntity~>ShipEntity)
            .filter(ShipEntity._uuid, UUID.fromString("048b080c-8ca1-429e-a640-138d928a8ecd",
                    ContainerEntity.color, "green"))
```

###Defining directed 1..1 relationships and 0..1
```scala
class ShipEntity extends OrientGraphEntity with UUIDPk {

  // Defines 1:1 directed relations: Ship -[SPECIAL]-> Container
  object specialContainer extends RelatedToOne[ContainerEntity](this, "SPECIAL")

  // Define 0..1 directed relation: Ship-[OWNED_BY]->Company
  object company extends OptionalRelatedToOne[CompanyEntity](this, "OWNED_BY")
}

// Create and assign the entities in your BL
val container = containerRepository.create
containerRepository.update(container) // This is handled in an own transaction

val ship = shipRepository.create
ship.specialContainer.set(container)

// Wires the ship with the container: Ship -["SPECIAL"]-> Container
shipRepository.update(ship) // The second transaction contains the creation of the directed edge
```

By using the OptionalRelatedToOne relation you don't need to define a relationship. By calling the ".isOpt" method on
an optional relation an Option(Some or None) will be returned.

##Caladesi Query Language Sample
```scala
// Fetching green ships with pagination
val greenShips = shipRepository find where ShipEntity.color eqs "green" skip 5 limit 5 ex

// Find single ship with color green and name Lola
val greenLola = (shipRepository find where ShipEntity.color eqs "green" and ShipEntity.name eqs "Lola" limit 1 ex).head

// Check for related company (we assume that greenLola was found)
greenLola.company.isOpt match {
  case Some(company) => // Do something with the company
  case None => // There is no company related
}

// Searching the indexed fields
val redShips = shipRepository.findIdx where ShipEntity.name contains "red" limit 10 ex
```

##Getting Started with Caladesi Framework OrientDB
You can use the OrientDB Component by adding the dependency to your project.

### SBT 0.11.3 (Simple Build Tool)
Modify your build.sbt

    libraryDependencies += "net.caladesiframework" % "caladesi-common_2.9.1" % "0.4.0-SNAPHOT" % "compile"

    libraryDependencies += "net.caladesiframework" % "caladesi-orientdb_2.9.1" % "0.4.0-SNAPHOT" % "compile"

###Maven:
Add the framework to your pom.xml:

```xml
<dependency>
  <groupId>net.caladesiframework</groupId>
  <artifactId>caladesi-common_${scala.version}</artifactId>
  <version>0.4.0-SNAPHOT</version>
</dependency>

<dependency>
  <groupId>net.caladesiframework</groupId>
  <artifactId>caladesi-orientdb_${scala.version}</artifactId>
  <version>0.4.0-SNAPHOT</version>
</dependency>
```

###Gradle
```groovy
dependencies {
    // Caladesi Framework
    compile "net.caladesiframework:caladesi-common_2.9.1:0.4.0-SNAPHOT",
        "net.caladesiframework:caladesi-orientdb_2.9.1:0.4.0-SNAPHOT"
}
```

##License

The Caladesi Framework is open source software released under the Apache 2.0 license.
