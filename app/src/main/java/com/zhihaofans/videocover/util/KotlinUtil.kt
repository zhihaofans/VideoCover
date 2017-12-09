package com.zhihaofans.videocover.util

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Build
import android.os.Environment
import com.orhanobut.logger.Logger
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable


/**
 * Created by zhihaofans on 2017/10/16.
 */
class KotlinUtil {

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

    class Android {
        fun getStoragePermission(activity: Activity): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                return false
            }
            return true
        }

        fun checkStoragePermission(activity: Activity): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                return false
            }
            return true
        }

        fun getStorage(): String {
            var a = Environment.getExternalStorageDirectory().absolutePath
            if (!a.endsWith("/")) {
                a = "$a/"
            }
            Logger.d(a)
            return a
        }

        fun getExtStorage(): String {
            var a = System.getenv("SECONDARY_STORAGE")
            if (!a.endsWith("/")) {
                a = "$a/"
            }
            return a
        }

        fun saveImage(picName: String, picdata: Bitmap): Boolean {
            val BaseDir = System.getenv("EXTERNAL_STORAGE") + "/videocover/"
            val f = File(BaseDir, picName)
            Logger.d("保存图片\n" + BaseDir + picName)
            f.mkdirs()
            if (f.exists()) {
                f.delete()
            }
            try {
                val out = FileOutputStream(f)
                picdata.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                out.close()
                Logger.d("已经保存")
                return true
            } catch (e: FileNotFoundException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
                Logger.e("保存失败(FileNotFoundException)")
                return false
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
                Logger.e("保存失败(IOException)")
                return false
            }

        }

        fun saveBitmap(path: String, fileName: String, ext: String, bitmap: Bitmap): Boolean {
            Logger.d("saveBitmap", "path:$path\nfile name:$fileName\next:$ext")
            val dir = File(path)
            Logger.d("saveBitmap", "$path:${StrUtil().fromBoolean(dir.exists(), "存在", "不存在")}")
            if (!dir.exists()) {
                dir.mkdirs()
            } else if (!dir.isDirectory) {
                dir.delete()
                dir.mkdirs()
            }
            Logger.d("saveBitmap", "$path:${StrUtil().fromBoolean(dir.exists(), "存在", "不存在")}")
            if (dir.exists()) {
                val f = File("$path$fileName")
                if (f.exists()) {
                    f.delete()
                }
                f.createNewFile()
                try {
                    val out = FileOutputStream(f)
                    when (ext) {
                        "jpg" -> bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                        "png" -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        "webp" -> bitmap.compress(Bitmap.CompressFormat.WEBP, 100, out)
                        else -> return false
                    }
                    out.flush()
                    out.close()
                    Logger.i(TAG, "已经保存")
                    return true
                } catch (e: FileNotFoundException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                } catch (e: IOException) {
                    // TODO Auto-generated catch block
                    e.printStackTrace()
                }
            }
            return false
        }

        fun saveBytes(path: String, fileName: String, bytes: ByteArray): Boolean {
            Logger.d("saveBytes", "path:$path\nfile name:$fileName")
            if (bytes.isEmpty()) {
                return false
            }
            val f = File(path)
            Logger.d("saveBitmap", "$path:${StrUtil().fromBoolean(f.exists(), "存在", "不存在")}")
            if (!f.exists()) {
                f.mkdirs()
            } else {
                if (!f.isDirectory) {
                    f.delete()
                }
            }
            Logger.d("saveBitmap", "$path:${StrUtil().fromBoolean(f.exists(), "存在", "不存在")}")
            if (f.exists()) {
                val file = File("$path/$fileName")
                if (file.exists()) {
                    file.delete()
                }
                file.createNewFile()
                var s = false
                try {
                    val fos = FileOutputStream(file)
                    fos.write(bytes)
                    fos.close()
                    s = true
                } catch (e: Exception) {
                    e.printStackTrace()
                    Logger.e("存储出错", e.message)
                    s = false
                } finally {
                    return s
                }
            }
            return false

        }
    }

    class Net {
        fun getImageBitmap(url: URL): Bitmap? {
            val bytes = getImageByte(url)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes!!.size)
        }

        fun getImageByte(url: URL): ByteArray? {
            return try {
                val response = OkHttpClient().newCall(Request.Builder().get().url(url).build()).execute()
                if (response.isSuccessful) {
                    response.body()!!.bytes()
                } else {
                    //......
                    Logger.e("getImageByte", "code:${response.code()}")
                    null
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Logger.e("getImageByte", "error:$e")
                null
            }
        }
    }

    class images {
        fun drawable2Bitmap(drawable: Drawable): Bitmap {
            val bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    if (drawable.opacity != PixelFormat.OPAQUE)
                        Bitmap.Config.ARGB_8888
                    else
                        Bitmap.Config.RGB_565)
            val canvas = Canvas(bitmap)
            //canvas.setBitmap(bitmap);
            drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
            drawable.draw(canvas)
            return bitmap
        }
    }

    fun getText(url: URL): String {
        val response = OkHttpClient().newCall(Request.Builder().get().url(url).build()).execute()
        Logger.d("getImageByte", "code:${response.code()}")
        return if (response.isSuccessful) {
            response.body().toString()
        } else {
            //......
            Logger.e("getImageByte", "code:${response.code()}")
            ""
        }

    }
}
