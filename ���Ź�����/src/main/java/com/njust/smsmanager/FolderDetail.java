package com.njust.smsmanager;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.njust.smsmanager.utils.CommonQueryHandler;
import com.njust.smsmanager.utils.Constants;
import com.njust.smsmanager.utils.UiUtils;
import com.njust.smsmanager.utils.Utils;

import java.util.Date;
import java.util.HashSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.Unbinder;

public class FolderDetail extends AppCompatActivity implements CommonQueryHandler.OnCursorChangedListener,
        AdapterView.OnItemClickListener{

    private int position;
    @BindView(R.id.lv_folder_detail) ListView listView;
    @BindView(R.id.toolbar_title) TextView toolbar_title;
    @BindView(R.id.btn_edit) TextView btn_edit;
    private FolderDetailAdapter adapter;
    private Unbinder unbinder;
    /**
     * 存放应该显示日期标题的条目的位置
     */
    private HashSet<Integer> showTitleSet;
    /**
     * 会话列表所需要的字段
     */
    private final String[] projection= new String[]{
            "sms.body AS snippet",
            "sms.thread_id AS _id",
            "address as address",
            "date as date",
            "type as type"
    };
    /**
     * 用于保存，选中的会话的ID
     */
    private static final int INDEX_BODY=0;
    private static final int INDEX_THREAD_ID=1;
    private static final int INDEX_TYPE = 4;
    private static final int INDEX_ADDRESS=2;
    private static final int INDEX_DATE=3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_detail);
        unbinder = ButterKnife.bind(this);
        btn_edit.setVisibility(View.GONE);
        showTitleSet = new HashSet<>();


        position = getIntent().getIntExtra("position", -1);

        if(position == -1){
            throw new RuntimeException("position == -1 我不知道要显示啥了");
        }
        adapter = new FolderDetailAdapter(this,null);
        listView.setAdapter(adapter);

        prepareData();
    }
    /**
     * 根据下标获得URI
     * @param position
     * @return
     */
    private Uri getUri(int position) {
        switch (position) {
            case 0:
                toolbar_title.setText("收件箱");
                return Constants.URI_INBOX;
            case 1:
                toolbar_title.setText("发件箱");
                return Constants.URI_OUTBOX;
            case 2:
                toolbar_title.setText("草稿箱");
                return Constants.URI_DRAFT;
            case 3:
                toolbar_title.setText("已发送");
                return Constants.URI_SEND;
        }
        return null;
    }
    private void prepareData() {
        CommonQueryHandler queryHandler = new CommonQueryHandler(getContentResolver());
        queryHandler.setOnCursorChangedListener(this);
        queryHandler.startQuery(99, adapter, getUri(position), projection, null, null, "date desc");
    }

    @Override
    public void onCursorChanged(int token, Object cookie, Cursor cursor) {
        long lastDay = 0;
        //清空集合
        showTitleSet.clear();
        //移动到最前面，防止可能出现的BUG
        cursor.moveToPosition(-1);

        while(cursor.moveToNext()){
            long toDay = cursor.getLong(INDEX_DATE);
            if(!UiUtils.isSameDay(lastDay,toDay)){
                showTitleSet.add(cursor.getPosition());
            }
            lastDay = toDay;
        }
        adapter.notifyDataSetChanged();
    }
    public static class ViewHolder{
        @BindView(R.id.iv_conversation_list_item) ImageView image;
        @BindView(R.id.date_title_folder_list_item) TextView dateTitle;
        @BindView(R.id.tv_conversation_list_item_adress) TextView address;
        @BindView(R.id.tv_conversation_list_item_msg) TextView msg;
        @BindView(R.id.tv_conversation_list_item_date) TextView date;

        public ViewHolder(View view)
        {
            ButterKnife.bind(this,view);
        }

    }
    private class FolderDetailAdapter extends CursorAdapter{

        public FolderDetailAdapter(Context context, Cursor c) {
            super(context, c, true);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            View view = View.inflate(context, R.layout.folder_detail_list_item, null);
            view.setTag(new ViewHolder(view));
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder vh = (ViewHolder) view.getTag();

            //设置短信内容
            vh.msg.setText(cursor.getString(INDEX_BODY));

            //设置日期
            long when = cursor.getLong(INDEX_DATE);
            vh.date.setText(DateFormat.getTimeFormat(context).format(when));

            //设置 联系人电话，或是姓名，
            //获得手机号码
            String address = cursor.getString(INDEX_ADDRESS);
            UiUtils.setPhoneNumber(vh.address,address,cursor.getString(INDEX_THREAD_ID));

            //显示头像
            //获得联系人的ID
            //显示头像  获取联系人头像的ID
            String id = Utils.getIDByNum(address);
            UiUtils.setPhotoBitmap(vh.image,id);

            //判断当前的位置是否在集合里面，如果在，显示标题，否则就隐藏
            if(showTitleSet.contains(cursor.getPosition())){
                //显示
                vh.dateTitle.setVisibility(View.VISIBLE);
                vh.dateTitle.setText(DateFormat.getDateFormat(context).format(new Date(when)));
            }else{
                vh.dateTitle.setVisibility(View.GONE);
            }
        }
    }

    @Override
    @OnItemClick(R.id.lv_folder_detail)
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Cursor cursor = adapter.getCursor();
        cursor.moveToPosition(position);
        Intent intent = new Intent(this,MessageDetailUI.class);
        String address = cursor.getString(INDEX_ADDRESS);
        if(null == address)
        {
            address = Utils.getPhoneNumByThreadId(cursor.getString(INDEX_THREAD_ID));
        }
        intent.putExtra("address",address);
        intent.putExtra("body",cursor.getString(INDEX_BODY));
        intent.putExtra("date",cursor.getString(INDEX_DATE));
        intent.putExtra("type",cursor.getString(INDEX_TYPE));
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        App.getRefWatcher().watch(this);
    }
}
