/*
 * Copyright 2022 Azavea
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.carto.analyticstoolbox

import geotrellis.spark.testkit.TestEnvironment
import org.apache.spark.SparkConf
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.sql.hive.hiveless.spatial.rules.SpatialFilterPushdownRules
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.scalatest.{BeforeAndAfterAll, Suite}

import java.io.File
import scala.io.Source
import scala.language.reflectiveCalls
import scala.util.Properties

trait TestEnvironmentHive extends TestEnvironment { self: Suite with BeforeAndAfterAll =>
  import TestEnvironmentHive._

  // function to override Hive SQL functions registration
  def registerHiveUDFs(ssc: SparkSession): Unit =
    Source
      .fromFile(new File("sql/createUDFs.sql").toURI)
      .using(_.mkString.split(";").toList.map(_.trim).filter(_.nonEmpty))
      .foreach(ssc.sql)

  // function to override optimizations
  def registerOptimizations(sqlContext: SQLContext): Unit =
    SpatialFilterPushdownRules.registerOptimizations(sqlContext)

  // override the SparkSession construction to enable Hive support
  override lazy val _ssc: SparkSession = {
    System.setProperty("spark.driver.port", "0")
    System.setProperty("spark.hostPort", "0")
    System.setProperty("spark.ui.enabled", "false")

    val conf = new SparkConf()
    conf
      .setMaster(sparkMaster)
      .setAppName("Test Hive Context")
      .set("spark.default.parallelism", "4")
      // Since Spark 3.2.0 this flag is set to true by default
      // We need it to be set to false, since it is required by the HBase TableInputFormat
      .set("spark.hadoopRDD.ignoreEmptySplits", "false")
      .set("spark.sql.warehouse.dir", "/tmp/cartoanalyticstoolbox/metastore_db")

    // Shortcut out of using Kryo serialization if we want to test against
    // java serialization.
    if (Properties.envOrNone("GEOTRELLIS_USE_JAVA_SER").isEmpty) {
      conf
        .set("spark.serializer", classOf[KryoSerializer].getName)
        .set("spark.kryoserializer.buffer.max", "500m")
        .set("spark.kryo.registrationRequired", "false")
      setKryoRegistrator(conf)
    }

    val sparkContext = SparkSession.builder().config(conf).enableHiveSupport().getOrCreate()

    System.clearProperty("spark.driver.port")
    System.clearProperty("spark.hostPort")
    System.clearProperty("spark.ui.enabled")

    registerOptimizations(sparkContext.sqlContext)
    registerHiveUDFs(sparkContext)

    sparkContext
  }
}

object TestEnvironmentHive {
  implicit class AutoCloseableOps[A <: AutoCloseable](val resource: A) extends AnyVal {
    def using[B](f: A => B): B = try f(resource)
    finally resource.close()
  }
}
