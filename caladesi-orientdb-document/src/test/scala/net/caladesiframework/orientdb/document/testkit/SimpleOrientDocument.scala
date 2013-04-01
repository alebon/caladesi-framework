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

class SimpleOrientDocument extends OrientRecord[SimpleOrientDocument] {

  def meta = SimpleOrientDocument

}

object SimpleOrientDocument extends SimpleOrientDocument with OrientMetaRecord[SimpleOrientDocument] {

}
