package com.njust.smsmanager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.njust.smsmanager.utils.Constants;
import com.njust.smsmanager.utils.UiUtils;
import com.njust.smsmanager.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MessageDetailUI extends AppCompatActivity {
    @BindView(R.id.btn_edit) TextView btn_edit;
    @BindView(R.id.tl_custom) Toolbar toolbar;
    @BindView(R.id.iv_photo) ImageView iv_photo;
    @BindView(R.id.tv_address) TextView tv_address;
    @BindView(R.id.tv_date) TextView tv_date;
    @BindView(R.id.tv_name) TextView tv_name;
    @BindView(R.id.tv_content) TextView tv_content;

    private Unbinder unbinder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_detail_ui);

        unbinder = ButterKnife.bind(this);
        initToolBar();

        Intent intent = getIntent();
        String address = intent.getStringExtra("address");
        String body  = intent.getStringExtra("body");
        long date = Long.parseLong(intent.getStringExtra("date"));
        int type = Integer.parseInt(intent.getStringExtra("type"));
        //显示头像  获取联系人头像的ID
        //显示头像  获取联系人头像的ID
        String id = Utils.getIDByNum(address);
        UiUtils.setPhotoBitmap(iv_photo,id);

        String dateString;
        if(DateUtils.isToday(date))
        {
            dateString = DateFormat.getTimeFormat(this).format(date);
        }
        else
        {
            dateString = DateFormat.getDateFormat(this).format(date);
        }
        if(type == Constants.TYPE_SEND)
        {
            dateString = "发送于:"+dateString;
        }
        else if(type == Constants.TYPE_RECEIVE)
        {
            dateString = "接收于:"+dateString;
        }
        else if(type == Constants.TYPE_DRAFT)
        {
            dateString = "存储于:"+dateString;
        }
        tv_address.setText(address);
        tv_date.setText(dateString);
        tv_name.setText(Utils.getNameByNum(address));
        tv_content.setText(body);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initToolBar() {
        btn_edit.setVisibility(View.GONE);
        toolbar.setNavigationIcon(R.drawable.ic_navigate_before);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(null != actionBar) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        App.getRefWatcher().watch(this);
    }
}
