package com.zhihaofans.videocover.util

import com.orhanobut.logger.Logger
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup

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
            su = true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (!su || html.isEmpty()) {
                return reData
            }
            val dom = Jsoup.parse(html)
            val ju = JsoupUtil(dom)
            videoAuthor = ju.safeAttr("head > meta[name=\"author\"]", "content")
            videoCover = ju.safeAttr("img.cover_image", "src")
            videoTitle = ju.safeAttr("div.v-title > h1", "title")
            if (videoCover.isNotEmpty()) {
                videoCover = str.urlAutoHttps(videoCover)
            }
            reData["title"] = videoTitle
            reData["author"] = videoAuthor
            reData["cover"] = videoCover
            Logger.d(reData)
            return reData
        }
    }

    fun youtube(vid: String): MutableMap<String, String> {
        return youtube(vid, false)
    }


    fun youtube(vid: String, onlyCover: Boolean): MutableMap<String, String> {
        val videoCover = "http://i.ytimg.com/vi/$vid/maxresdefault.jpg"
        val reData = mutableMapOf("title" to "YouTube暂不支持获取标题", "cover" to videoCover, "author" to "YouTube暂不支持获取作者", "web" to "https://youtu.be/$vid")
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