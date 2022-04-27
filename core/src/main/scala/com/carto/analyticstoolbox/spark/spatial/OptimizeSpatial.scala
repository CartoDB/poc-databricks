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

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.TableIdentifier
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.types.BinaryType


object OptimizeSpatial extends Serializable {

  def apply(
    sourceTable: String,
    outputTable: String,
    outputLocation: String,
    computeBlockSize: String => Long,
    compression: String,
    maxRecordsPerFile: Int,
    geomColumn: String
  )(implicit ssc: SparkSession): Unit = {
    // drop tmp views, IF NOT EXISTS is not supported by Spark SQL, that's a DataBricks feature
    // using try catch to capture
    val catalog = ssc.sessionState.catalog

    catalog.dropTable(TableIdentifier(s"${sourceTable}_idx_view"), ignoreIfNotExists = true, purge = false)
    catalog.dropTable(TableIdentifier(outputTable), ignoreIfNotExists = true, purge = false)

    // via SQL
    /*try ssc.sql(s"DROP TABLE ${sourceTable}_idx_view;") catch { case _: AnalysisException => // e.printStackTrace() }
    // overwrite the output table
    try ssc.sql(s"DROP TABLE $outputTable;") catch { case _: AnalysisException => // e.printStackTrace() }*/

    // Decide, based on column type of geometry, which parsing function to use
    val df = ssc.table(sourceTable)
    val parseGeom =
      if (df.schema(geomColumn).dataType == BinaryType) "ST_geomFromTWKB"
      else "ST_geomFromWKT"

    // view creation
    // SQL definition is easier and more readable
    ssc.sql(
      s"""
         |CREATE TEMPORARY VIEW ${sourceTable}_idx_view AS(
         |  WITH orig_q AS (
         |    SELECT
         |      * EXCEPT($geomColumn),
         |      $parseGeom($geomColumn) AS geom
         |      FROM $sourceTable
         |    )
         |    SELECT
         |      *,
         |      st_z2LatLon(geom) AS __carto_z2,
         |      st_extentFromGeom(geom) AS __carto_index,
         |      st_partitionCentroid(geom, $zoom) AS __carto_partitioning
         |      FROM orig_q
         |      DISTRIBUTE BY __carto_partitioning SORT BY __carto_z2.min, __carto_z2.max
         |  );
         |""".stripMargin
    )

    // configure the output
    val blockSize = computeBlockSize(s"${sourceTable}_idx_view")
    val conf      = ssc.conf
    conf.set("parquet.block.size", blockSize)
    conf.set("spark.sql.parquet.compression.codec", compression)
    conf.set("spark.sql.files.maxRecordsPerFile", maxRecordsPerFile)

    // via SQL
    /*ssc.sql(s"SET parquet.block.size = $blockSize;")
    ssc.sql(s"SET spark.sql.parquet.compression.codec=$compression;")
    ssc.sql(s"SET spark.sql.files.maxRecordsPerFile=$maxRecordsPerFile;")*/

    ssc.sql(
      s"""
         |CREATE TABLE $outputTable
         |USING PARQUET LOCATION '$outputLocation/$outputTable'
         |AS (SELECT * EXCEPT (__carto_partitioning,  __carto_z2) FROM ${sourceTable}_idx_view);
         |""".stripMargin
    )
  }

  /** automatically computes the block size */
  def apply(
    sourceTable: String,
    outputTable: String,
    outputLocation: String,
    blockSizeDefault: Int,
    compression: String,
    maxRecordsPerFile: Int,
    geomColumn: String
  )(implicit ssc: SparkSession): Unit =
    apply(
      sourceTable,
      outputTable,
      outputLocation,
      computeBlockSize = t => blockSizeCompute(t, blockSizeDefault),
      compression,
      maxRecordsPerFile,
      geomColumn
    )

  /** TODO: improve heuristic */
  def blockSizeCompute(table: String, blockSizeDefault: Int)(implicit ssc: SparkSession): Long = {
    val df  = ssc.sql(s"SELECT partitioning FROM $table LIMIT 10;")
    val p   = df.take(1).map(_.getLong(0)).headOption.getOrElse(0)
    val dfc = ssc.sql(s"SELECT COUNT(*) FROM $table WHERE partitioning = $p;")

    math.max(dfc.head.getLong(0) * 10 / 2, blockSizeDefault)
  }
}
