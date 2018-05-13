package com.zhihaofans.videocover.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import com.orhanobut.logger.Logger
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


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

    }

    class Android {
        fun getStorage(): String {
            var a = Environment.getExternalStorageDirectory().absolutePath
            if (!a.endsWith("/")) {
                a = "$a/"
            }
            Logger.d(a)
            return a
        }

        fun saveImage(picName: String, picdata: Bitmap): Boolean {
            val BaseDir = getStorage() + Environment.DIRECTORY_PICTURES + "/videocover/"
            val f = File(BaseDir, picName)
            Logger.d("保存图片\n$BaseDir$picName")
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


        fun updateGallery(context: Context, filePath: String) {
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(File(filePath))))
        }

        fun openJpgInOtherApp(context: Context, file: String) {

            val intent = Intent()
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.action = Intent.ACTION_VIEW
            val uriJpg = Uri.parse(file)
            Logger.d(uriJpg)
            intent.setDataAndType(uriJpg, "image/jpeg")
            context.startActivity(intent)


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
}
