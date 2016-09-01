package com.njust.smsmanager;

import android.app.Application;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.StrictMode;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.Toast;

import com.njust.smsmanager.dbUtils.DBManager;
import com.njust.smsmanager.utils.Constants;
import com.njust.smsmanager.utils.Utils;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application {
    private static ContentResolver resolver;
    private static ExecutorService executor;
    private static Resources resources;
    private static DBManager groupManager;
    private static DBManager threadGroupManager;
    private static RefWatcher mRefWatcher;
    private static LruCache<String,Bitmap> bitmapLruCache;

    @Override
    public void onCreate() {
        super.onCreate();
        mRefWatcher = LeakCanary.install(this);
        groupManager = DBManager.getInstance(this, Constants.TABLE_GROUPS);
        threadGroupManager = DBManager.getInstance(this,Constants.TABLE_THREAD_GROUPS);
        resolver = getContentResolver();
        executor = Executors.newCachedThreadPool();
        resources = getResources();
        if(!BuildConfig.DEBUG)
        {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectNetwork()
                    .penaltyLog()
                    .build());

            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .detectActivityLeaks()              //检测Activity泄露
                    .detectLeakedSqlLiteObjects()       //检测数据库对象泄露
                    .detectLeakedClosableObjects()      //检测Closable对象泄露
                    .penaltyLog()                       //在LogCat中打印
                    .build());
        }

        //图片缓存
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        bitmapLruCache = new BitmapLruCache(cacheSize);
    }

    public static ContentResolver getResolver()
    {
        return resolver;
    }

    public static Bitmap getDefaultPhoto()
    {
        return Utils.decodeBitmap(getResource(),R.drawable.ic_contact_picture);
    }

    public static Bitmap getUnknowPhoto()
    {
        return Utils.decodeBitmap(getResource(),R.drawable.ic_unknown_picture_contact);
    }

    public static ExecutorService getExecutor()
    {
        return executor;
    }

    public static Resources getResource()
    {
        return resources;
    }

    public static DBManager getGroupManager() {
        return groupManager;
    }

    public static DBManager getThreadGroupManager() {
        return threadGroupManager;
    }

    public static RefWatcher getRefWatcher()
    {
        return mRefWatcher;
    }


    private static class BitmapLruCache extends LruCache<String,Bitmap> {
        public BitmapLruCache(int cacheSize) {
            super(cacheSize);
        }

        @Override
        protected int sizeOf(String key, Bitmap value) {
            return value.getRowBytes()*value.getHeight()/1024;
        }
    }

    public static void addBitmapToMemoryCache(String key,Bitmap bitmap)
    {
        if(null == getBitmapFromMemCache(key))
        {
            bitmapLruCache.put(key,bitmap);
        }
    }

    public static Bitmap getBitmapFromMemCache(String key)
    {
        return bitmapLruCache.get(key);
    }
}
