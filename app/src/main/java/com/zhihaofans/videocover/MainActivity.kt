package com.zhihaofans.videocover

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import com.facebook.drawee.backends.pipeline.Fresco
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zhihaofans.videocover.util.KotlinUtil
import com.zhihaofans.videocover.util.SiteUtil
import com.zhihaofans.videocover.view.AboutActivity
import com.zhihaofans.videocover.view.SingleActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick


class MainActivity : AppCompatActivity() {
    private var getSu = false
    private val ad = KotlinUtil.Android()
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fresco.initialize(this)
        Fresco.getImagePipeline().clearCaches()
        setContentView(R.layout.activity_main)
        Logger.addLogAdapter(AndroidLogAdapter())
        ad.getStorage()
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

        sp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
                when (pos) {
                    0 -> editText_input.setText(R.string.setting_defaultBilibiliVid)
                    1 -> editText_input.setText(R.string.setting_defaultYoutubeVid)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing.
            }
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
            val spNo = sp.selectedItemPosition
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
                                    startActivity<SingleActivity>("cover" to vCover, "vid" to vId, "title" to vTitle, "web" to vWeb, "author" to vAuthor)
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
                            val reData = site.youtube(vId, true)
                            vCover = reData["cover"].toString()
                            vTitle = reData["title"].toString()
                            vWeb = reData["web"].toString()
                            vAuthor = reData["author"].toString()
                            uiThread {
                                a.hide()
                                if (vCover.isNotEmpty()) {
                                    a.hide()
                                    startActivity<SingleActivity>("cover" to vCover, "vid" to vId, "title" to vTitle, "web" to vWeb, "author" to vAuthor)
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
}
