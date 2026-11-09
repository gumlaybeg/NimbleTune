package com.gumlaybeg.nimbletune.models

import com.gumlaybeg.nimbletune.db.entities.FormatEntity
import com.gumlaybeg.nimbletune.db.entities.Song

/**
 * For passing along song metadata
 */
data class SongTempData(val song: Song, val format: FormatEntity?)