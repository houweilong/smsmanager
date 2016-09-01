package com.njust.smsmanager.utils;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.ImageView;

import com.njust.smsmanager.App;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class Utils {
    /**
     * dip转换px
     */
    public static int dip2px(int dip) {
        final float scale = App.getResource().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }

    /**
     * pxz转换dip
     */

    public static int px2dip(int px) {
        final float scale = App.getResource().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);

    }

    public static String getPhoneNumByThreadId(final String thread_id)
    {
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                String recipientId = null;
                String address = null;
                Cursor c = App.getResolver().query(Uri.parse("content://mms-sms/conversations?simple=true"),
                        new String[]{"recipient_ids"}, "_id = " + thread_id, null, null);
                if(null != c && c.moveToNext())
                {
                    recipientId = c.getString(0).split(" ")[0];
                    c.close();
                }
                c = App.getResolver().query(Uri.parse("content://mms-sms/canonical-addresses"),
                        null, "_id = " + recipientId, null, null);
                if(null != c && c.moveToNext())
                {
                    address = c.getString(1);
                    c.close();
                }
                return address;
            }
        };
        Future<String> future = App.getExecutor().submit(callable);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 根据电话号码查询联系人的姓名，若无此人，则返回null
     * @param number
     * @return
     */
    public static String getNameByNum(String number)
    {
        Uri uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI;
        final Uri uri1 = Uri.withAppendedPath(uri,number);
        ExecutorService executor = App.getExecutor();
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                Cursor cursor = App.getResolver().query(uri1,new String[]{"display_name"},
                        null,null,null);
                if(null == cursor || cursor.getCount() == 0)
                {
                    if(cursor != null)
                    {
                        cursor.close();
                    }
                    return null;
                }
                //让cursor指向第一行
                cursor.moveToFirst();
                String name = cursor.getString(0);
                cursor.close();
                return name;
            }
        };

        Future<String> future = executor.submit(callable);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 根据电话号码查询联系人的ID，若无此人，返回null
     * @param number
     * @return
     */
    public static String getIDByNum(String number)
    {
        Uri uri = ContactsContract.PhoneLookup.CONTENT_FILTER_URI;
        final Uri uri1 = Uri.withAppendedPath(uri,number);
        ExecutorService executor = App.getExecutor();
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                Cursor cursor = App.getResolver().query(uri1,new String[]{"_id"},null,null,null);
                if(null == cursor || cursor.getCount() == 0)
                {
                    if(cursor != null)
                    {
                        cursor.close();
                    }
                    return null;
                }
                //让cursor指向第一行
                cursor.moveToFirst();
                String num = cursor.getString(0);
                cursor.close();
                return num;
            }
        };
        Future<String> future = executor.submit(callable);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *  查询ContactsContract.Contacts.CONTENT_URI + 加上上面得到id,
     *  构建好Uri之后调用ContactsContract.Contacts.openContactPhotoInputStream得到图片的流
     * @param id 联系人的id
     * @return bitmap 对象或者是null
     */
    public static Bitmap getBitmapById(String id)
    {
        //将id值追加至要查询的URI
        final Uri contractUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI,id);
        //查询联系人，获取联系人头像的输入流
        //如果没有头像，返回输入流为null
        ExecutorService executor = App.getExecutor();
        Callable<Bitmap> callable = new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(
                        App.getResolver(),contractUri);
                if(null == inputStream)
                {
                    return null;
                }
                return BitmapFactory.decodeStream(inputStream);
            }
        };
        Future<Bitmap> future = executor.submit(callable);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据电话号码得到会话id
     * @param address
     * @return
     */
    private static String getThreadIdByAddress(final String address)
    {

        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                String recipientId = null;
                String thread_id = null;
                Cursor c = App.getResolver().query(Uri.parse("content://mms-sms/canonical-addresses"),
                        null, "address = " + address, null, null);
                if(null != c && c.moveToNext())
                {
                    recipientId = c.getString(0);
                    c.close();
                }
                c = App.getResolver().query(Uri.parse("content://mms-sms/conversations?simple=true"),
                        new String[]{"_id"}, "recipient_ids = " + recipientId, null, null);
                if(null != c && c.moveToNext())
                {
                    thread_id = c.getString(0);
                    c.close();
                }
                return thread_id;
            }
        };
        Future<String> future = App.getExecutor().submit(callable);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据会话的ID删除会话内容
     * @param threadId
     * @return
     */
    public static int deleteThreadById(final String threadId)
    {
        ExecutorService executor = App.getExecutor();
        Callable<Integer> callable = new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return App.getResolver().delete(Constants.URI_SMS,"thread_id = "+threadId,null);
            }
        };
        Future<Integer> future = executor.submit(callable);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 发送短信
     * @param context
     * @param address
     * @param msg
     */
    public static void sendMessage(Context context,String address,String msg)
    {
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> divideMessage = smsManager.divideMessage(msg);

        Intent intent = new Intent("com.njust.smsmanager.receiver.SendSucessReceiver");
        intent.putExtra("address",address);
        intent.putExtra("thread_id",getThreadIdByAddress(address));
        PendingIntent sentIntent = PendingIntent.getBroadcast(context,111,intent,PendingIntent.FLAG_ONE_SHOT);

        for(String message : divideMessage)
        {
            smsManager.sendTextMessage(address, //要发送短信的目标地址
                    null,//短信服务中心的号码
                    message,//要发送的内容
                    sentIntent,//发送成功时的隐士意图
                    null);//接收成功后的意图
        }
        writeMessageToDb(address,msg);
    }

    /**
     * 向管理短信的表sms中写入数据
     * @param address
     * @param msg
     */
    private static void writeMessageToDb(String address,String msg)
    {
        final ContentValues values = new ContentValues();
        values.put("address",address);
        values.put("type",Constants.TYPE_SEND);
        values.put("body",msg);
        ExecutorService executor = App.getExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                App.getResolver().insert(Constants.URI_SMS,values);
            }
        };
        executor.submit(runnable);
    }

    public static Bitmap decodeBitmap(final Resources resources, final int bitmap_id)
    {
        ExecutorService executorService = App.getExecutor();
        Callable<Bitmap> callable = new Callable<Bitmap>() {
            @Override
            public Bitmap call() throws Exception {
                return BitmapFactory.decodeResource(resources,bitmap_id);
            }
        };
        Future<Bitmap> future = executorService.submit(callable);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

}
