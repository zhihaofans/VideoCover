package com.zhihaofans.videocover.util

/**
 * Created by zhihaofans on 2017/10/13.
 */
class StrUtil {
    fun urlAutoHttps(url: String): String {
        if (url.startsWith("//")) {
            return "https:$url"
        }
        return url
    }

    fun path2ext(path: String): String {
        if (path.isNotEmpty()) {
            val dot = path.lastIndexOf('.')
            if (dot > -1 && dot < path.length - 1) {
                return path.substring(dot + 1)
            }
        }
        return path
    }

    fun fromBoolean(boolean: Boolean, y: String, n: String): String {
        if (boolean) {
            return y
        } else {
            return n
        }
    }
}