package com.njust.smsmanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.njust.smsmanager.ConversationDetailUI;

public class SendSucessReceiver extends BroadcastReceiver {
    public SendSucessReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //短信发送成功后得到调用
        String address = intent.getStringExtra("address");
        String thread_id = intent.getStringExtra("thread_id");
        Toast.makeText(context,"短信发送成功了"+address,Toast.LENGTH_SHORT).show();
        //打开会话详情的Activity
        //获得对应的电话号码
        Intent intent_detail = new Intent(context, ConversationDetailUI.class);
        intent_detail.putExtra("address",address);
        intent_detail.putExtra("thread_id",thread_id);
        intent_detail.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent_detail);
    }
}
