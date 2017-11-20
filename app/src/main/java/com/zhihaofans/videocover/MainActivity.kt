package com.zhihaofans.videocover

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.facebook.drawee.backends.pipeline.Fresco
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zhihaofans.videocover.util.AcfunUtil
import com.zhihaofans.videocover.util.BilibiliUtil
import com.zhihaofans.videocover.util.KotlinUtil
import com.zhihaofans.videocover.util.SysUtil
import com.zhihaofans.videocover.view.AboutActivity
import com.zhihaofans.videocover.view.SingleActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick


class MainActivity : AppCompatActivity() {
    val REQUESTCODE = 1
    var getSu = false
    val sys = SysUtil()
    val set = sys.setting()
    val ad = KotlinUtil.Android()
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fresco.initialize(this)
        Fresco.getImagePipeline().clearCaches()
        setContentView(R.layout.activity_main)
        Logger.addLogAdapter(AndroidLogAdapter())
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
        rxPermissions
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
    }

    private fun exit() {
        System.exit(0)
    }

    private fun start() {
        toast(R.string.text_getPerSu)
        val a = indeterminateProgressDialog("Please wait a minute.", "Getting…")
        a.hide()
        sys.setContext(this)
        Logger.d("start()1")
        b_sAa.onClick {
            startActivity<AboutActivity>()
        }
        Logger.d("start()2")
        b_start.onClick {
            val c = listOf(getString(R.string.text_bilibili))
            selector("", c, { _, i ->
                Logger.d(i)
                when (i) {
                    0 -> {
                        val bili = BilibiliUtil()
                        val cc = listOf(getString(R.string.video))
                        selector(getString(R.string.text_bilibili), cc, { _, ii ->
                            Logger.d(ii)
                            when (ii) {
                                0 -> {
                                    alert {
                                        customView {
                                            verticalLayout {
                                                val et = editText {
                                                    hint = "av123456"
                                                    title = "${getString(R.string.text_bilibili)}-${getString(R.string.video)}"
                                                    setText(set.getString("bilibili_vid", getString(R.string.setting_defaultBilibiliVid)))
                                                    singleLine = true
                                                }
                                                positiveButton(R.string.text_get) {
                                                    a.show()
                                                    if (et.text.toString().isNotEmpty()) {
                                                        set.setString("bilibili_vid", et.text.toString())
                                                        var v_title = ""
                                                        var v_img = ""
                                                        var v_id = et.text.toString()
                                                        val b = doAsync {
                                                            //IO task or other computation with high cpu load
                                                            val reData = bili.getVideoAll(v_id)
                                                            v_img = reData["cover"].toString()
                                                            v_title = reData["title"].toString()
                                                            uiThread {
                                                                a.hide()
                                                                if (v_img.isNotEmpty()) {
                                                                    a.hide()
                                                                    startActivity<SingleActivity>("img" to v_img, "vid" to v_id, "title" to v_title)
                                                                } else {
                                                                    alert {
                                                                        title = getString(R.string.error_noResult)
                                                                        setFinishOnTouchOutside(false)
                                                                        okButton { title = getString(R.string.text_ok) }
                                                                    }
                                                                }

                                                            }
                                                        }
                                                        a.setOnCancelListener {
                                                            b.cancel(true)
                                                            a.hide()
                                                            if (b.isCancelled) {
                                                                toast(R.string.text_canceled)
                                                            } else {
                                                                toast(getString(R.string.text_canceled) + getString(R.string.text_fail))
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
                                            }
                                        }

                                    }.show()
                                }
                            }
                        })
                    }
                    1 -> {
                        val ac = AcfunUtil()
                        val xx = listOf(getString(R.string.video))
                        selector(getString(R.string.text_acfun), xx, { _, ii ->
                            Logger.d(ii)
                            when (ii) {
                                0 -> {
                                    alert {
                                        customView {
                                            verticalLayout {
                                                val et = editText {
                                                    hint = "av123456"
                                                    title = "${getString(R.string.text_acfun)}-${getString(R.string.video)}"
                                                    setText(set.getString("acfun_vid", getString(R.string.setting_defaultAcfunVid)))
                                                    singleLine = true
                                                }
                                                positiveButton(R.string.text_get) {
                                                    a.show()
                                                    if (et.text.toString().isNotEmpty()) {
                                                        set.setString("acfun_vid", et.text.toString())
                                                        var st = false
                                                        var v_title = ""
                                                        var img = set.getString("acfun_${et.text}", "")
                                                        val b = doAsync {
                                                            //IO task or other computation with high cpu load
                                                            if (img.isEmpty() || img == "") {
                                                                img = ac.getVideo(et.text, a)
                                                                if (img.isEmpty() || img == "") {
                                                                    st = true
                                                                }
                                                            } else {
                                                                v_title = ac.getVideoTitle(et.text, a)
                                                            }
                                                            uiThread {
                                                                a.hide()
                                                                if (st) {
                                                                    toast(R.string.error_noSupportPage)
                                                                    a.cancel()
                                                                } else {
                                                                    if (img.isNotEmpty()) {
                                                                        set.setString("acfun_${et.text}", img)
                                                                        startActivity<SingleActivity>("img" to img, "vid" to et.text.toString(), "title" to v_title)
                                                                    } else {
                                                                        alert {
                                                                            title = getString(R.string.error_noResult)
                                                                            setFinishOnTouchOutside(false)
                                                                            okButton { title = getString(R.string.text_ok) }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        a.setOnCancelListener {
                                                            b.cancel(true)
                                                            a.hide()
                                                            if (b.isCancelled) {
                                                                toast(R.string.text_canceled)
                                                            } else {
                                                                toast(getString(R.string.text_canceled) + getString(R.string.text_fail))
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
                                            }
                                        }

                                    }.show()
                                }
                            }
                        })
                    }
                }
            })
        }
        Logger.d("start()3")
    }

    fun stopPD(pd: ProgressDialog, stopMsg: String) {
        pd.hide()
        toast(stopMsg)
    }
}
