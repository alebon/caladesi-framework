/*
* Copyright (c) 2012 Sheeprice Ltd.
* All rights reserved.
*
* http://license.sheeprice.com/LICENSE-1.0
*
* COPYING, REDISTRIBUTION AND USE IN ANY FORM ARE PROHIBITED WITHOUT AN
* EXPLICIT WRITTEN PERMISSION.
*/

package net.caladesiframework.orientdb.config

import collection.mutable.HashMap

object OrientConfigurationRegistry {

  // Private map ClassName -> Repository
  private val map = new HashMap[String, OrientConfiguration]()

  /**
   * Register a config
   *
   * @return
   */
  def register(configuration: OrientConfiguration, name: String = "default") = {
    map.put(name, configuration)
  }

  /**
   * Loads a config by db name
   *
   * @param name
   * @return
   */
  def loadByName(name: String): OrientConfiguration = {
    map.get(name) match {
      case Some(configuration) => configuration
      case None => throw new RuntimeException("No configuration found for key %s".format(name))
    }
  }

}
