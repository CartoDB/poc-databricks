# Carto Analytics Toolbox

[![CI](https://github.com/cartodb/analytics-toolbox-databricks/actions/workflows/ci.yml/badge.svg)](https://github.com/cartodb/analytics-toolbox-databricks/actions/workflows/ci.yml)
[![Maven Badge](https://img.shields.io/maven-central/v/com.carto.analytics-toolbox/core_2.12?color=blue)](https://search.maven.org/search?q=g:com.carto.analytics-toolbox%20and%20core)
[![Snapshots Badge](https://img.shields.io/nexus/s/https/oss.sonatype.org/com.carto.analytics-toolbox/core_2.12)](https://oss.sonatype.org/content/repositories/snapshots/com/carto/analytics-toolbox/core_2.12/)

CARTO Analytics Toolbox for Databricks provides geospatial functionality leveraging the Geomesa SparkSQL capabilities. It implements Spatial Hive UDFs and consists of the following modules:

* `core` with Hive GIS UDFs (depends on [GeoMesa](https://github.com/locationtech/geomesa) and [Hiveless](https://github.com/azavea/hiveless))

## Get started

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots") // for snapshot artifacts
)

libraryDependencies ++= Seq(
  "com.carto.analytics-toolbox" %% "core" % "<version>"
)
```

## Supported GIS functions (core)

```sql
CREATE OR REPLACE FUNCTION st_geometryFromText as 'com.carto.analytics.toolbox.core.ST_GeomFromWKT';
CREATE OR REPLACE FUNCTION st_intersects as 'com.carto.analytics.toolbox.core.ST_Intersects';
CREATE OR REPLACE FUNCTION st_simplify as 'com.carto.analytics.toolbox.core.ST_Simplify';
 -- ...and more
```

The full list of supported functions can be found [here](./core/sql/createUDFs.sql).

## License
Code is provided under the Apache 2.0 license available at http://opensource.org/licenses/Apache-2.0,
as well as in the LICENSE file. This is the same license used as Spark.
