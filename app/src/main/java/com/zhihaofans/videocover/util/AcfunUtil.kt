package com.zhihaofans.videocover.util

import android.app.ProgressDialog
import android.text.Editable
import com.alibaba.fastjson.JSON
import com.orhanobut.logger.Logger
import com.zhihaofans.videocover.MainActivity
import com.zhihaofans.videocover.jsonparse.AcfunParse
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.io.IOException

/**
 * Created by zhihaofans on 2017/10/15.
 */
class AcfunUtil {
    val ma = MainActivity()
    private fun Logger.d(s: String) {
        Logger.d(s)
    }

    private fun error(s: String) {
        Logger.e(s)
    }

    fun getVideo(vid: String, pd: ProgressDialog): String {
        var imgUrl: String = ""
        try {
            imgUrl = Jsoup.connect("http://www.acfun.cn/v/$vid").get().select("div#pageInfo").attr("data-pic")
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

    fun getVideoJson(vid: String, pd: ProgressDialog): String {
        val imgUrl = "http://api.acfun.cn/videos/$vid"
        var acJson = ""
        var acfun = AcfunParse()
        return try {
            val response = OkHttpClient().newCall(Request.Builder().get().url(imgUrl).build()).execute()
            Logger.d("code:${response.code()}")
            if (response.isSuccessful) {
                acJson = response.body().toString()
                acfun = JSON.parseObject(acJson, AcfunParse::class.java)
                return ""
            } else {
                //......
                Logger.e("code:${response.code()}")
                ""
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Logger.e("error:$e")
            ""
        }

    }

    fun getVideo(vid: Editable, pd: ProgressDialog): String {
        return getVideoJson(vid.toString(), pd)
    }

    fun getVideoTitle(vid: String, pd: ProgressDialog): String {
        var t: String = ""
        try {
            t = Jsoup.connect("http://www.acfun.cn/v/$vid").get().select("div#pageInfo").attr("data-title")
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

    fun getVideoTitle(vid: Editable, pd: ProgressDialog): String {
        return getVideoTitle(vid.toString(), pd)
    }
}