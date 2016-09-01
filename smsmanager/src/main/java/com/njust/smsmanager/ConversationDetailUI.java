package com.njust.smsmanager;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.njust.smsmanager.utils.CommonQueryHandler;
import com.njust.smsmanager.utils.Constants;
import com.njust.smsmanager.utils.UiUtils;
import com.njust.smsmanager.utils.Utils;

import java.util.Date;
import java.util.HashSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * 会话详情页面
 */
public class ConversationDetailUI extends AppCompatActivity implements View.OnClickListener,
        CommonQueryHandler.OnCursorChangedListener{
    @BindView(R.id.tl_custom) Toolbar toolbar;
    @BindView(R.id.toolbar_title) TextView title;
    @BindView(R.id.btn_edit) TextView btn_edit;
    @BindView(R.id.lv_conversation_detail) ListView listView;
    @BindView(R.id.et_msg_conversation_detail) EditText inputMsg;
    @BindView(R.id.btn_send_new_message) TextView sendMsg;
    private String thread_id;
    private String address;
    private MessageDetailAdapter adapter;
    private Unbinder unbinder;
    /**
     * 要查询的字段
     */
    private InputMethodManager imm;

    private final String[] projection = new String[]{
            "_id","date","type","body"
    };
    private static final int INDEX_DATE = 1;
    private static final int INDEX_TYPE = 2;
    private static final int INDEX_BODY = 3;
    /**
     * 存放应该显示日期标题的条目的位置
     */
    private HashSet<Integer> showTitleSet = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置页面无标题
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_conversation_detail_ui);
        unbinder = ButterKnife.bind(this);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        initToolBar();

        thread_id = getIntent().getStringExtra("thread_id");
        address = getIntent().getStringExtra("address");

        inputMsg.requestFocus();


        //如果没有thread_id就抛异常
        if(null == thread_id)
        {
            throw new RuntimeException("你没有给我对话的序号");
        }
        
        init();

        initListView();

        prepareData();
    }

    private void initListView() {
        //设置条目的分割线为null
        listView.setDivider(null);
        listView.setDividerHeight(Utils.dip2px(10));
        View view = new View(this);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                Utils.dip2px(1));
        view.setLayoutParams(params);
        listView.addHeaderView(view);
        listView.addFooterView(view);
        adapter = new MessageDetailAdapter(this,null);
        listView.setAdapter(adapter);
        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
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
    private void prepareData() {
        CommonQueryHandler queryHandler = new CommonQueryHandler(getContentResolver());

        //给queryHandler添加事件监听
        queryHandler.setOnCursorChangedListener(this);
        queryHandler.startQuery(10,adapter, Constants.URI_SMS,projection,
                "thread_id = ?",new String[]{thread_id},"date");
    }

    public void onCursorChanged(int token, Object cookie, Cursor cursor) {
        listView.setSelection(adapter.getCount()-1);
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

    private void init() {
        //设置标题
        String name = Utils.getNameByNum(address);
        if(null != name)
        {
            title.setText(name);
        }
        else
        {
            title.setText(address);
        }
    }

    @Override
    @OnClick(R.id.btn_send_new_message)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_send_new_message:
                String msg = inputMsg.getText().toString();
                if(TextUtils.isEmpty(msg)){
                    Toast.makeText(this, "请输入要发送的内容", Toast.LENGTH_SHORT).show();
                    return;
                }

                //开始发送短信
                Utils.sendMessage(this,address,msg);
                inputMsg.setText("");
                //隐藏输入法
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                break;
        }
    }

    static class ViewHolder
    {
        @BindView(R.id.date_title_conversation_list_item) TextView dateTitle;

        @BindView(R.id.tl_receive) TableLayout tl_receive;
        @BindView(R.id.tv_msg_receive) TextView msg_receice;
        @BindView(R.id.tv_date_receive) TextView date_receive;

        @BindView(R.id.tl_send) TableLayout tl_send;
        @BindView(R.id.tv_msg_send) TextView msg_send;
        @BindView(R.id.tv_date_send) TextView date_send;

        public ViewHolder(View view)
        {
            ButterKnife.bind(this,view);
        }
    }


    private class MessageDetailAdapter extends CursorAdapter{
        public MessageDetailAdapter(Context context, Cursor c) {
            super(context, c, true);
        }

        @Override
        protected void onContentChanged() {
            super.onContentChanged();
            //super中的内容执行完毕后，数据才刷新
            listView.setSelection(listView.getCount()-1);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            View view = View.inflate(context, R.layout.list_item_conversation_detail, null);
            ViewHolder vh = new ViewHolder(view);
            view.setTag(vh);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();

            long when = cursor.getLong(INDEX_DATE);
            String msg = cursor.getString(INDEX_BODY);
            int type = cursor.getInt(INDEX_TYPE);
            String dateString = DateFormat.getTimeFormat(context).format(when);
            if(type == Constants.TYPE_RECEIVE)
            {
                //隐藏发送的TableLayout
                holder.tl_send.setVisibility(View.GONE);
                //显示并设置接收的TableLayout和其中View的内容
                holder.tl_receive.setVisibility(View.VISIBLE);
                holder.date_receive.setText(dateString);
                holder.msg_receice.setText(msg);
            }
            else if(type == Constants.TYPE_SEND)
            {
                //发送的消息
                holder.tl_receive.setVisibility(View.GONE);
                holder.tl_send.setVisibility(View.VISIBLE);
                holder.date_send.setText(dateString);
                holder.msg_send.setText(msg);
            }
            else
            {
                //草稿
                holder.tl_send.setVisibility(View.GONE);
                holder.tl_receive.setVisibility(View.GONE);
                inputMsg.setText(msg);
            }
            //TODO 对日期标题进行设置

            //判断当前的位置是否在集合里面，如果在，显示标题，否则就隐藏
            if(showTitleSet.contains(cursor.getPosition())){
                //显示
                holder.dateTitle.setVisibility(View.VISIBLE);
                holder.dateTitle.setText(DateFormat.getDateFormat(context).format(new Date(when)));
            }else{
                holder.dateTitle.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        App.getRefWatcher().watch(this);
    }
}
