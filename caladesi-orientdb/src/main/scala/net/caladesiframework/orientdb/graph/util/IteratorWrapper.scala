/*
* Copyright (c) 2012 Sheeprice Ltd.
* All rights reserved.
*
* http://license.sheeprice.com/LICENSE-1.0
*
* COPYING, REDISTRIBUTION AND USE IN ANY FORM ARE PROHIBITED WITHOUT AN
* EXPLICIT WRITTEN PERMISSION.
*/

package net.caladesiframework.orientdb.graph.util

trait IteratorWrapper {

  /**implicit def toIterator[T](ci: ClosableIterable[T]) = new ScalaCloseableIterable[T](ci)

  class ScalaCloseableIterable[T](ci: ClosableIterable[T]) {

    def foreach[U](f: (T) => U) {
      val i = ci.iterator()
      try {
        while (i.hasNext) {
          val t = i.next()
          f(t)
        }
      } finally {
        ci.close()
      }
    }
  }*/

}
