package com.carto.analyticstoolbox.spark.spatial.rules

import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.catalyst.plans.logical.{Filter, LogicalPlan}
import org.apache.spark.sql.catalyst.rules.Rule

object SpatialFilterPushdownRules extends Rule[LogicalPlan] {
  def apply(plan: LogicalPlan): LogicalPlan =
    plan.transformUp { case f @ Filter(condition, plan) =>
      println(condition)

      f
    }

  def registerOptimizations(sqlContext: SQLContext): Unit =
    Seq(SpatialFilterPushdownRules).foreach { r =>
      if (!sqlContext.experimental.extraOptimizations.contains(r))
        sqlContext.experimental.extraOptimizations ++= Seq(r)
    }
}
