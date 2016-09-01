package com.njust.smsmanager.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.njust.smsmanager.App;

import java.lang.reflect.Field;
import java.util.Date;

public class UiUtils {
    /**
     * 判断 二个日期是否是同一天
     * @param lastDay
     * @param toDay
     * @return
     */
    public static boolean isSameDay(long lastDay, long toDay) {
        Time time = new Time();
        time.set(toDay);

        int thenYear = time.year;
        int thenMonth = time.month;
        int thenMonthDay = time.monthDay;

        time.set(lastDay);
        return (thenYear == time.year)
                && (thenMonth == time.month)
                && (thenMonthDay == time.monthDay);
    }

    public static void setPhotoBitmap(ImageView imageView, String id)
    {
        Bitmap bitmap;
        if(null == id)
        {
            //没有此联系人,设置默认头像
            bitmap = App.getBitmapFromMemCache(Constants.UNKNOWBITMAP);
            if(bitmap == null)
            {
                bitmap = App.getUnknowPhoto();
                App.addBitmapToMemoryCache(Constants.UNKNOWBITMAP,bitmap);
            }
        }
        else
        {
            //根据联系人ID查找联系人的头像
            bitmap = App.getBitmapFromMemCache(id);
            if(null == bitmap)
            {
                bitmap = Utils.getBitmapById(id);
                if(null == bitmap)
                {
                    //有此联系人，但是联系人未设置头像，显示默认头像
                    bitmap = App.getBitmapFromMemCache(Constants.DEFAULTBITMAP);
                    if(null == bitmap)
                    {
                        bitmap = App.getDefaultPhoto();
                        App.addBitmapToMemoryCache(Constants.DEFAULTBITMAP,bitmap);
                    }
                }
                else
                {
                    App.addBitmapToMemoryCache(id,bitmap);
                }

            }
        }
        imageView.setImageBitmap(bitmap);
    }

    public static void setDate(Context context,TextView textView, long when)
    {
        String date;
        if(DateUtils.isToday(when))
        {
            //如果是今天，就返回一个表示时间的字符串
            date = DateFormat.getTimeFormat(context).format(when);
        }
        else
        {
            date = DateFormat.getDateFormat(context).format(new Date(when));
        }
        textView.setText(date);
    }

    public static void setPhoneNumber(TextView textView,String address,String thread_id)
    {
        if(null == address)
        {
            address = Utils.getPhoneNumByThreadId(thread_id);
        }
        //根据号码查询联系人的姓名
        String name = Utils.getNameByNum(address);
        if(null == name)
        {
            //没有此联系人，显示号码
            textView.setText(address);
        }
        else
        {
            textView.setText(name);
        }
    }

    public static void fixInputMethodManagerLeak(Context destContext)
    {
        if(null == destContext)
        {
            return;
        }
        InputMethodManager imm = (InputMethodManager) destContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if(null == imm)
        {
            return;
        }
        String[] arr = {"mCurRootView", "mServedView", "mNextServedView"};
        Field f;
        Object obj_get;
        for(int i=0;i<arr.length;i++)
        {
            String param = arr[i];
            try {
                f = imm.getClass().getDeclaredField(param);
                if(!f.isAccessible())
                {
                    f.setAccessible(true);
                }
                obj_get = f.get(imm);
                if(null != obj_get && obj_get instanceof View)
                {
                    View v_get = (View) obj_get;
                    if(v_get.getContext() == destContext)
                    {
                        //被InputMethodManager持有的引用context是想要销毁的目标
                        f.set(imm,null); //置空，破坏掉path to gc节点
                    }
                    else
                    {
                        //不是想要目标销毁的，即为又进了另一层界面了，不要处理，避免影响原逻辑,也就不用继续for循环了
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
