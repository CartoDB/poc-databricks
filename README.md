# Carto Analytics Toolbox

[![CI](https://github.com/cartodb/analytics-toolbox-databricks/actions/workflows/ci.yml/badge.svg)](https://github.com/cartodb/analytics-toolbox-databricks/actions/workflows/ci.yml)
[![Maven Badge](https://img.shields.io/maven-central/v/com.carto.analyticstoolbox/core_2.12?color=blue)](https://search.maven.org/search?q=g:com.carto.analyticstoolbox%20and%20core)
[![Snapshots Badge](https://img.shields.io/nexus/s/https/s01.oss.sonatype.org/com.carto.analyticstoolbox/core_2.12)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/carto/analyticstoolbox/core_2.12/)

CARTO Analytics Toolbox for Databricks provides geospatial functionality leveraging the Geomesa SparkSQL capabilities. It implements Spatial Hive UDFs and consists of the following modules:

* `core` with Hive GIS UDFs (depends on [GeoMesa](https://github.com/locationtech/geomesa) and [Hiveless](https://github.com/azavea/hiveless))

## Quick Start

```scala
resolvers ++= Seq(
  // for snapshot artifacts only
  "s01-oss-sonatype" at "https://s01.oss.sonatype.org/content/repositories/snapshots"
)

libraryDependencies ++= Seq(
  "com.carto.analyticstoolbox" %% "core" % "<latest version>"
)
```

## Supported GIS functions (core)

```sql
CREATE OR REPLACE FUNCTION st_geometryFromText as 'com.carto.analyticstoolbox.core.ST_GeomFromWKT';
CREATE OR REPLACE FUNCTION st_intersects as 'com.carto.analyticstoolbox.core.ST_Intersects';
CREATE OR REPLACE FUNCTION st_simplify as 'com.carto.analyticstoolbox.core.ST_Simplify';
 -- ...and more
```

The full list of supported functions can be found [here](./core/sql/createUDFs.sql).

## Table Optimization

There are two functions defined to help with the raw table preparations. Both transform the input table 
into the intersections query optimized shape; for more details see [OptimizeSpatial.scala](./core/src/main/scala/com/carto/analyticstoolbox/spark/spatial/OptimizeSpatial.scala):

1. **optimizeSpatialAuto**
   * Uses heuristics to compute the optimal parquet block size
2. **optimizeSpatial**
   * Uses the user input to set the output parquet file block size

```scala
import com.carto.analyticstoolbox.spark.spatial._

val sourceTable: String = ???
val outputTable: String = ???
val outputLocation: String = ???

// optimize with the block size computation 
spark.optimizeSpatialAuto(sourceTable, outputTable, outputLocation)
// optimize with the user defined block size
spark.optimizeSpatial(sourceTable, outputTable, outputLocation, blockSize = 20097000)
```
