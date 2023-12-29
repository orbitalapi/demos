package com.icycube.posts

import com.icycube.TypeNames.com.icycube.posts.ViewCount
import kotlin.Int
import lang.taxi.annotations.DataType

@DataType(
  value = ViewCount,
  imported = true
)
typealias ViewCount = Int
