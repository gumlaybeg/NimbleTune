package com.gumlaybeg.nimbletune.models

import com.zionhuang.innertube.models.YTItem

data class ItemsPage(
    val items: List<YTItem>,
    val continuation: String?,
)
