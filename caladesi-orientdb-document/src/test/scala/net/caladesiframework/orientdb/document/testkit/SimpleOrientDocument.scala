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
import net.caladesiframework.document.Field

class SimpleOrientDocument extends OrientRecord[SimpleOrientDocument] {

  def meta = SimpleOrientDocument

  object name extends SimpleField(this)

  val testValue: String = "String value"

}

object SimpleOrientDocument extends SimpleOrientDocument with OrientMetaRecord[SimpleOrientDocument] {

}

class SimpleField[OwnerType](ownerConstruct: OwnerType) extends Field[String, OwnerType]{

  def name = null

  def owner = ownerConstruct
}
