package com.zhihaofans.videocover.view

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.android.material.snackbar.Snackbar
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.tbruyelle.rxpermissions2.RxPermissions
import com.tencent.bugly.Bugly
import com.tencent.bugly.beta.Beta
import com.zhihaofans.videocover.R
import com.zhihaofans.videocover.util.KotlinUtil
import com.zhihaofans.videocover.util.SiteUtil
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {
    private var getSu = false
    private val ad = KotlinUtil.Android()
    private var nowSiteId = ""
    private var nowSiteIndex = 0
    private var nowSiteQualityIndex = 0
    private var siteList = mutableListOf<MutableMap<String, MutableList<String>>>()

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.addLogAdapter(AndroidLogAdapter())
        Fresco.initialize(this)
        Fresco.getImagePipeline().clearCaches()
        setContentView(R.layout.activity_main)
        buglyInit()
        ad.getStorage()
        siteList = mutableListOf(
                mutableMapOf(
                        "name" to mutableListOf("bilibili"),
                        "quality" to mutableListOf(getString(R.string.text_quality_default)),
                        "defauit_vid" to mutableListOf(getString(R.string.setting_defaultBilibiliVid))
                ),
                mutableMapOf(
                        "name" to mutableListOf("youtube"),
                        "quality" to mutableListOf("1080p+", "720p", "480p", "320p"),
                        "defauit_vid" to mutableListOf(getString(R.string.setting_defaultYoutubeVid))
                )
        )
        button_site.text = (siteList[nowSiteIndex]["name"] as MutableList<String>)[0]
        button_quality.text = (siteList[nowSiteIndex]["quality"] as MutableList<String>)[nowSiteQualityIndex]
        b_start.isClickable = false
        b_start.setOnClickListener {
            if (!getSu) {
                alert {
                    title = getString(R.string.error)
                    message = getString(R.string.error_needStoragePermission)
                    okButton {
                        exit()
                    }
                    onCancelled {
                        exit()
                    }
                }.show()
            } else {
                start()
            }
        }
        b_sAa.setOnClickListener {
            if (!getSu) {
                alert {
                    title = getString(R.string.error)
                    message = getString(R.string.error_needStoragePermission)
                    okButton {
                        exit()
                    }
                    onCancelled {
                        exit()
                    }
                }.show()
            } else {
                start()
            }
        }
        val rxPermissions = RxPermissions(this@MainActivity)
        rxPermissions.setLogging(true)
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe { granted ->
                    if (granted) { // Always true pre-M
                        // Yes
                        getSu = true
                        b_start.isClickable
                        start()
                    } else {
                        // No
                        alert {
                            title = getString(R.string.error)
                            message = getString(R.string.error_needStoragePermission)
                            okButton {
                                exit()
                            }
                            onCancelled {
                                exit()
                            }
                        }.show()
                    }
                }
        button_quality.setOnClickListener {
            selector("", siteList[nowSiteIndex]["quality"]!!) { _, i ->
                nowSiteQualityIndex = i
                button_site.text = (siteList[nowSiteIndex]["name"] as MutableList<String>)[0]
                button_quality.text = (siteList[nowSiteIndex]["quality"] as MutableList<String>)[nowSiteQualityIndex]
            }
        }
        button_site.setOnClickListener {
            val tempSizeList = siteList.map {
                it["name"]!![0]
            }
            selector("", tempSizeList) { _, i ->
                nowSiteIndex = i
                nowSiteQualityIndex = 0
                nowSiteId = (siteList[nowSiteIndex]["name"] as MutableList<String>)[0]
                button_site.text = nowSiteId
                button_quality.text = (siteList[nowSiteIndex]["quality"] as MutableList<String>)[nowSiteQualityIndex]
                editText_input.setText((siteList[nowSiteIndex]["defauit_vid"] as MutableList<String>)[0])
            }
        }
    }

    private fun exit() {
        exitProcess(0)
    }

    private fun start() {
        Snackbar.make(coordinatorLayout_main, R.string.text_getPerSu, Snackbar.LENGTH_SHORT).show()
        val progressdialogA = indeterminateProgressDialog("Please wait a minute.", "Getting…")
        progressdialogA.hide()
        Logger.d("start(1)")
        b_sAa.setOnClickListener {
            startActivity<AboutActivity>()
        }
        Logger.d("start(2)")
        b_start.setOnClickListener {
            val spNo = nowSiteIndex
            val vId = editText_input.text.toString()
            Logger.d(spNo)
            val site = SiteUtil()
            when (spNo) {
                0 -> {
                    progressdialogA.show()
                    if (vId.isNotEmpty()) {
                        val b = doAsync {
                            val reData = site.bilibili(vId)
                            uiThread {
                                progressdialogA.hide()
                                if (reData == null) {
                                    Snackbar.make(coordinatorLayout_main, "数据空白", Snackbar.LENGTH_SHORT).show()
                                } else {
                                    val vCover = reData.cover
                                    val vTitle = reData.title
                                    val vWeb = reData.web
                                    val vAuthor = reData.author
                                    if (vCover.isNotEmpty()) {
                                        startActivity<SingleActivity>("cover" to vCover, "vid" to vId, "title" to vTitle, "web" to vWeb, "author" to vAuthor, "site" to nowSiteId)
                                    } else {
                                        Snackbar.make(coordinatorLayout_main, getString(R.string.error_noSupportPage), Snackbar.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                        progressdialogA.setOnCancelListener {
                            b.cancel(true)
                            progressdialogA.hide()
                            if (b.isCancelled) {
                                Snackbar.make(coordinatorLayout_main, getString(R.string.text_canceled), Snackbar.LENGTH_SHORT).show()
                            } else {
                                Snackbar.make(coordinatorLayout_main, getString(R.string.text_canceled) + getString(R.string.text_fail), Snackbar.LENGTH_SHORT).show()
                            }
                        }

                    } else {
                        progressdialogA.hide()
                        alert {
                            title = getString(R.string.error_noInput)
                            setFinishOnTouchOutside(false)
                            okButton { title = getString(R.string.text_ok) }
                        }
                    }


                }
                1 -> {
                    //TODO("YouTube")
                    progressdialogA.show()
                    if (vId.isNotEmpty()) {
                        var vTitle: String
                        var vCover: String
                        var vWeb: String
                        var vAuthor: String
                        val b = doAsync {
                            val reData = site.youtube(vId, quality = nowSiteQualityIndex, onlyCover = true)
                            vCover = reData["cover"].toString()
                            vTitle = reData["title"].toString()
                            vWeb = reData["web"].toString()
                            vAuthor = reData["author"].toString()
                            uiThread {
                                progressdialogA.hide()
                                if (vCover.isNotEmpty()) {
                                    progressdialogA.hide()
                                    startActivity<SingleActivity>("cover" to vCover, "vid" to vId, "title" to vTitle, "web" to vWeb, "author" to vAuthor, "site" to nowSiteId)
                                } else {
                                    Snackbar.make(coordinatorLayout_main, getString(R.string.error_noResult), Snackbar.LENGTH_SHORT).show()
                                }
                            }
                        }
                        progressdialogA.setOnCancelListener {
                            b.cancel(true)
                            progressdialogA.hide()
                            if (b.isCancelled) {
                                Snackbar.make(coordinatorLayout_main, getString(R.string.text_canceled), Snackbar.LENGTH_SHORT).show()
                            } else {
                                Snackbar.make(coordinatorLayout_main, getString(R.string.text_canceled) + getString(R.string.text_fail), Snackbar.LENGTH_SHORT).show()
                            }
                        }

                    } else {
                        progressdialogA.hide()
                        alert {
                            title = getString(R.string.error_noInput)
                            setFinishOnTouchOutside(false)
                            okButton { title = getString(R.string.text_ok) }
                        }.show()
                    }


                }
            }

        }
        Logger.d("start()3")
    }

    private fun buglyInit() {
        Bugly.init(applicationContext, "f11a7870d7", true)//初始化
        Beta.enableNotification = true//设置在通知栏显示下载进度
        Beta.autoDownloadOnWifi = true//设置Wifi下自动下载
        Beta.enableHotfix = false//关闭热更新能力
    }
}
