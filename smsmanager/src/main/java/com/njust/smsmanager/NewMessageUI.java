package com.njust.smsmanager;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.njust.smsmanager.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class NewMessageUI extends AppCompatActivity implements View.OnClickListener{
    @BindView(R.id.actv_new_message) AutoCompleteTextView actv;
    @BindView(R.id.iv_to_contact_new_message) ImageView ivGoContact;
    @BindView(R.id.et_msg_conversation_detail) EditText etContent;
    @BindView(R.id.btn_send_new_message) TextView btnSend;
    @BindView(R.id.btn_cancel_new_message) TextView btn_cancel_new_message;
    private Unbinder unbinder;
    private final String[] projection= new String[]{

            "_id","data1","display_name"
    };
    private static final int INDEX_DATA1 = 1;
    private static final int INDEX_DISPLAY_NAME = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message_ui);
        unbinder = ButterKnife.bind(this);

        ActvAdapter adapter = new ActvAdapter(this, null);
        actv.setAdapter(adapter);

        // FilterQueryProvider 会监听actv中输入的内容,只要发生变化，就会实时收到最新的数据
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                if(constraint == null){
                    return null;
                }
                //根据constraint的内容，查询相关的联系人

                Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

                String selection = "data1 like '%"+constraint+"%'";

                return getContentResolver().query(uri, projection, selection, null, null);
            }
        });
    }

    @Override
    @OnClick({R.id.btn_send_new_message,R.id.iv_to_contact_new_message,R.id.btn_cancel_new_message})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send_new_message:
                //点击发送短信
                String content = etContent.getText().toString();
                etContent.setText("");
                if(TextUtils.isEmpty(content)){
                    Toast.makeText(this, "请输入内容",Toast.LENGTH_SHORT).show();
                    //etContent请求获得焦点
                    etContent.requestFocus();
                    return ;
                }

                String number = actv.getText().toString();
                if(TextUtils.isEmpty(number)){
                    Toast.makeText(this, "请输入收件人号码", Toast.LENGTH_SHORT).show();
                    actv.requestFocus();
                    return ;
                }

                Utils.sendMessage(this, number, content);

                break;
            case R.id.iv_to_contact_new_message:
                //点击跳转至联系人的图片
                Intent intent = new Intent();
                intent.setAction("android.intent.action.PICK");
                intent.setData(Uri.parse("content://com.android.contacts/contacts"));
                startActivityForResult(intent, 100);
                break;
            case R.id.btn_cancel_new_message:
                //取消新建消息，返回会话页面
                finish();
                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(null == data)
        {
            return;
        }
        if(100 == requestCode){
            Uri uri = data.getData();

            Cursor cursor = getContentResolver().query(uri, new String[]{"_id"}, null, null, null);
            if(null != cursor) {
                //返回的cursor 默认在 -1行,要获得数据，让cursor指向第一行
                cursor.moveToFirst();
                //获得联系人的id编号
                int id = cursor.getInt(0);
                cursor.close();
                //根据ID编号，查询联系人号码

                Uri uri2 = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                //where 条件
                String selection = "contact_id = " + id;

                Cursor cursor2 = getContentResolver().query(uri2, new String[]{"data1"}, selection, null, null);
                if(null != cursor2) {
                    //返回的cursor2 默认在 -1行,要获得数据，让cursor指向第一行
                    cursor2.moveToFirst();
                    //cursor2中只有一条内容
                    String address = cursor2.getString(0);
                    cursor2.close();
                    actv.setText(address);
                }
            }
        }
    }

    static class ViewHolder
    {
        @BindView(R.id.tv_name_list_item_new_message) TextView name;
        @BindView(R.id.tv_address_list_item_new_message) TextView address;

        public ViewHolder(View view)
        {
            ButterKnife.bind(this,view);
        }
    }

    static class ActvAdapter extends CursorAdapter {

        public ActvAdapter(Context context, Cursor c) {
            super(context,c,true);
        }

        @Override
        /**
         * 当点击actv 下拉的listView 中某一个条目时，返回的字符串
         */
        public CharSequence convertToString(Cursor cursor) {
            return cursor.getString(INDEX_DATA1);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view  =View.inflate(context, R.layout.list_item_new_message, null);
            view.setTag(new ViewHolder(view));
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.name.setText(cursor.getString(INDEX_DISPLAY_NAME));
            holder.address.setText(cursor.getString(INDEX_DATA1));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        App.getRefWatcher().watch(this);
    }
}
