package com.gumlaybeg.nimbletune.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import com.gumlaybeg.nimbletune.models.ImageCacheManager
import javax.inject.Inject

class LmImageCacheMgr @Inject constructor(

) {

    private var localImageCache = ImageCacheManager(300)


    /**
     * Extract the album art from the audio file. The image is not resized
     * (did you mean to use getLocalThumbnail(path: String?, resize: Boolean)?).
     *
     * @param path Full path of audio file
     */
    fun getLocalThumbnail(path: String?): Bitmap? = getLocalThumbnail(path, false)

    /**
     * Extract the album art from the audio file
     *
     * @param path Full path of audio file
     * @param resize Whether to resize the Bitmap to a thumbnail size (max 512px)
     */
    fun getLocalThumbnail(path: String?, resize: Boolean): Bitmap? {
        if (path == null) {
            return null
        }
        // try cache lookup
        val cachedImage = if (resize) {
            localImageCache.retrieveImage(path)?.resizedImage
        } else {
            localImageCache.retrieveImage(path)?.image
        }

        if (cachedImage != null) {
            return cachedImage
        }

        val mData = MediaMetadataRetriever()

        val decoded: Bitmap? = try {
            mData.setDataSource(path)
            val art = mData.embeddedPicture
            if (art != null) BitmapFactory.decodeByteArray(art, 0, art.size) else null
        } catch (e: Exception) {
            localImageCache.cache(path, null, resize)
            null
        } finally {
            runCatching { mData.release() }
        }

        var image = decoded ?: return null

        if (resize) {
            val maxDim = maxOf(image.width, image.height)
            if (maxDim > 512) {
                val scale = 512f / maxDim
                val targetW = (image.width * scale).toInt().coerceAtLeast(1)
                val targetH = (image.height * scale).toInt().coerceAtLeast(1)
                image = Bitmap.createScaledBitmap(image, targetW, targetH, true)
            }
        }

        localImageCache.cache(path, image, resize)
        return image
    }

    fun purgeCache() {
        localImageCache.purgeCache()
    }
}
