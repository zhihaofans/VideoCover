package com.zhihaofans.videocover.jsonparse

/**
 * Created by zhihaofans on 2017/10/18.
 */
class AcfunParse {
    private var code: Int = 0
    private var message: String = ""
    private var data: AcfunDataParse = AcfunDataParse()
    fun getCode(): Int {
        return code
    }

    fun setCode(code: Int) {
        this.code = code
    }

    fun getMessage(): String {
        return message
    }

    fun setMessage(message: String) {
        this.message = message
    }

    fun getData(): AcfunDataParse {
        return data
    }

    fun setData(data: AcfunDataParse) {
        this.data = data
    }
}