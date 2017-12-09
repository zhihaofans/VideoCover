package com.zhihaofans.videocover.util

import org.jsoup.nodes.Document

/**
 * Created by zhihaofans on 2017/11/20.
 */
class JsoupUtil(inputDom: Document) {
    private val dom: Document? = inputDom

    fun safeAttr(cssQuery: String, attrName: String): String {
        val a = dom!!.select(cssQuery)
        if (a.isNotEmpty()) {
            val attr = a.attr(attrName)
            if (attr.isNotEmpty()) {
                return attr
            }
        }
        return ""
    }

    fun safeHtml(cssQuery: String): String {
        val a = dom!!.select(cssQuery)
        if (a.isNotEmpty()) {
            val html = a.html()
            if (html.isNotEmpty()) {
                return html
            }
        }
        return ""
    }

    fun safeText(cssQuery: String): String {
        val a = dom!!.select(cssQuery)
        if (a.isNotEmpty()) {
            val text = a.html()
            if (text.isNotEmpty()) {
                return text
            }
        }
        return ""
    }
}