/*
* Copyright (c) 2012 Sheeprice Ltd.
* All rights reserved.
*
* http://license.sheeprice.com/LICENSE-1.0
*
* COPYING, REDISTRIBUTION AND USE IN ANY FORM ARE PROHIBITED WITHOUT AN
* EXPLICIT WRITTEN PERMISSION.
*/

package net.caladesiframework.orientdb.document.raw

import org.specs2.mutable.Specification
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx
import com.orientechnologies.orient.core.record.impl.ODocument

class OrientDbSpec extends Specification {

  "OrientDb in Embedded Mode" should {
    "work as expected" in {
      // OPEN THE DATABASE
      val db: ODatabaseDocumentTx = new ODatabaseDocumentTx("local:/%s/orient-raw-data".format(System.getProperty("java.io.tmpdir")));
      if (!db.exists()) {
        db.create()
      }
      if (db.isClosed) {
        db.open("admin", "admin")
      }

      // CREATE A NEW DOCUMENT AND FILL IT
      val doc = new ODocument("Person")
      doc.field( "name", "Luke" )
      doc.field( "surname", "Skywalker" )
      doc.field( "city", new ODocument("City").field("name","Rome").field("country", "Italy") )

      // SAVE THE DOCUMENT
      doc.save();

      db.close();

      "Fake" must_==("Fake")
    }
  }

  "OrientDb in Memory Mode" should {
    "work as expected" in {
      // OPEN THE DATABASE
      val db: ODatabaseDocumentTx = new ODatabaseDocumentTx("memory:test");
      if (!db.exists()) {
        db.create()
      }
      if (db.isClosed) {
        db.open("admin", "admin")
      }

      // CREATE A NEW DOCUMENT AND FILL IT
      val doc = new ODocument("Person")
      doc.field( "name", "Luke" )
      doc.field( "surname", "Skywalker" )
      doc.field( "city", new ODocument("City").field("name","Rome").field("country", "Italy") )

      // SAVE THE DOCUMENT
      doc.save()

      db.close()

      "Fake" must_==("Fake")
    }
  }

}
