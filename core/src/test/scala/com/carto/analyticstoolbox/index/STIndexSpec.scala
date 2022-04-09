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

package com.carto.analyticstoolbox.index

import com.carto.analyticstoolbox.{TestEnvironmentHive, TestTables}
import org.apache.spark.sql.catalyst.plans.logical.Filter
import org.scalatest.funspec.AnyFunSpec

class STIndexSpec extends AnyFunSpec with TestEnvironmentHive with TestTables {

  describe("ST Index functions spec") {
    it("ST_IntersectsExtent should filter a Parquet file") {
      val df = ssc.sql(
        """
          |SELECT * FROM polygons_parquet WHERE ST_IntersectsExtent(bbox, ST_GeomFromGeoJSON('{"type":"Polygon","coordinates":[[[-75.5859375,40.32517767999294],[-75.5859375,43.197167282501276],[-72.41015625,43.197167282501276],[-72.41015625,40.32517767999294],[-75.5859375,40.32517767999294]]]}'))
          |""".stripMargin
      )

      df.count() shouldBe 5
    }

    it("ST_IntersectsExtent plan should be optimized") {
      val df = ssc.sql(
        """
          |SELECT * FROM polygons_parquet WHERE ST_IntersectsExtent(bbox, ST_GeomFromGeoJSON('{"type":"Polygon","coordinates":[[[-75.5859375,40.32517767999294],[-75.5859375,43.197167282501276],[-72.41015625,43.197167282501276],[-72.41015625,40.32517767999294],[-75.5859375,40.32517767999294]]]}'))
          |""".stripMargin
      )

      val dfe = ssc.sql(
        """
          |SELECT * FROM polygons_parquet
          |WHERE bbox.xmin >= -75.5859375
          |AND bbox.ymin >= 40.3251777
          |AND bbox.xmax <= -72.4101562
          |AND bbox.ymax <= 43.1971673
          |""".stripMargin
      )

      df.count() shouldBe dfe.count()

      // compare optimized plans filters
      val dfc  = df.queryExecution.optimizedPlan.collect { case Filter(condition, _) => condition }
      val dfec = dfe.queryExecution.optimizedPlan.collect { case Filter(condition, _) => condition }

      dfc shouldBe dfec
    }
  }
}