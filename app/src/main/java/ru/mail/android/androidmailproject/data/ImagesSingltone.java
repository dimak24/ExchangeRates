package ru.mail.android.androidmailproject.data;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.LruCache;
import android.widget.ImageView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import ru.mail.android.androidmailproject.R;
import ru.mail.android.androidmailproject.activities.startActivity.StartActivity;
import ru.mail.android.androidmailproject.auxiliary.ImageManager;
import ru.mail.android.androidmailproject.auxiliary.LoadImageTask;

public class ImagesSingltone {
    private static ImagesSingltone instance;
    private static LruCache<String, Bitmap> memoryCache;
    private Context context;

    public void setContext(Context context) {
        this.context = context;
    }

    ImagesSingltone() {
        if (memoryCache == null) {
            final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            final int cacheSize = maxMemory / 8;
            memoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
                }
            };
        }
    }


    public static ImagesSingltone getInstance() {
        synchronized (ImagesSingltone.class) {
            if (instance == null) {
                instance = new ImagesSingltone();
            }
            return instance;
        }
    }


    public Bitmap getBitmapFromMemCache(int position) {
        String name = CurrenciesSingletone.getInstance().getCurrenciesNames(false)[position];
        return memoryCache.get(name);
    }

    public void addBitmapToMemoryCache(int position, Bitmap bitmap) {
        String name = CurrenciesSingletone.getInstance().getCurrenciesNames(false)[position];

        if (bitmap != null)
            memoryCache.put(name, ImageManager.addBorder(ImageManager.makeTransparentBackground(
                    ImageManager.makeTransparentBackground(bitmap))));
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void loadImage(Context context, int position, ImageView iv) {
        final Bitmap bm = getBitmapFromMemCache(position);
        if (null != bm) {
            cancelDownload(position, iv);
            iv.setImageBitmap(bm);
        } else {
            LoadImageTask lt = new LoadImageTask(context, iv, position);
            lt.execute();
        }
    }

    private static class DownloadDrawable extends ColorDrawable {
        private final WeakReference<LoadImageTask> loadTaskWeak;

        private DownloadDrawable(LoadImageTask loadTask) {
            super(Color.WHITE);
            loadTaskWeak = new WeakReference<>(loadTask);
        }

        public LoadImageTask getTask() {
            return loadTaskWeak.get();
        }
    }

    private static void cancelDownload(int key, ImageView imageView) {
        LoadImageTask task = getBitmapDownloaderTask(imageView);
        if (null != task) {
            String bitKey = task.getName();
            if ((bitKey == null) || (!bitKey.equals(key))) {
                task.cancel(true);
            }
        }
    }

    private static LoadImageTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DownloadDrawable) {
                DownloadDrawable dd = (DownloadDrawable)drawable;
                return dd.getTask();
            }
        }
        return null;
    }

}
