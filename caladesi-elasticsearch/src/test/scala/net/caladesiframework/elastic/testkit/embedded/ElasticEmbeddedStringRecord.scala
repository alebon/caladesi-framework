/*
* Copyright (c) 2012 Sheeprice Ltd.
* All rights reserved.
*
* http://license.sheeprice.com/LICENSE-1.0
*
* COPYING, REDISTRIBUTION AND USE IN ANY FORM ARE PROHIBITED WITHOUT AN
* EXPLICIT WRITTEN PERMISSION.
*/

package net.caladesiframework.elastic.testkit.embedded

import net.caladesiframework.elastic.record.{ElasticMetaRecord, ElasticRecord}
import net.caladesiframework.elastic.field.StringField

class ElasticEmbeddedStringRecord extends ElasticRecord[ElasticEmbeddedStringRecord] {

  def meta = ElasticEmbeddedStringRecord

  object stringField extends StringField(this)

}

object ElasticEmbeddedStringRecord extends ElasticEmbeddedStringRecord with ElasticMetaRecord[ElasticEmbeddedStringRecord]
