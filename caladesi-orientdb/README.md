#Caladesi Framework OrientDB Component

The Caladesi Framework OrientDB Component is written in Scala and is intended to be used in other Scala projects. It is
an abstraction layer for the fabulous OrientDB.

##The OrientDB Entity Repository

The main goal of the repository is to create a simple Scala API for OrientDB. You can perform CRUD operation on the
defined entities.

The following (Repository) examples are not final:

###Defining the entities

```scala
class ShipEntity extends OrientGraphEntity with UuidPk {

  // We want the ship to have a name
  object name extends StringField(this)

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
}
```

Usage in your BL code (draft, not implemented yet):

```scala
val shipRepository = new ShipRepository()
shipRepository.init

val single = shipRepository.findBy(ShipEntity._uuid, UUID.fromString("048b080c-8ca1-429e-a640-138d928a8ecd"))

// All green container assigned to ship with uuid 048b080c-8ca1-429e-a640-138d928a8ecd
val list = containerRepository.findAll(ContainerEntity~>ShipEntity)
            .filter(ShipEntity._uuid, UUID.fromString("048b080c-8ca1-429e-a640-138d928a8ecd",
                    ContainerEntity.color, "green"))
```

##Getting Started with Caladesi Framework OrientDB
You can use the OrientDB Component by adding the dependency to your project.

### SBT 0.11.3 (Simple Build Tool)
Modify your build.sbt

    libraryDependencies += "net.caladesiframework" % "caladesi-common_2.9.1" % "0.1.0" % "compile"

    libraryDependencies += "net.caladesiframework" % "caladesi-orientdb_2.9.1" % "0.1.0" % "compile"

###Maven:
Add the framework to your pom.xml:

```xml
<dependency>
  <groupId>net.caladesiframework</groupId>
  <artifactId>caladesi-common_${scala.version}</artifactId>
  <version>0.1.0</version>
</dependency>

<dependency>
  <groupId>net.caladesiframework</groupId>
  <artifactId>caladesi-orientdb_${scala.version}</artifactId>
  <version>0.1.0</version>
</dependency>
```

###Gradle
```groovy
dependencies {
    // Caladesi Framework
    compile "net.caladesiframework:caladesi-common_2.9.1:0.1.0",
        "net.caladesiframework:caladesi-orientdb_2.9.1:0.1.0"
}
```

##Drafts (features for next versions)

```scala
class ShipEntity extends OrientGraphEntity with UuidPk {

  // Defines 1:N relations: Ship -[HAS]-> Container*
  object containerList extends MultiRelation[Container](this, "HAS")

  // Defines 1:1 directed relations: Ship -[SPECIAL]-> Container
  object singleContainer extends SingleRelation[Container](this, "SPECIAL")

  // Defines remote 1:1 directed relations: Ship-[OWNED_BY]->Company
  // Remote means that the ShipEntity has to lookup for a REST service and perform a call
  object remoteCompany extends SingleRelationRemote[CompanyAPI](this, "OWNED_BY")
}
```

```scala
// Usage
ship.containerList.add(containerRepository.create)

val specialContainer = containerRepository.create
specialContainer.color.set("red")

ship.singleContainer.reAssign(specialContainer)

// Perform REST call to create a new Company entity
ship.remoteCompany.reAssign(CompanyAPI.create)

// Ship and new container are updated
shipRepository.update(ship)
```

##License

The Caladesi Framework is open source software released under the Apache 2.0 license.
