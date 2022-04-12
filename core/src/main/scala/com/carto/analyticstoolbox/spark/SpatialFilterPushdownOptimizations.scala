package com.carto.analyticstoolbox.spark

import org.apache.spark.sql.SparkSessionExtensions

import com.carto.analyticstoolbox.spark.rules.SpatialFilterPushdownRules


class SpatialFilterPushdownOptimizations extends (SparkSessionExtensions => Unit) {
    def apply(e: SparkSessionExtensions): Unit = {
        e.injectOptimizerRule(spark => {
            SpatialFilterPushdownRules
        })
    }
}