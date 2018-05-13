package com.zhihaofans.videocover.util

import com.google.gson.Gson
import com.orhanobut.logger.Logger
import com.zhihaofans.videocover.gson.BiliBiliGalmoeGson
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.io.IOException

/**
 *
 * @author zhihaofans
 * @date 2017/12/4
 */
class SiteUtil {
    private val str = KotlinUtil.StrUtil()
    fun get(siteIndex: Int, vid: String): MutableMap<String, String> {
        return when (siteIndex) {
            0 -> bilibili(vid)
            1 -> youtube(vid)
            else -> mutableMapOf()
        }
    }

    fun bilibili(vid: String): MutableMap<String, String> {
        val reData = mutableMapOf("title" to "", "cover" to "", "author" to "", "web" to "https://www.bilibili.com/video/$vid")
        var videoCover = ""
        var videoTitle = ""
        var videoAuthor = ""
        var html = ""
        val client = OkHttpClient()
        var su = false
        try {
            val request = Request.Builder()
                    .url("https://www.bilibili.com/video/$vid/")
                    .build()
            val response = client.newCall(request).execute()
            html = response.body()!!.string()
            Logger.d(html)
            su = true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (!su || html.isEmpty()) {
                return reData
            }
            val ju = JsoupUtil(Jsoup.parse(html))
            videoAuthor = ju.safeAttr("head > meta[name=\"author\"]", "content")
            videoCover = ju.safeAttr("head > meta[itemprop=\"image\"]", "content")
            videoTitle = ju.safeAttr("head > meta[property=\"og:title\"]", "content")
            if (videoTitle.endsWith("_哔哩哔哩 (゜-゜)つロ 干杯~-bilibili")) videoTitle = videoTitle.replace("_哔哩哔哩 (゜-゜)つロ 干杯~-bilibili", "")
            if (videoCover.isEmpty()) videoCover = ju.safeAttr("head > meta[property=\"og:image\"]", "content")
            if (videoCover.isEmpty()) videoCover = bilibiliVideo3rd(vid)
            if (videoCover.isNotEmpty()) videoCover = str.urlAutoHttps(videoCover)
            reData["title"] = videoTitle
            reData["author"] = videoAuthor
            reData["cover"] = videoCover
            Logger.d(reData)
            return reData
        }
    }

    fun bilibiliVideo3rd(vid: String): String {
        var imgUrl = ""
        val g = Gson()
        return try {
            val response = OkHttpClient().newCall(Request.Builder().get().url("https://www.galmoe.com/t.php?aid=$vid").build()).execute()
            Logger.d("code:${response.code()}")
            if (response.isSuccessful) {
                val body = response.body()
                Logger.d(body)
                val rejson: String? = body?.string()
                Logger.d(rejson)
                val biliBiliGalmoeGson = g.fromJson(rejson, BiliBiliGalmoeGson::class.java)
                if (biliBiliGalmoeGson.result == 1) imgUrl = biliBiliGalmoeGson.url
            } else {
                Logger.e("code:${response.code()}")
            }
            imgUrl
        } catch (e: IOException) {
            e.printStackTrace()
            Logger.e("error:$e")
            imgUrl
        }
    }

    fun youtube(vid: String, quality: Int = 0): MutableMap<String, String> {
        return youtube(vid, quality, false)
    }


    fun youtube(vid: String, quality: Int = 0, onlyCover: Boolean): MutableMap<String, String> {
        val videoCover = when (quality) {
            0 -> "https://img.youtube.com/vi/$vid/maxresdefault.jpg"
            1 -> "https://img.youtube.com/vi/$vid/sddefault.jpg"
            2 -> "https://img.youtube.com/vi/$vid/hqdefault.jpg"
            3 -> "https://img.youtube.com/vi/$vid/mqdefault.jpg"
            else -> "https://img.youtube.com/vi/$vid/maxresdefault.jpg"
        }

        val reData = mutableMapOf("title" to "YouTube暂不支持获取标题，带来不便敬请谅解。", "cover" to videoCover, "author" to "YouTube暂不支持获取作者", "web" to "https://youtu.be/$vid")
        if (onlyCover) {
            Logger.d(reData)
            return reData
        }
        var videoTitle = ""
        var videoAuthor = ""
        var html = ""
        val client = OkHttpClient()
        var su = false
        try {
            val request = Request.Builder()
                    .url("https://www.youtube.com/watch?v=$vid")
                    .build()
            val response = client.newCall(request).execute()
            html = response.body()!!.string()
            su = true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (!su || html.isEmpty()) {
                return reData
            }
            val dom = Jsoup.parse(html)
            val ju = JsoupUtil(dom)
            val htmlStart = html.indexOf(",\"videoDetails\":{")
            // Video Author
            val videoAuthorStart = html.indexOf(",\"author\":\"", htmlStart) + 11
            val videoAuthorEnd = html.indexOf("\"", videoAuthorStart + 1)
            videoAuthor = html.substring(startIndex = videoAuthorStart, endIndex = videoAuthorEnd)
            // Video Title
            val videoTitleStart = html.indexOf("\",\"title\":\"", htmlStart) + 11
            val videoTitleEnd = html.indexOf("\"", videoTitleStart + 1)
            videoTitle = html.substring(startIndex = videoTitleStart, endIndex = videoTitleEnd)
            reData["title"] = videoTitle
            reData["author"] = videoAuthor
            Logger.d(reData)
            return reData
        }

    }

}