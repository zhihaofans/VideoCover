package com.zhihaofans.videocover.view

import android.app.ProgressDialog
import android.content.ClipboardManager
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.drawable.ProgressBarDrawable
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.imagepipeline.image.ImageInfo
import com.orhanobut.logger.Logger
import com.zhihaofans.videocover.R
import com.zhihaofans.videocover.util.KotlinUtil
import com.zhihaofans.videocover.util.StrUtil
import com.zhihaofans.videocover.util.SysUtil
import kotlinx.android.synthetic.main.activity_single.*
import kotlinx.android.synthetic.main.content_single.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import java.net.URL


var imgUrl = ""
var vid = ""
var v_title = ""
var loadFailed = false

class SingleActivity : AppCompatActivity(), AnkoLogger {
    private val sys = SysUtil()
    private val str = StrUtil()
    private val set = sys.setting()
    private var aa: ProgressDialog? = null
    override fun onDestroy() {
        super.onDestroy()
        aa!!.dismiss()
        iv.setImageURI("")
        Fresco.getImagePipeline().clearCaches()
        //do something
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        //重写ToolBar返回按钮的行为，防止重新打开父Activity重走生命周期方法
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single)
        setSupportActionBar(toolbar_single)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        var a = indeterminateProgressDialog("Please wait a minute.")
        aa = a
        a.setCanceledOnTouchOutside(false)
        a.hide()
        sys.setContext(this)
        imgUrl = intent.extras.getString("img")
        vid = intent.extras.getString("vid")
        v_title = intent.extras.getString("title")
        Logger.d("img:$imgUrl\nvid:$vid\ntitle:$v_title")
        if (imgUrl.isEmpty()) {
            alert {
                title = getString(R.string.error)
                message = "图片地址错误"
                setFinishOnTouchOutside(false)
                yesButton {
                    finish()
                }
            }.show()
        }
        if (vid.isEmpty()) {
            alert {
                title = getString(R.string.error)
                message = "视频id错误"
                setFinishOnTouchOutside(false)
                yesButton {
                    finish()
                }
            }.show()
        }
        if (v_title.isEmpty()) {
            alert {
                title = getString(R.string.error)
                message = "视频标题错误"
                setFinishOnTouchOutside(false)
                yesButton {
                    finish()
                }
            }.show()
        }
        title = getString(R.string.text_loading)
        iv.hierarchy = GenericDraweeHierarchyBuilder(resources).setProgressBarImage(ProgressBarDrawable()).build()
        iv.controller = Fresco.newDraweeControllerBuilder()
                .setUri(Uri.parse(imgUrl))
                .setAutoPlayAnimations(true)
                .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                    override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) {
                        super.onFinalImageSet(id, imageInfo, animatable)
                        //1:获取bitmap
                        //2：存sd卡
                        //3：分享sd的图片
                        Logger.d("获取图片完毕")
                        if (imageInfo == null) {
                            return
                        }
                        title = vid
                        Snackbar.make(coordinatorLayout_single, v_title, Snackbar.LENGTH_LONG).setAction(R.string.text_copy, {
                            sys.copy(v_title, cm)
                        }).show()
                        val height = imageInfo.height
                        val width = imageInfo.width
                        val layoutParams = iv.layoutParams
                        if (cl.height == 0) {
                            cl.maxHeight = set.getInt("clHeight", cl.height)
                            cl.minHeight = set.getInt("clHeight", cl.height)
                        }
                        if (cl.width == 0) {
                            cl.maxWidth = set.getInt("clWidth", cl.width)
                            cl.minWidth = set.getInt("clWidth", cl.width)
                        }
                        layoutParams.width = ll_single.width
                        layoutParams.height = ll_single.height
                        set.setInt("clWidth", cl.width)
                        set.setInt("clHeight", cl.height)
                        //layoutParams.height = Integer.valueOf(((width * height).toFloat() / width.toFloat()).toInt().toString())
                        layoutParams.height = (cl.width.toFloat() / (width.toFloat() / height.toFloat())).toInt()
                        iv.layoutParams = layoutParams
                        Logger.d("height:${imageInfo.height}\nwidth:${imageInfo.width}\nlayoutParams.height:${layoutParams.height}\nlayoutParams.width:${layoutParams.width}")
                    }

                    override fun onFailure(id: String, throwable: Throwable) {
                        loadFailed = true
                        throwable.printStackTrace()
                        Handler().postDelayed({
                            //execute the task
                            toast(R.string.error_loadImgFail)
                            finish()
                        }, 500)

                    }
                })
                .build()
        ll.onClick {
            if (loadFailed) {
                toast(R.string.error_loadImgFail)
                finish()
            } else {
                Logger.d(imgUrl)
                val countries = listOf("浏览器打开视频网页", "浏览器打开图片", "下载图片", "分享图片地址", "复制图片地址")
                selector(vid, countries, { _, i ->
                    when (i) {
                        0 -> browse("https://www.bilibili.com/video/$vid/")//浏览器打开网页
                        1 -> browse(imgUrl)//浏览器打开图片
                        2 -> {
                            val ext = str.path2ext(imgUrl)
                            val fileName = "$vid.$ext"
                            Logger.d(fileName)
                            /*
                            selector("", listOf(), { _, ii ->
                                when(ii){

                                }

                            })
                            */
                            a.show()
                            val b = doAsync {
                                val su: Boolean = saveImg(imgUrl, "video_cover/", fileName)
                                uiThread {
                                    a.hide()
                                    var tipStr: String = ""
                                    if (su) {
                                        tipStr = getString(R.string.text_su)
                                    } else {
                                        tipStr = getString(R.string.text_fail)
                                    }
                                    toast(tipStr)
                                }
                            }
                            a.setOnCancelListener {
                                a = indeterminateProgressDialog("Please wait a minute.", "Getting…")
                                b.cancel(true)
                                toast(R.string.text_canceled)
                            }

                        }
                        3 -> share(imgUrl)//分享图片地址
                        4 -> {
                            //复制图片地址
                            if (sys.copy(imgUrl, cm)) {
                                toast("复制成功")
                            } else {
                                toast("复制失败")
                            }
                        }

                    }

                })
            }

        }
    }

    private fun saveImg(url: String, path: String, fileName: String): Boolean {
        val ad = KotlinUtil.Android()
        val net = KotlinUtil.Net()
        val str = StrUtil()
        val ext = str.path2ext(fileName)
        net.download(url, fileName)
        val b: ByteArray = net.getImageByte(URL(url)) as ByteArray
        if (b.isEmpty()) {
            error("saveImg|Empty")
            return false
        }
        when (ext) {
            "jpg", "png", "webp" -> return ad.saveBitmap("${ad.getStorage()}$path", fileName, ext, BitmapFactory.decodeByteArray(b, 0, b.size))
            else -> return ad.saveBytes("${ad.getStorage()}$path", fileName, b)
        }
    }

    fun saveToast(su: Boolean) {
        if (su) {
            toast(getString(R.string.text_save) + getString(R.string.text_su))
        } else {
            toast(getString(R.string.text_save) + getString(R.string.text_fail))
        }
    }


}
