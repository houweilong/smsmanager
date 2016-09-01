package com.njust.smsmanager.utils;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.CursorAdapter;

public class CommonQueryHandler extends AsyncQueryHandler {

    public CommonQueryHandler(ContentResolver cr) {
        super(cr);
    }

    /**
     * 定义curson改变时的事件监听
     */
    public interface OnCursorChangedListener
    {
        void onCursorChanged(int token,Object cookie,Cursor cursor);
    }

    public void setOnCursorChangedListener(OnCursorChangedListener onCursorChangedListener) {
        this.onCursorChangedListener = onCursorChangedListener;
    }

    private OnCursorChangedListener onCursorChangedListener;


    /**
     * 查询完成后回调的方法
     * @param token 就是startQuery方法中的第一个方法 传来的整数值
     * @param cookie 就是startQuery方法的第二个参数传进来的对象
     * @param cursor
     */
    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if(null != cookie && cookie instanceof CursorAdapter)
        {
            CursorAdapter adapter = (CursorAdapter) cookie;
            //将查询返回的cursor设置给adapter
            adapter.changeCursor(cursor);
        }
        if(null != onCursorChangedListener)
        {
            //触发监听事件
            onCursorChangedListener.onCursorChanged(token,cookie,cursor);
        }
    }

}
