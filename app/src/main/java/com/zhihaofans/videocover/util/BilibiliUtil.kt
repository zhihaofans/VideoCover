package com.zhihaofans.videocover.util

import android.app.ProgressDialog
import android.text.Editable
import com.orhanobut.logger.Logger
import com.zhihaofans.videocover.MainActivity
import okhttp3.*
import org.jsoup.Jsoup
import java.util.*


/**
 *
 * @author zhihaofans
 * @date 2017/10/13
 */
class BilibiliUtil {
    private val str = KotlinUtil.StrUtil()


    @Suppress("UNREACHABLE_CODE")
    fun getVideoAll(vid: String): MutableMap<String, String> {
        var reData = mutableMapOf("title" to "", "cover" to "", "author" to "")
        var videoCover = ""
        var videoTitle = ""
        var videoAuthor = ""
        var videoDescription = ""
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
            val jsoupUtil = JsoupUtil(dom)
            videoAuthor = jsoupUtil.safeAttr("head > meta[name=\"author\"]", "content")
            videoCover = jsoupUtil.safeAttr("img.cover_image", "src")
            videoTitle = jsoupUtil.safeAttr("div.v-title > h1", "title")
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
        } finally {
            Logger.d("vid;$vid|imgUrl:$imgUrl")
            if (imgUrl.isNotEmpty()) {
                val str = KotlinUtil.StrUtil()
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