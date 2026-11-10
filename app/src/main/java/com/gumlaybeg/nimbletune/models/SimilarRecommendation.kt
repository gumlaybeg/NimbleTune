package com.gumlaybeg.nimbletune.models

import com.gumlaybeg.nimbletune.db.entities.LocalItem
import com.music.innertube.models.YTItem

data class SimilarRecommendation(
    val title: LocalItem,
    val items: List<YTItem>,
)
