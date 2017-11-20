package com.zhihaofans.videocover.util

import android.app.ProgressDialog
import android.text.Editable
import com.orhanobut.logger.Logger
import com.zhihaofans.videocover.MainActivity
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup


/**
 * Created by zhihaofans on 2017/10/13.
 */
class BilibiliUtil {
    val ma = MainActivity()
    val str = StrUtil()
    val jsouputil = JsoupUtil()
    private fun Logger.d(s: String) {
        Logger.d(s)
    }

    private fun error(s: String) {
        Logger.e(s)
    }

    @Suppress("UNREACHABLE_CODE")
    fun getVideoAll(vid: String): MutableMap<String, String> {
        var reData = mutableMapOf("title" to "", "cover" to "", "author" to "", "description" to "")
        var videoCover: String
        var videoTitle: String
        var videoAuthor: String
        var videoDescription: String
        var html = ""
        val client = OkHttpClient()
        var su = false
        try {
            val request = Request.Builder()
                    .url("https://www.bilibili.com/video/$vid/")
                    .build()
            val response = client.newCall(request).execute()
            html = response.body()!!.string()
            su = true
        } catch (e: Exception) {
            error(e.toString())
        } finally {
            if (!su || html.isEmpty()) {
                return reData
            }
            val dom = Jsoup.parse(html)
            jsouputil.init(dom)
            videoDescription = jsouputil.safeAttr("head > meta[name=\"description\"]", "content")
            videoAuthor = jsouputil.safeAttr("head > meta[name=\"author\"]", "content")
            videoCover = jsouputil.safeAttr("img.cover_image", "src")
            videoTitle = jsouputil.safeAttr("div.v-title > h1", "title")
            if (videoCover.isNotEmpty()) {
                videoCover = str.urlAutoHttps(videoCover)
            }
            reData = mutableMapOf("title" to videoTitle, "cover" to videoCover, "author" to videoAuthor, "description" to videoDescription)
            Logger.d(reData)
            return reData
        }

    }

    fun getVideo(vid: String, pd: ProgressDialog): String {
        var imgUrl = ""
        try {
            imgUrl = Jsoup.connect("https://www.bilibili.com/video/$vid/").get().select("img.cover_image").attr("src")
        } catch (e: Exception) {
            error(e.toString())
            ma.stopPD(pd, e.toString())
        } finally {
            Logger.d("vid;$vid|imgUrl:$imgUrl")
            if (imgUrl.isNotEmpty()) {
                val str = StrUtil()
                return str.urlAutoHttps(imgUrl)
            }
            return ""
        }

    }

    fun getVideoTitle(vid: String, pd: ProgressDialog): String {
        var t: String = ""
        try {
            t = Jsoup.connect("https://www.bilibili.com/video/$vid/").get().select("div.v-title").text()
        } catch (e: Exception) {
            error(e.toString())
            ma.stopPD(pd, e.toString())
        } finally {
            Logger.d("vid;$vid|title:$t")
            if (t.isNotEmpty()) {
                return t
            }
            return ""
        }
    }

    fun getVideoAll(vid: Editable): MutableMap<String, String> {
        return getVideoAll(vid.toString())
    }

    fun getVideo(vid: Editable, pd: ProgressDialog): String {
        return getVideo(vid.toString(), pd)
    }

    fun getVideoTitle(vid: Editable, pd: ProgressDialog): String {
        return getVideoTitle(vid.toString(), pd)
    }
}