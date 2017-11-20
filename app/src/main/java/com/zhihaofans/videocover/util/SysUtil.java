package com.zhihaofans.videocover.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.orhanobut.logger.Logger;
import com.zhihaofans.videocover.R;
import com.zhihaofans.videocover.view.SingleActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * @author zhihaofans
 * @date 2017/10/15
 */

public class SysUtil {
    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    public Boolean copy(String str, ClipboardManager clipManager) {
        if (str != null) {
            if (clipManager != null) {
                clipManager.setPrimaryClip(ClipData.newPlainText("Hi", str));
                return true;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    public class setting {
        public String getString(String name, String defaultStr) {
            String str = PreferenceManager.getDefaultSharedPreferences(context).getString(name, defaultStr);
            if (Objects.equals(str, defaultStr)) {
                if (!setString(name, defaultStr)) {
                    Logger.e("set on getString:NO");
                }
            }
            return str;
        }

        public Boolean setString(String name, String value) {
            return PreferenceManager.getDefaultSharedPreferences(context).edit().putString(name, value).commit();
        }

        public int getInt(String name, int defaultInt) {
            int i = PreferenceManager.getDefaultSharedPreferences(context).getInt(name, defaultInt);
            if (Objects.equals(i, defaultInt)) {
                if (!setInt(name, defaultInt)) {
                    Logger.e("set on getString:NO");
                }
            }
            return i;
        }

        public Boolean setInt(String name, int value) {
            return PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(name, value).commit();
        }

        public Boolean clearAll() {
            return PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit();
        }
    }

    public String getStoragePath() {
        return context.getFilesDir().getPath();
    }

    public void saveImg(String url, String fileName) {
        final SingleActivity sa = new SingleActivity();
        final String localSavePath = getStoragePath() + "/VideoCover/" + fileName;
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url)).setProgressiveRenderingEnabled(true).build();
        DataSource<CloseableReference<CloseableImage>> dataSource = Fresco.getImagePipeline().fetchDecodedImage(imageRequest, null);
        dataSource.subscribe(new BaseBitmapDataSubscriber() {
            @Override
            public void onNewResultImpl(@Nullable Bitmap bitmap) {
                if (bitmap != null) {
                    if (saveBitmap(bitmap, localSavePath)) {
                        Logger.d("saveImg:Y");
                        Toast.makeText(context, context.getString(R.string.text_save) + context.getString(R.string.text_su), Toast.LENGTH_SHORT).show();
                        //保存成功处理
                    } else {
                        Logger.e("saveImg:N");
                        Toast.makeText(context, context.getString(R.string.text_save) + context.getString(R.string.text_fail), Toast.LENGTH_SHORT).show();
                        //保存失败处理
                    }
                } else {
                    Logger.e("saveImg:N(empty)");
                    Toast.makeText(context, context.getString(R.string.text_save) + context.getString(R.string.text_fail), Toast.LENGTH_SHORT).show();
                    //保存失败处理
                }
            }

            @Override
            public void onFailureImpl(DataSource dataSource) {
                Logger.e("saveImg", "N(Failure)");
                Toast.makeText(context, context.getString(R.string.text_save) + context.getString(R.string.text_fail), Toast.LENGTH_SHORT).show();
                //保存失败处理
            }
        }, CallerThreadExecutor.getInstance());
    }

    public Boolean saveBitmap(Bitmap bitmap, String localSavePath) {
        if (localSavePath.isEmpty()) {
            throw new NullPointerException("保存的路径不能为空");
        }
        File f = new File(localSavePath);
        if (f.exists()) {// 如果本来存在的话，删除
            f.delete();
        }
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;

    }
}
