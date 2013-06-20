/*
* Copyright (c) 2012 Sheeprice Ltd.
* All rights reserved.
*
* http://license.sheeprice.com/LICENSE-1.0
*
* COPYING, REDISTRIBUTION AND USE IN ANY FORM ARE PROHIBITED WITHOUT AN
* EXPLICIT WRITTEN PERMISSION.
*/

package net.caladesiframework.elastic.testkit

import java.io.{IOException, File}
import org.specs2.mutable.Around
import org.specs2.execute.{AsResult, Result}
import java.util.UUID
import net.caladesiframework.elastic.provider.{Elastic, ElasticProvider}
import net.caladesiframework.elastic.DefaultElasticProviderIdentifier

trait ElasticTestKit {

  lazy val storeDirPath = "/%s/elastic-test-db".format(System.getProperty("java.io.tmpdir"))

  def destroyProviderAndDatabase(elasticProvider: ElasticProvider) = {
    elasticProvider.shutdown

    val storeDir: File = new File(elasticProvider.path)

    //make sure directory exists
    if(!storeDir.exists()){

      println("Elastic directory does not exist.")

    }else{

      try{

        delete(storeDir);

      } catch {
        case e:IOException => e.printStackTrace();
      }
    }
  }

  object ElasticEmbeddedTestContext extends Around {

    def around[T](t: => T)(implicit evidence$1: AsResult[T]) = {
      val storeDirPathRandom = storeDirPath + UUID.randomUUID().toString
      val provider = ElasticProvider("elasticTestNode", storeDirPathRandom, false )
      provider.startUp()

      Elastic.defineProvider(DefaultElasticProviderIdentifier, provider)

      try {
        AsResult(t)
      } finally {
        destroyProviderAndDatabase(Elastic.getProvider(DefaultElasticProviderIdentifier).get)
      }
    }
  }

  private def delete(file: File) {

    if(file.isDirectory()){

      //directory is empty, then delete it
      if(file.list().length==0){
        file.delete()
      } else {

        //list all the directory contents
        val files =  file.list()

        for (temp <- files) {
          //construct the file structure
          val fileDelete: File = new File(file, temp)

          //recursive delete
          delete(fileDelete)
        }

        //check the directory again, if empty then delete it
        if(file.list().length==0){
          file.delete()
        }
      }

    }else{
      //if file, then delete it
      file.delete()
    }
  }

}
