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

package com.carto.analyticstoolbox.core

import com.carto.analyticstoolbox.index._

import com.azavea.hiveless.HUDF
import com.azavea.hiveless.implicits.tupler._
import com.azavea.hiveless.serializers.HDeserializer.Errors.ProductDeserializationError
import geotrellis.vector._
import shapeless._

class ST_Contains extends HUDF[(ST_Contains.Arg, ST_Contains.Arg), Boolean] {
  def function = ST_Contains.function
}

object ST_Contains {
  // We could use Either[Extent, Geometry], but Either has no safe fall back CNil
  // which may lead to derivation error messages rather than parsing
  type Arg = Extent :+: Geometry :+: CNil

  def parseGeometry(a: Arg): Option[Geometry] = a.select[Geometry].orElse(a.select[Extent].map(_.toPolygon()))

  private def parseGeometryUnsafe(a: Arg, aname: String): Geometry =
    parseGeometry(a).getOrElse(throw ProductDeserializationError[ST_Contains, Arg](aname))

  def function(left: Arg, right: Arg): Boolean = {
    val (l, r) = (parseGeometryUnsafe(left, "first"), parseGeometryUnsafe(right, "second"))

    l.contains(r)
  }
}
