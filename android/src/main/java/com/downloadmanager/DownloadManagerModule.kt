package com.downloadmanager

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.util.LongSparseArray
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap


class DownloadManagerModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {


  override fun getName(): String {
    return NAME
  }


  private var appDownloads = LongSparseArray<Callback>()
  private var downloader: Downloader = Downloader(reactContext)

  private val downloadReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      try {
        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
        if (appDownloads.indexOfKey(downloadId) >= 0) {
          val downloadStatus = downloader.checkDownloadStatus(downloadId)
          val downloadOnDoneCb = appDownloads.get(downloadId)

          if (downloadStatus.getString("status")!!.equals("STATUS_SUCCESSFUL", ignoreCase = true)) {
            downloadOnDoneCb.invoke(null, downloadStatus)
          } else {
            downloadOnDoneCb.invoke(downloadStatus, null)
          }
          appDownloads.remove(downloadId)
        }
      } catch (e: Exception) {
        Log.e("RN_DOWNLOAD_MANAGER", Log.getStackTraceString(e))
      }
    }
  }

  fun ReactNativeDownloadManagerModule(reactContext: ReactApplicationContext) {
    downloader = Downloader(reactContext)
    appDownloads = LongSparseArray()
    val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
    reactContext.registerReceiver(downloadReceiver, filter)
  }


  @ReactMethod
  fun download(url: String, headers: ReadableMap, config: ReadableMap, onDone: Callback) {
    try {
      val request = downloader.createRequest(url, headers, config)
      val downloadId = downloader.queueDownload(request)
      appDownloads.put(downloadId, onDone)
    } catch (e: Exception) {
      onDone.invoke(e.message, null)
    }
  }

  @ReactMethod
  fun queueDownload(url: String, headers: ReadableMap, config: ReadableMap, onStart: Callback) {
    try {
      val request = downloader.createRequest(url, headers, config)
      val downloadId = downloader.queueDownload(request)
      onStart.invoke(null, downloadId.toString())
    } catch (e: Exception) {
      onStart.invoke(e.message, null)
    }
  }

  @ReactMethod
  fun attachOnCompleteListener(downloadId: String, onComplete: Callback) {
    try {
      val dloadId = downloadId.toLong()
      appDownloads.put(dloadId, onComplete)
      val status = downloader.checkDownloadStatus(dloadId)
      val alreadyDoneStatuses = listOf("STATUS_SUCCESSFUL", "STATUS_FAILED")
      val currentStatus = status.getString("status") ?: ""
      if (alreadyDoneStatuses.contains(currentStatus)) {
        appDownloads.remove(dloadId)
        onComplete.invoke(null, status)
      }
    } catch (e: Exception) {
      onComplete.invoke(e.message, null)
    }
  }

  @ReactMethod
  fun cancel(downloadId: String, onCancel: Callback) {
    try {
      downloader.cancelDownload(downloadId.toLong())
      onCancel.invoke(null, downloadId)
    } catch (e: Exception) {
      onCancel.invoke(e.message, null)
    }
  }

  @ReactMethod
  fun checkStatus(downloadId: String, onStatus: Callback) {
    try {
      val status = downloader.checkDownloadStatus(downloadId.toLong())
      onStatus.invoke(null, status)
    } catch (e: Exception) {
      onStatus.invoke(e.message, null)
    }
  }

  companion object {
    const val NAME = "DownloadManager"
  }
}
