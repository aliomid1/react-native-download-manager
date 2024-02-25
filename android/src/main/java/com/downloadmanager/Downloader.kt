package com.downloadmanager

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeMap

class Downloader(private val context: Context) {
  private val downloadManager: DownloadManager

  init {
    downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
  }

  fun createRequest(
    url: String?, headers: ReadableMap, requestConfig: ReadableMap
  ): DownloadManager.Request {
    val downloadTitle = requestConfig.getString("downloadTitle")
    val downloadDescription = requestConfig.getString("downloadTitle")
    val saveAsName = requestConfig.getString("saveAsName")
    val external = requestConfig.getBoolean("external")
    val external_path = requestConfig.getString("path")
    val allowedInRoaming = requestConfig.getBoolean("allowedInRoaming")
    val allowedInMetered = requestConfig.getBoolean("allowedInMetered")
    val showInDownloads = requestConfig.getBoolean("showInDownloads")
    val downloadUri = Uri.parse(url)
    val request = DownloadManager.Request(downloadUri)
    val iterator = headers.keySetIterator()
    while (iterator.hasNextKey()) {
      val key = iterator.nextKey()
      request.addRequestHeader(key, headers.getString(key))
    }
    if (external) {
      request.setDestinationInExternalPublicDir(external_path, saveAsName)
    } else {
      request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, saveAsName)
    }
    request.setTitle(downloadTitle)
    request.setDescription(downloadDescription)
    request.setAllowedOverRoaming(allowedInRoaming)
    request.setAllowedOverMetered(allowedInMetered)
    request.setVisibleInDownloadsUi(showInDownloads)
    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
    return request
  }

  fun queueDownload(request: DownloadManager.Request?): Long {
    return downloadManager.enqueue(request)
  }

  fun checkDownloadStatus(downloadId: Long): WritableMap {
    val downloadQuery = DownloadManager.Query()
    downloadQuery.setFilterById(downloadId)
    val cursor = downloadManager.query(downloadQuery)
    var result = HashMap<String?, String?>()
    if (cursor.moveToFirst()) {
      result = getDownloadStatus(cursor, downloadId)
    } else {
      result["status"] = "UNKNOWN"
      result["reason"] = "COULD_NOT_FIND"
      result["downloadId"] = downloadId.toString()
    }
    val wmap: WritableMap = WritableNativeMap()
    for ((key, value) in result) {
      wmap.putString(key, value)
    }
    return wmap
  }

  fun cancelDownload(downloadId: Long): Int {
    return downloadManager.remove(downloadId)
  }

  private fun getDownloadStatus(cursor: Cursor, downloadId: Long): HashMap<String?, String?> {
    val columnStatusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
    val STATUS = cursor.getInt(columnStatusIndex)
    val columnReasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
    val REASON = cursor.getInt(columnReasonIndex)
    val filenameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
    val filename = cursor.getString(filenameIndex)
    var statusText = ""
    var reasonText: String? = ""
    when (STATUS) {
      DownloadManager.STATUS_FAILED -> {
        statusText = "STATUS_FAILED"
        reasonText = when (REASON) {
          DownloadManager.ERROR_CANNOT_RESUME -> "ERROR_CANNOT_RESUME"
          DownloadManager.ERROR_DEVICE_NOT_FOUND -> "ERROR_DEVICE_NOT_FOUND"
          DownloadManager.ERROR_FILE_ALREADY_EXISTS -> "ERROR_FILE_ALREADY_EXISTS"
          DownloadManager.ERROR_FILE_ERROR -> "ERROR_FILE_ERROR"
          DownloadManager.ERROR_HTTP_DATA_ERROR -> "ERROR_HTTP_DATA_ERROR"
          DownloadManager.ERROR_INSUFFICIENT_SPACE -> "ERROR_INSUFFICIENT_SPACE"
          DownloadManager.ERROR_TOO_MANY_REDIRECTS -> "ERROR_TOO_MANY_REDIRECTS"
          DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> "ERROR_UNHANDLED_HTTP_CODE"
          else -> "ERROR_UNKNOWN"
        }
      }

      DownloadManager.STATUS_PAUSED -> {
        statusText = "STATUS_PAUSED"
        reasonText = when (REASON) {
          DownloadManager.PAUSED_QUEUED_FOR_WIFI -> "PAUSED_QUEUED_FOR_WIFI"
          DownloadManager.PAUSED_UNKNOWN -> "PAUSED_UNKNOWN"
          DownloadManager.PAUSED_WAITING_FOR_NETWORK -> "PAUSED_WAITING_FOR_NETWORK"
          DownloadManager.PAUSED_WAITING_TO_RETRY -> "PAUSED_WAITING_TO_RETRY"
          else -> "UNKNOWN"
        }
      }

      DownloadManager.STATUS_PENDING -> statusText = "STATUS_PENDING"
      DownloadManager.STATUS_RUNNING -> statusText = "STATUS_RUNNING"
      DownloadManager.STATUS_SUCCESSFUL -> {
        statusText = "STATUS_SUCCESSFUL"
        reasonText = filename
      }

      else -> {
        statusText = "STATUS_UNKNOWN"
        reasonText = STATUS.toString()
      }
    }
    val result = HashMap<String?, String?>()
    result["status"] = statusText
    result["reason"] = reasonText
    result["downloadId"] = downloadId.toString()
    return result
  }
}
