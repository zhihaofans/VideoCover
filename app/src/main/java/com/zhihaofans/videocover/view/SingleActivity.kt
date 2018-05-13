package com.zhihaofans.videocover.view

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Animatable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.facebook.common.executors.CallerThreadExecutor
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.drawable.ProgressBarDrawable
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.facebook.imagepipeline.backends.okhttp.OkHttpImagePipelineConfigFactory
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.image.ImageInfo
import com.facebook.imagepipeline.listener.RequestListener
import com.facebook.imagepipeline.listener.RequestLoggingListener
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.orhanobut.logger.Logger
import com.squareup.okhttp.OkHttpClient
import com.zhihaofans.videocover.R
import com.zhihaofans.videocover.util.KotlinUtil
import kotlinx.android.synthetic.main.activity_single.*
import kotlinx.android.synthetic.main.content_single.*
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick


class SingleActivity : AppCompatActivity() {
    private val str = KotlinUtil.StrUtil()
    private val ad = KotlinUtil.Android()
    private val imagePipeline = Fresco.getImagePipeline()
    private var video_info = mutableMapOf<String, String>()
    private var loadFailed = false
    private val image = KotlinUtil.images()

    override fun onDestroy() {
        super.onDestroy()
        iv.setImageURI("")
        imagePipeline.clearCaches()
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
        var coverBitmap: Bitmap = image.drawable2Bitmap(getDrawable(R.mipmap.ic_launcher))
        var su = false
        try {
            video_info = mutableMapOf(
                    "vid" to intent.extras.getString("vid", "获取失败"),
                    "title" to intent.extras.getString("title", "获取失败"),
                    "cover" to intent.extras.getString("cover", ""),
                    "author" to intent.extras.getString("author", "获取失败"),
                    "web" to intent.extras.getString("web", "获取失败"),
                    "site" to intent.extras.getString("site", "")
            )
            su = true
            Logger.d(video_info)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (!su) {
                this@SingleActivity.title = getString(R.string.error_loadImgFail)
                alert(message = "(loadData)", title = getString(R.string.error)) {
                    okButton {
                        finish()
                    }
                    onCancelled {
                        finish()
                    }
                }.show()
            } else {
                Logger.d("img:${video_info["cover"]}\nvid:${video_info["vid"]}\ntitle:${video_info["title"]}")
                if (video_info["cover"]!!.isEmpty()) {
                    alert(getString(R.string.error), "图片地址错误") {
                        setFinishOnTouchOutside(false)
                        yesButton {
                            finish()
                        }
                    }.show()
                }
                if (video_info["vid"]!!.isEmpty()) {
                    alert(getString(R.string.error), "视频id错误") {
                        setFinishOnTouchOutside(false)
                        yesButton {
                            finish()
                        }
                    }.show()
                }
                if (video_info["title"]!!.isEmpty()) {
                    video_info["title"] = "这个视频暂不支持获取标题，带来不便敬请谅解。"
                }
                title = getString(R.string.text_loading)
                val mOkHttpClient = OkHttpClient()
                val listeners: Set<RequestListener> = hashSetOf(RequestLoggingListener())
                val config: ImagePipelineConfig = OkHttpImagePipelineConfigFactory
                        .newBuilder(this@SingleActivity, mOkHttpClient)
                        .setDownsampleEnabled(true)
                        .setRequestListeners(listeners)
                        .build()
                Fresco.initialize(this@SingleActivity, config)
                val imageRequest = ImageRequestBuilder
                        .newBuilderWithSource(Uri.parse(video_info["cover"]))
                        .setProgressiveRenderingEnabled(true)
                        .build()
                val dataSource = imagePipeline.fetchDecodedImage(imageRequest, this@SingleActivity)
                dataSource.subscribe(object : BaseBitmapDataSubscriber() {
                    override fun onNewResultImpl(bitmap: Bitmap?) {
                        coverBitmap = bitmap!!
                    }

                    override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>?) {
                        coverBitmap = image.drawable2Bitmap(getDrawable(R.mipmap.ic_launcher))
                    }

                }, CallerThreadExecutor.getInstance())
                iv.hierarchy = GenericDraweeHierarchyBuilder(resources).setProgressBarImage(ProgressBarDrawable()).build()
                iv.controller = Fresco.newDraweeControllerBuilder()
                        .setUri(Uri.parse(video_info["cover"]))
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
                                this@SingleActivity.title = video_info["vid"]
                                Snackbar.make(coordinatorLayout_single, video_info["title"].toString(), Snackbar.LENGTH_LONG).setAction(R.string.text_copy, {
                                    if (video_info["title"]!!.isNotEmpty()) {
                                        cm.primaryClip = ClipData.newPlainText("Hi", video_info["title"])
                                        Snackbar.make(coordinatorLayout_single, "OK", Snackbar.LENGTH_SHORT).show()
                                    }
                                }).show()
                                val height = imageInfo.height
                                val width = imageInfo.width
                                val layoutParams = iv.layoutParams
                                layoutParams.width = ll_single.width
                                layoutParams.height = ll_single.height
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
                        Logger.d(video_info["cover"])
                        val countries = listOf("浏览器打开视频网页", "浏览器打开图片", "下载图片", "分享图片地址", "复制图片地址")
                        selector(video_info["vid"], countries, { _, i ->
                            when (i) {
                                0 -> browse(video_info["web"].toString())//浏览器打开网页
                                1 -> browse(video_info["cover"].toString())//浏览器打开图片
                                2 -> {
                                    val ext = str.path2ext(video_info["cover"].toString())
                                    val nowSite: String = video_info["site"].toString()
                                    val fileName = "${nowSite}_${video_info["vid"]}.$ext"
                                    val savePath = ad.getStorage() + Environment.DIRECTORY_PICTURES + "/videocover/"
                                    var showText = "$savePath$fileName"
                                    if (ad.saveImage(fileName, coverBitmap)) {
                                        ad.updateGallery(this@SingleActivity, savePath)
                                        showText += "\nOK"
                                        Snackbar.make(coordinatorLayout_single, "OK ($savePath$fileName)", Snackbar.LENGTH_SHORT).setAction(R.string.text_open, {
                                            ad.openJpgInOtherApp(this@SingleActivity, savePath + fileName)
                                        })
                                                .show()
                                    } else {
                                        showText += "\nNo"
                                        Snackbar.make(coordinatorLayout_single, "NO", Snackbar.LENGTH_SHORT).show()
                                    }
                                    Logger.d(showText)
                                }
                                3 -> share(video_info["cover"].toString())//分享图片地址
                                4 -> {
                                    //复制图片地址
                                    if (video_info["cover"]!!.isNotEmpty()) {
                                        cm.primaryClip = ClipData.newPlainText("Hi", video_info["cover"])
                                        Snackbar.make(coordinatorLayout_single, "OK", Snackbar.LENGTH_SHORT).show()
                                    }
                                }

                            }

                        })
                    }

                }
            }
        }
    }


}
