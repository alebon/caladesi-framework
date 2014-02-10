package net.caladesiframework.neo4j.index

import net.caladesiframework.document.{OptionalField, RequiredField, Field}
import net.caladesiframework.neo4j.record.Neo4jEntity
import net.caladesiframework.neo4j.db.Neo4jDatabaseService
import java.util.{HashMap => jMap}

trait Index {

  /**
   * Check for index and create one if its missing
   *
   * @param field
   * @param ds
   * @return
   */
  def checkFieldIndex(field: Field[_,_] with IndexedField)(implicit ds: Neo4jDatabaseService) = {
    val indexName = Naming().indexName(field, field.owner.asInstanceOf[Neo4jEntity[_]])

    if (!ds.graphDatabase.index().existsForNodes(indexName)) {

      val idx = ds.graphDatabase.index()

      // Create the index
      field match {
        case field: Field[_,_] with UniqueIndexed =>
          idx.forNodes(indexName)
        case field: Field[_,_] with FulltextIndexed =>
          val idxParams = new jMap[String, String]()
          idxParams.put("type", "fulltext")

          idx.forNodes(indexName, idxParams)
      }

    }
  }

  /**
   * Update index entries for the entity
   *
   * @param entity
   * @param ds
   * @return
   */
  def updateIndex(entity: Neo4jEntity[_], reIndex: Boolean = true)(implicit ds: Neo4jDatabaseService)  = {
    val idx = ds.graphDatabase.index()

    entity.meta.fields.asInstanceOf[List[Field[_,_]]] foreach {

      // Create the index
      field =>

        val indexName = Naming().indexName(field, entity)

        field match {
          case field: RequiredField[_,_] with IndexedField =>
            val nodeIdx = idx.forNodes(indexName)
            if (null != nodeIdx) {
              nodeIdx.remove(entity.getUnderlyingNode.get, field.name)
              nodeIdx.add(entity.getUnderlyingNode.get, field.name, field.get)
            }
          case field: OptionalField[_,_] with IndexedField =>
            val nodeIdx = idx.forNodes(indexName)
            if (null != nodeIdx) {
              nodeIdx.remove(entity.getUnderlyingNode.get, field.name)

              field.hasValue match {
                case true => nodeIdx.add(entity.getUnderlyingNode.get, field.name, field.get)
                case _ =>
              }
            }
          case _ =>
            // Ignore field
        }
    }
  }

  /**
   * Removes all entries for the entity
   *
   * @param entity
   * @param ds
   * @return
   */
  def removeFromIndex(entity: Neo4jEntity[_])(implicit ds: Neo4jDatabaseService) = {
    entity.meta.fields foreach {
      field => {
        field match {
          case field: Field[_, Neo4jEntity[_]] with IndexedField =>
            val indexName = Naming().indexName(field, entity)
            val idxForNode = ds.graphDatabase.index().forNodes(indexName)
            idxForNode.remove(entity.getUnderlyingNode.get)
          case _ =>
            // ignore it
        }
      }
    }

  }

}

sealed case class Naming() {

  def indexName(field: Field[_,_], entity: Neo4jEntity[_]): String = {
    field match {
      case field: Field[_,_] with FulltextIndexed =>
        "idx:fulltext_%s_%s".format(entity.meta.label, field.name)
      case field: Field[_,_] with UniqueIndexed =>
        "idx:exact_%s_%s".format(entity.meta.label, field.name)
    }

  }

}
