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

package org.apache.spark.sql.hive.hiveless.spatial.rules

import com.carto.analyticstoolbox.core._
import com.carto.analyticstoolbox.index.ST_IntersectsExtent
import com.azavea.hiveless.serializers.syntax._
import org.locationtech.jts.geom.Geometry
import geotrellis.vector._
import cats.syntax.option._
import org.apache.spark.sql.catalyst.expressions._
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.catalyst.plans.logical.{Filter, LogicalPlan}
import org.apache.spark.sql.catalyst.rules.Rule
import org.apache.spark.sql.hive.HiveGenericUDF

object SpatialFilterPushdownRules extends Rule[LogicalPlan] {

  def apply(plan: LogicalPlan): LogicalPlan =
    plan.transformDown {
      // HiveGenericUDF is a private[hive] case class
      case Filter(condition: HiveGenericUDF, plan) if condition.of[ST_IntersectsExtent] =>
        // extract bbox, snd
        val Seq(bboxExpr, geometryExpr) = condition.children
        // extract extent from the right
        val extent = geometryExpr.eval(null).convert[Geometry].extent

        // transform expression
        val expr = List(
          IsNotNull(bboxExpr),
          GreaterThanOrEqual(GetStructField(bboxExpr, 0, "xmin".some), Literal(extent.xmin)),
          GreaterThanOrEqual(GetStructField(bboxExpr, 1, "ymin".some), Literal(extent.ymin)),
          LessThanOrEqual(GetStructField(bboxExpr, 2, "xmax".some), Literal(extent.xmax)),
          LessThanOrEqual(GetStructField(bboxExpr, 3, "ymax".some), Literal(extent.ymax))
        ).and

        Filter(expr, plan)
    }

  def registerOptimizations(sqlContext: SQLContext): Unit =
    Seq(SpatialFilterPushdownRules).foreach { r =>
      if (!sqlContext.experimental.extraOptimizations.contains(r))
        sqlContext.experimental.extraOptimizations ++= Seq(r)
    }
}
