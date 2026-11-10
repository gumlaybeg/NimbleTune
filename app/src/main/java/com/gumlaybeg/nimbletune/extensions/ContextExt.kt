package com.gumlaybeg.nimbletune.extensions

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.gumlaybeg.nimbletune.constants.InnerTubeCookieKey
import com.gumlaybeg.nimbletune.constants.YtmSyncKey
import com.gumlaybeg.nimbletune.constants.LikedAutoDownloadKey
import com.gumlaybeg.nimbletune.constants.LikedAutodownloadMode
import com.gumlaybeg.nimbletune.utils.dataStore
import com.gumlaybeg.nimbletune.utils.get
import com.music.innertube.utils.parseCookieString
import kotlinx.coroutines.runBlocking

fun Context.isSyncEnabled(): Boolean {
    return runBlocking {
        val ytmSync = dataStore[YtmSyncKey] ?: true
        ytmSync && isUserLoggedIn()
    }
}

fun Context.isUserLoggedIn(): Boolean {
    return runBlocking {
        val cookie = dataStore[InnerTubeCookieKey] ?: ""
        "SAPISID" in parseCookieString(cookie) && isInternetConnected()
    }
}

fun Context.isInternetConnected(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
}

fun Context.getLikeAutoDownload(): LikedAutodownloadMode  {
    return dataStore[LikedAutoDownloadKey].toEnum(LikedAutodownloadMode.OFF)
}