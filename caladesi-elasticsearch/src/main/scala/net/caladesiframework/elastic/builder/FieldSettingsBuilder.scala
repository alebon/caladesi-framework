/*
* Copyright (c) 2012 Sheeprice Ltd.
* All rights reserved.
*
* http://license.sheeprice.com/LICENSE-1.0
*
* COPYING, REDISTRIBUTION AND USE IN ANY FORM ARE PROHIBITED WITHOUT AN
* EXPLICIT WRITTEN PERMISSION.
*/

package net.caladesiframework.elastic.builder

import net.caladesiframework.field.Field
import net.caladesiframework.elastic.field.analyzer.{NotAnalyzed, Analyzed}

class FieldSettingsBuilder {

  /**
   * Returns mapping for a field
   *
   * @param field Field
   * @return
   */
  def getJsonSettingsFor(field: Field[_,_]): String = {

    if (!field.isInstanceOf[Analyzed] && field.isInstanceOf[NotAnalyzed]) {
      return """
        "{""" + field.name + """}" : {"type" : "string", "index" : "not_analyzed"}
                             """.stripMargin

    }

    // Default analyzer is "analyzed"
    return """
        "{""" + field.name + """}" : {"type" : "string", "index" : "analyzed"}
                             """.stripMargin
  }

}
