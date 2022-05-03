package com.carto.analyticstoolbox.index.h3

import com.uber.h3core.H3CoreV3

object H3CoreV3Producer extends Serializable {
  @transient lazy val get: H3CoreV3 = H3CoreV3.newInstance()
}
