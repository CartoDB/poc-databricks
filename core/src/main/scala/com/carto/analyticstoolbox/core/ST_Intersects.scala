/*
 * Copyright 2021 Azavea
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

import com.carto.analyticstoolbox.core._
import com.carto.analyticstoolbox.index._

import com.azavea.hiveless.HUDF
import com.azavea.hiveless.implicits.tupler._
import com.azavea.hiveless.serializers.UnaryDeserializer
import geotrellis.vector._
import shapeless._

class ST_Intersects extends HUDF[(ST_Intersects.Arg, ST_Intersects.Arg), Boolean] {
  val name: String = "st_intersects"
  def function     = ST_Intersects.function
}

object ST_Intersects {
  import UnaryDeserializer.Errors.ProductDeserializationError

  type Arg = Extent :+: Geometry :+: CNil

  def parseExtent(a: Arg): Option[Extent] = a.select[Extent].orElse(a.select[Geometry].map(_.extent))

  private def parseExtentUnsafe(a: Arg, aname: String): Extent =
    parseExtent(a).getOrElse(throw ProductDeserializationError[Arg](classOf[ST_Intersects], aname))

  def function(left: Arg, right: Arg): Boolean = {
    val (l, r) = (parseExtentUnsafe(left, "first"), parseExtentUnsafe(right, "second"))

    l.intersects(r)
  }
}
