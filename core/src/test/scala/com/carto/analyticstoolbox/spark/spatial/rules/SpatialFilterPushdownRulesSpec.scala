package com.carto.analyticstoolbox.spark.spatial.rules

import com.carto.analyticstoolbox.core.ST_GeomFromGeoJson
import geotrellis.spark.testkit.TestEnvironment
import org.apache.spark.SparkConf
import org.apache.spark.serializer.KryoSerializer
import org.apache.spark.sql.SparkSession
import org.scalatest.funspec.AnyFunSpec

import java.io.File
import scala.util.Properties

class SpatialFilterPushdownRulesSpec extends AnyFunSpec with TestEnvironment {

  val uri = new File("core/src/test/resources/polygons").toURI.toString

  // SQL
  ssc.sql("CREATE TEMPORARY FUNCTION ST_GeomFromGeoJSON as 'com.carto.analyticstoolbox.core.ST_GeomFromGeoJson';") // .count()
  ssc.sql("CREATE TEMPORARY FUNCTION ST_GeomFromWKT as 'com.carto.analyticstoolbox.core.ST_GeomFromWKT';")         // .count()
  ssc.sql("CREATE TEMPORARY FUNCTION ST_Intersects as 'com.carto.analyticstoolbox.core.ST_Intersects';")           // .count()

  // create a tmp view
  ssc.read
    .option("delimiter", ",")
    .option("header", "true")
    .csv(uri)
    .createOrReplaceTempView("polygons")

  describe("SpatialFilterPushdownRules") {
    it("test optimizations") {

      val res = ssc.sql(
        """
          |SELECT * FROM polygons WHERE ST_Intersects(ST_GeomFromWKT(geom), ST_GeomFromGeoJSON('{"type":"Polygon","coordinates":[[[-75.5859375,40.32517767999294],[-75.5859375,43.197167282501276],[-72.41015625,43.197167282501276],[-72.41015625,40.32517767999294],[-75.5859375,40.32517767999294]]]}'))
          |""".stripMargin
      )

      println(res.collect().toList.length)
    }
  }

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

    sparkContext
  }
}
