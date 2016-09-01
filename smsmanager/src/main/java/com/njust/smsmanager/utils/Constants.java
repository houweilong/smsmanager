package com.njust.smsmanager.utils;

import android.net.Uri;

public class Constants {

    /**
     * 不在联系人里的图片
     */
    public static final String UNKNOWBITMAP = "unknowbitmap";
    /**
     * 默认的图片
     */
    public static final String DEFAULTBITMAP = "defaultbitmap";

    /**
     * 群组对应的表名
     */
    public static final String TABLE_GROUPS = "groups";
    /**
     * 会话与群组对应关系的表名
     */
    public static final String TABLE_THREAD_GROUPS = "thread_groups";
    /**
     * 日志的TAG
     */
    public static final String TAG = "com.njust.smsmanager";
    /**
     * 查询会话的URI
     */
    public static final Uri URI_CONVERSATION = Uri.parse("content://sms/conversations");
    /**
     * 操作SMS表的URI
     */
    public static final Uri URI_SMS = Uri.parse("content://sms");
    /**
     * 接收短信的Type
     */
    public static final int TYPE_RECEIVE = 1;
    /**
     * 发送短信的Type
     */
    public static final int TYPE_SEND = 2;
    public static final int TYPE_DRAFT = 3;
    /**
     * 收件箱的URI
     */
    public static final Uri URI_INBOX = Uri.parse("content://sms/inbox");
    /**
     * 已发送的URI
     */
    public static final Uri URI_SEND = Uri.parse("content://sms/sent");
    /**
     * 草稿箱的URI
     */
    public static final Uri URI_DRAFT = Uri.parse("content://sms/draft");
    /**
     * 发件箱的URI
     */
    public static final Uri URI_OUTBOX = Uri.parse("content://sms/outbox");
}
