/*
* Copyright (c) 2012 Sheeprice Ltd.
* All rights reserved.
*
* http://license.sheeprice.com/LICENSE-1.0
*
* COPYING, REDISTRIBUTION AND USE IN ANY FORM ARE PROHIBITED WITHOUT AN
* EXPLICIT WRITTEN PERMISSION.
*/

package net.caladesiframework.orientdb.document.testkit

import net.caladesiframework.orientdb.document.record.{OrientMetaRecord, OrientRecord}
import net.caladesiframework.orientdb.document.field.{OptionalUuidField, UuidPk}

class OrientMemoryUuidRecord extends OrientRecord[OrientMemoryUuidRecord] with UuidPk {
  def meta = OrientMemoryUuidRecord

  object optionalUuidField extends OptionalUuidField(this)
}

object OrientMemoryUuidRecord extends OrientMemoryUuidRecord with OrientMetaRecord[OrientMemoryUuidRecord] {
  override def databaseName = "memoryDB"
}
