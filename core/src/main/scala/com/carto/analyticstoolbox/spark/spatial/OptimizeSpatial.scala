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

package com.carto.analyticstoolbox.spark.spatial

import org.apache.spark.sql.{AnalysisException, SparkSession}

object OptimizeSpatial extends Serializable {

  def apply(
      sourceTable: String,
      outputTable: String,
      outputLocation: String,
      zoom: Int = 8,
      blockSize: Int = 20097000,
      compression: String = "lz4",
      maxRecordsPerFile: Int = 0
  )(implicit ssc: SparkSession): Unit = {
    // configure the output
    ssc.sql(s"SET parquet.block.size = $blockSize;")
    ssc.sql(s"SET spark.sql.parquet.compression.codec=$compression;")
    // ssc.sql(s"SET spark.sql.files.maxRecordsPerFile=$maxRecordsPerFile;")

    // drop tmp views, IF NOT EXISTS is not supported by Spark SQL, that's a DataBricks feature
    // using try catch to capture
    try ssc.sql(s"DROP TABLE ${sourceTable}_idx_view;")
    catch {
      case e: AnalysisException => e.printStackTrace()
    }

    try ssc.sql(s"DROP TABLE $outputTable;")
    catch {
      case e: AnalysisException => e.printStackTrace()
    }

    // view creation
    ssc.sql(
      s"""
         |CREATE TEMPORARY VIEW ${sourceTable}_idx_view AS(
         |  WITH orig_q AS (
         |    SELECT
         |      * EXCEPT(geom),
         |      geom AS wkt,
         |      ST_geomFromWKT(geom) AS geom
         |      FROM $sourceTable
         |    )
         |    SELECT
         |      *,
         |      st_z2LatLon(geom) AS z2,
         |      st_extentFromGeom(geom) AS bbox,
         |      st_partitionCentroid(geom, $zoom) AS partitioning
         |      FROM orig_q
         |      DISTRIBUTE BY partitioning SORT BY z2.min, z2.max
         |  );
         |""".stripMargin
    )

    ssc.sql(
      s"""
         |CREATE TABLE $outputTable
         |USING PARQUET LOCATION '$outputLocation/$outputTable'
         |AS (SELECT * FROM ${sourceTable}_idx_view);
         |""".stripMargin
    )
  }
}
