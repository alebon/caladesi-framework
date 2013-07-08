#Caladesi Framework Neo4j Component

The Caladesi Framework Neo4j Component is written in Scala and is intended to be used in other Scala projects. It is
an abstraction layer for the fabulous Neo4j Database.

##The Neo4j Entity Repository

The main goal of the repository is to create a simple Scala API for Neo4j. You can perform CRUD operation on the
defined entities and search for entities. Furthermore - to make use of a graph database - you can define relationships
between entities and traverse them.

The following examples should give you an idea of how to use the caladesi framework to define graph based repositories:

###Defining the entities

```scala
class ShipEntity extends Neo4jGraphEntity with UuidPk {

  // We want the ship to have a name that we can search for
  object name extends StringField(this) with FulltextIndexed

  // Color of the ship
  object color extends StringField(this)

  // Ships have a weight and we name the inner property "weight"
  object weight extends DoubleField(this) {
    // That is the property name that is stored in DB later on
    override def name = "_some_weight"
  }
}

class ContainerEntity extends Neo4jGraphEntity {

  object color extends StringField(this)

  // This property is not required, our ship may have a label
  object label extends OptionalStringField(this)

}
```

###Defining the Repository

```scala
class ShipRepository(implicit config: Neo4jConfiguration) extends Neo4jGraphRepository[ShipEntity] {

  // This will be the name of the relation that is pointing to the Sub_Reference node
  override def RELATION_NAME = "SHIP"

}
```

The default configuration tells the repository to use "ENTITY" as relation for our entities. To
override this just set a different RELATION_NAME.

Usage in your BL code:

```scala
val shipRepository = new ShipRepository()
shipRepository.init

// Find one ship entity from index where uuid equals "048b080c-8ca1-429e-a640-138d928a8ecd"
val uuid = UUID.fromString("048b080c-8ca1-429e-a640-138d928a8ecd")
val single = shipRepository.findIdx(ShipEntity._uuid, uuid)
```

###Defining directed 1..1, 1..n and 0..1 relationships

```scala
class ShipEntity extends Neo4jGraphEntity {

  // Defines 1:1 directed relations: Ship -[:SPECIAL]-> Container
  object specialContainer extends RelatedToOne[ContainerEntity](this, "SPECIAL")

  // Define 0..1 directed relation: Ship-[OWNED_BY]->Company
  object company extends OptionalRelatedToOne[CompanyEntity](this, "OWNED_BY")

  // Define 1..n directed relations: Ship-[:TRANSPORTS]->Container
  object containerSet extends RelatedToMany[ContainerEntity](this, "TRANSPORTS")
}

// Create and assign the entities in your BL
val container = containerRepository.create
containerRepository.update(container) // This is handled in an own transaction

val ship = shipRepository.create
ship.specialContainer.set(container)
ship.containerSet.put(container)

// Update the entity (creating relations and saving properties)
shipRepository.update(ship)
```

By using the OptionalRelatedToOne relation you don't need to define a relationship. By calling the ".is" method on
an optional relation an Option(Some or None) will be returned. You can reset an optional field by using ".clear" on it.

##Getting Started with Caladesi Framework Neo4j
You can use the Neo4j Component by adding the dependency to your project.

### SBT 0.12.3 (Simple Build Tool)
Modify your build.sbt

    libraryDependencies += "net.caladesiframework" %% "caladesi-common" % "0.5.1" % "compile"

    libraryDependencies += "net.caladesiframework" %% "caladesi-neo4j-graph" % "0.5.1" % "compile"

###Maven:
Add the framework to your pom.xml:

```xml
<dependency>
  <groupId>net.caladesiframework</groupId>
  <artifactId>caladesi-common_${scala.version}</artifactId>
  <version>0.5.1</version>
</dependency>

<dependency>
  <groupId>net.caladesiframework</groupId>
  <artifactId>caladesi-neo4j-graph_${scala.version}</artifactId>
  <version>0.5.1</version>
</dependency>
```

###Gradle
```groovy
dependencies {
    // Caladesi Framework
    compile "net.caladesiframework:caladesi-common_2.10.1:0.5.1",
        "net.caladesiframework:caladesi-neo4j-graph_2.10.1:0.5.1"
}
```

##License

The Caladesi Framework is open source software released under the Apache 2.0 license.
Neo4j is shipped with the GPL/AGPL license. Please be aware of it at runtime level and check the Neo4j license page for
details!