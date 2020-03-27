package com.zhihaofans.videocover.view

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.facebook.drawee.backends.pipeline.Fresco
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
import org.jetbrains.anko.sdk25.coroutines.onClick


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
                        "quality" to mutableListOf<String>(getString(R.string.text_quality_default)),
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
        b_start.onClick {
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
        b_sAa.onClick {
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
        button_quality.onClick {
            selector("", siteList[nowSiteIndex]["quality"]!!, { dialogInterface, i ->
                nowSiteQualityIndex = i
                button_site.text = (siteList[nowSiteIndex]["name"] as MutableList<String>)[0]
                button_quality.text = (siteList[nowSiteIndex]["quality"] as MutableList<String>)[nowSiteQualityIndex]
            })
        }
        button_site.onClick {
            val tempSizeList = siteList.map {
                it["name"]!![0]
            }
            selector("", tempSizeList, { dialogInterface, i ->
                nowSiteIndex = i
                nowSiteQualityIndex = 0
                nowSiteId = (siteList[nowSiteIndex]["name"] as MutableList<String>)[0]
                button_site.text = nowSiteId
                button_quality.text = (siteList[nowSiteIndex]["quality"] as MutableList<String>)[nowSiteQualityIndex]
                editText_input.setText((siteList[nowSiteIndex]["defauit_vid"] as MutableList<String>)[0])
            })
        }
    }

    private fun exit() {
        System.exit(0)
    }

    private fun start() {
        Snackbar.make(coordinatorLayout_main, R.string.text_getPerSu, Snackbar.LENGTH_SHORT).show()
        val a = indeterminateProgressDialog("Please wait a minute.", "Getting…")
        a.hide()
        Logger.d("start()1")
        b_sAa.onClick {
            startActivity<AboutActivity>()
        }
        Logger.d("start()2")
        b_start.onClick {
            val spNo = nowSiteIndex
            val vId = editText_input.text.toString()
            Logger.d(spNo)
            val site = SiteUtil()
            when (spNo) {
                0 -> {
                    a.show()
                    if (vId.isNotEmpty()) {
                        var vTitle: String
                        var vCover: String
                        var vWeb: String
                        var vAuthor: String
                        val b = doAsync {
                            val reData = site.get(0, vId)
                            vCover = reData["cover"].toString()
                            vTitle = reData["title"].toString()
                            vWeb = reData["web"].toString()
                            vAuthor = reData["author"].toString()
                            uiThread {
                                a.hide()
                                if (vCover.isNotEmpty()) {
                                    a.hide()
                                    startActivity<SingleActivity>("cover" to vCover, "vid" to vId, "title" to vTitle, "web" to vWeb, "author" to vAuthor, "site" to nowSiteId)
                                } else {
                                    Snackbar.make(coordinatorLayout_main, getString(R.string.error_noSupportPage), Snackbar.LENGTH_SHORT).show()
                                }
                            }
                        }
                        a.setOnCancelListener {
                            b.cancel(true)
                            a.hide()
                            if (b.isCancelled) {
                                Snackbar.make(coordinatorLayout_main, getString(R.string.text_canceled), Snackbar.LENGTH_SHORT).show()
                            } else {
                                Snackbar.make(coordinatorLayout_main, getString(R.string.text_canceled) + getString(R.string.text_fail), Snackbar.LENGTH_SHORT).show()
                            }
                        }

                    } else {
                        a.hide()
                        alert {
                            title = getString(R.string.error_noInput)
                            setFinishOnTouchOutside(false)
                            okButton { title = getString(R.string.text_ok) }
                        }
                    }


                }
                1 -> {
                    //TODO("YouTube")
                    a.show()
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
                                a.hide()
                                if (vCover.isNotEmpty()) {
                                    a.hide()
                                    startActivity<SingleActivity>("cover" to vCover, "vid" to vId, "title" to vTitle, "web" to vWeb, "author" to vAuthor, "site" to nowSiteId)
                                } else {
                                    Snackbar.make(coordinatorLayout_main, getString(R.string.error_noResult), Snackbar.LENGTH_SHORT).show()
                                }
                            }
                        }
                        a.setOnCancelListener {
                            b.cancel(true)
                            a.hide()
                            if (b.isCancelled) {
                                Snackbar.make(coordinatorLayout_main, getString(R.string.text_canceled), Snackbar.LENGTH_SHORT).show()
                            } else {
                                Snackbar.make(coordinatorLayout_main, getString(R.string.text_canceled) + getString(R.string.text_fail), Snackbar.LENGTH_SHORT).show()
                            }
                        }

                    } else {
                        a.hide()
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
