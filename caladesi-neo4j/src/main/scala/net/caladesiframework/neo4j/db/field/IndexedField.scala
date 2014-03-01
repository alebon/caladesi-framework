package net.caladesiframework.neo4j.db.field

trait IndexedField {

}

trait FulltextIndex extends IndexedField {

}

trait UniqueIndex extends IndexedField {

}

trait ExactIndex extends IndexedField {

}
