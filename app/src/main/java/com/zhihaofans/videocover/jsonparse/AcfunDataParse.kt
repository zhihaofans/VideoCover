package com.zhihaofans.videocover.jsonparse

/**
 * Created by zhihaofans on 2017/10/18.
 */
class AcfunDataParse {
    private var title: String = ""
    private var image: String = ""
    private var isArticle: Int = 0

    fun getIsArticle(): Int {
        return isArticle
    }

    fun setIsArticle(isArticle: Int) {
        this.isArticle = isArticle
    }

    fun getTitle(): String {
        return title
    }

    fun setTitle(title: String) {
        this.title = title
    }

    fun getImage(): String {
        return image
    }

    fun setImage(image: String) {
        this.image = image
    }

}