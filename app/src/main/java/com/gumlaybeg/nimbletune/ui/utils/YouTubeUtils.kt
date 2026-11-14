package com.gumlaybeg.nimbletune.ui.utils

import kotlin.math.roundToInt

private val GOOGLE_USER_CONTENT_HOST_REGEX =
    "^https://(lh|yt)\\d+\\.(googleusercontent|ggpht)\\.com/.*".toRegex()
private val WH_PARAM_REGEX = "=w(\\d+)-h(\\d+).*".toRegex()
private val S_PARAM_REGEX = "=s(\\d+)".toRegex()
private val YTIMG_LOW_RES_REGEX =
    "^(https://i\\d*\\.ytimg\\.com/vi/[^/]+/)(?:default|mqdefault)\\.jpg(\\?.*)?$".toRegex()

fun String.resize(
    width: Int? = null,
    height: Int? = null,
): String {
    if (width == null && height == null) return this

    if (this matches GOOGLE_USER_CONTENT_HOST_REGEX) {
        WH_PARAM_REGEX.find(this)?.let { match ->
            val origW = match.groupValues[1].toIntOrNull() ?: 0
            val origH = match.groupValues[2].toIntOrNull() ?: 0
            val w = width ?: if (height != null && origH > 0) {
                (height.toDouble() / origH * origW).roundToInt().coerceAtLeast(1)
            } else origW
            val h = height ?: if (width != null && origW > 0) {
                (width.toDouble() / origW * origH).roundToInt().coerceAtLeast(1)
            } else origH

            if (w > 0 && h > 0) {
                val base = substring(0, match.range.first)
                val cropFlag = if (width != null && height != null) "-p" else ""
                return "$base=w$w-h$h$cropFlag-l90-rj"
            }
        }

        if (S_PARAM_REGEX.containsMatchIn(this)) {
            val targetSize = maxOf(width ?: 0, height ?: 0)
            if (targetSize > 0) {
                return S_PARAM_REGEX.replaceFirst(this, "=s$targetSize")
            }
        }
    }

    val targetSize = maxOf(width ?: 0, height ?: 0)
    if (targetSize >= 480) {
        YTIMG_LOW_RES_REGEX.matchEntire(this)?.let { match ->
            return "${match.groupValues[1]}hqdefault.jpg"
        }
    }

    return this
}
