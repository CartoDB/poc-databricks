# Carto Analytics Toolbox

[![CI](https://github.com/cartodb/analytics-toolbox-databricks/actions/workflows/ci.yml/badge.svg)](https://github.com/cartodb/analytics-toolbox-databricks/actions/workflows/ci.yml)
[![Maven Badge](https://img.shields.io/maven-central/v/com.carto.analytics-toolbox/core_2.12?color=blue)](https://search.maven.org/search?q=g:com.carto.analytics-toolbox%20and%20core)
[![Snapshots Badge](https://img.shields.io/nexus/s/https/oss.sonatype.org/com.carto.analytics-toolbox/core_2.12)](https://oss.sonatype.org/content/repositories/snapshots/com/carto/analytics-toolbox/core_2.12/)

This project is based on [Hiveless](https://github.com/azavea/hiveless) is a Scala library for working with [Spark](https://spark.apache.org/) and [Hive](https://hive.apache.org/) using a more expressive typed API.

It adds typed HiveUDFs and implements Spatial Hive UDFs. It consists of the following modules:

* `core` with Hive GIS UDFs (depends on [GeoMesa](https://github.com/locationtech/geomesa))

## Get started

```scala
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= Seq(
  "com.carto.analytics-toolbox" %% "core" % "<version>"
)
```

## Supported GIS functions (core)

```sql
CREATE OR REPLACE FUNCTION st_area AS 'com.carto.analytics.toolbox.core.ST_Area';
CREATE OR REPLACE FUNCTION st_asBinary AS 'com.carto.analytics.toolbox.core.ST_AsBinary';
CREATE OR REPLACE FUNCTION st_asGeoJson AS 'com.carto.analytics.toolbox.core.ST_AsGeoJson';
CREATE OR REPLACE FUNCTION st_asLatLonText AS 'com.carto.analytics.toolbox.core.ST_AsLatLonText';
CREATE OR REPLACE FUNCTION st_asText AS 'com.carto.analytics.toolbox.core.ST_AsText';
CREATE OR REPLACE FUNCTION st_centroid AS 'com.carto.analytics.toolbox.core.ST_Centroid';
CREATE OR REPLACE FUNCTION st_contains AS 'com.carto.analytics.toolbox.core.ST_Contains';
CREATE OR REPLACE FUNCTION st_covers AS 'com.carto.analytics.toolbox.core.ST_Covers';
CREATE OR REPLACE FUNCTION st_crosses AS 'com.carto.analytics.toolbox.core.ST_Crosses';
CREATE OR REPLACE FUNCTION st_difference AS 'com.carto.analytics.toolbox.core.ST_Difference';
CREATE OR REPLACE FUNCTION st_disjoint AS 'com.carto.analytics.toolbox.core.ST_Disjoint';
CREATE OR REPLACE FUNCTION st_equals AS 'com.carto.analytics.toolbox.core.ST_Equals';
CREATE OR REPLACE FUNCTION st_exteriorRing AS 'com.carto.analytics.toolbox.core.ST_ExteriorRing';
CREATE OR REPLACE FUNCTION st_geoHash AS 'com.carto.analytics.toolbox.core.ST_GeoHash';
CREATE OR REPLACE FUNCTION st_geomFromWKB AS 'com.carto.analytics.toolbox.core.ST_GeomFromWKB';
CREATE OR REPLACE FUNCTION st_geomFromWKT AS 'com.carto.analytics.toolbox.core.ST_GeomFromWKT';
CREATE OR REPLACE FUNCTION st_intersection AS 'com.carto.analytics.toolbox.core.ST_Intersection';
CREATE OR REPLACE FUNCTION st_intersects AS 'com.carto.analytics.toolbox.core.ST_Intersects';
CREATE OR REPLACE FUNCTION st_isGeomField AS 'com.carto.analytics.toolbox.core.ST_IsGeomField';
CREATE OR REPLACE FUNCTION st_makeBBOX AS 'com.carto.analytics.toolbox.core.ST_MakeBBOX';
CREATE OR REPLACE FUNCTION st_makeLine AS 'com.carto.analytics.toolbox.core.ST_MakeLine';
CREATE OR REPLACE FUNCTION st_numGeometries AS 'com.carto.analytics.toolbox.core.ST_NumGeometries';
CREATE OR REPLACE FUNCTION st_numPoints AS 'com.carto.analytics.toolbox.core.ST_NumPoints';
CREATE OR REPLACE FUNCTION st_overlaps AS 'com.carto.analytics.toolbox.core.ST_Overlaps';
CREATE OR REPLACE FUNCTION st_pointFromWKB AS 'com.carto.analytics.toolbox.core.ST_PointFromWKB';
CREATE OR REPLACE FUNCTION st_simplify AS 'com.carto.analytics.toolbox.core.ST_Simplify';
CREATE OR REPLACE FUNCTION st_simplifyPreserveTopology AS 'com.carto.analytics.toolbox.core.ST_SimplifyPreserveTopology';
CREATE OR REPLACE FUNCTION st_touches AS 'com.carto.analytics.toolbox.core.ST_Touches';
CREATE OR REPLACE FUNCTION st_within AS 'com.carto.analytics.toolbox.core.ST_Within';
CREATE OR REPLACE FUNCTION st_x AS 'com.carto.analytics.toolbox.core.ST_X';
CREATE OR REPLACE FUNCTION st_y AS 'com.carto.analytics.toolbox.core.ST_Y';
```

## License
Code is provided under the Apache 2.0 license available at http://opensource.org/licenses/Apache-2.0,
as well as in the LICENSE file. This is the same license used as Spark.
