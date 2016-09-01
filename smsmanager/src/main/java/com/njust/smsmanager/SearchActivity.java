package com.njust.smsmanager;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.njust.smsmanager.utils.CommonQueryHandler;
import com.njust.smsmanager.utils.Constants;
import com.njust.smsmanager.utils.UiUtils;
import com.njust.smsmanager.utils.Utils;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends ListActivity {
    private SearchListAdapter adapter;
    /**
     * 会话列表所需要的字段
     */
    private final String[] projection= new String[]{
            "sms.body AS snippet",
            "sms.thread_id AS _id",
            "address as address",
            "date as date"
    };
    private static final int INDEX_BODY=0;
    private static final int INDEX_THREAD_ID=1;
    private static final int INDEX_ADDRESS=2;
    private static final int INDEX_DATE=3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleIntent(getIntent());

    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent)
    {
        if(Intent.ACTION_SEARCH.equals(intent.getAction()))
        {
            String query = intent.getStringExtra(SearchManager.QUERY);
            ListView listView = getListView();
            adapter = new SearchListAdapter(this, null);
            listView.setAdapter(adapter);
            doMySerach(query);
        }
    }


    /**
     * 开始搜索
     * @param query
     */
    private void doMySerach(String query) {
        CommonQueryHandler queryHandler = new CommonQueryHandler(getContentResolver());


        String where = " body like '%"+query+"%'";

        queryHandler.startQuery(999, adapter, Constants.URI_SMS, projection, where, null, "date desc");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    static class ViewHolder{
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
    private static class SearchListAdapter extends CursorAdapter{
        public SearchListAdapter(Context context, Cursor c) {
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

            //给item设置 内容

            //设置短信内容
            vh.msg.setText(cursor.getString(INDEX_BODY));

            //设置日期
            long when = cursor.getLong(INDEX_DATE);
            UiUtils.setDate(context,vh.date,when);

            //设置 联系人电话，或是姓名，
            //获得手机号码
            String address = cursor.getString(INDEX_ADDRESS);
            String thread_id = cursor.getString(INDEX_THREAD_ID);
            UiUtils.setPhoneNumber(vh.address,address,thread_id);

            //显示头像
            //获得联系人的ID
            String id = Utils.getIDByNum(address);
            UiUtils.setPhotoBitmap(vh.image,id);

            //TODO 对日期标题进行设置
            vh.dateTitle.setText(DateFormat.getDateFormat(context).format(new Date(when)));

            //隐藏date标题
            vh.dateTitle.setVisibility(View.GONE);

        }

    }
}
