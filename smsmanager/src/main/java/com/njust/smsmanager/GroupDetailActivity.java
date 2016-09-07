package com.njust.smsmanager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.njust.smsmanager.dbUtils.Groups;
import com.njust.smsmanager.utils.CommonQueryHandler;
import com.njust.smsmanager.utils.Constants;
import com.njust.smsmanager.utils.UiUtils;
import com.njust.smsmanager.utils.Utils;

import org.w3c.dom.Text;

import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import butterknife.Unbinder;

public class GroupDetailActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        View.OnClickListener,AdapterView.OnItemLongClickListener{
    //信息列表
    @BindView(R.id.lv_conversation) ListView listView;

    //显示删除进度的对话框
    private ProgressDialog proDialog;

    @BindView(R.id.btn_conver_select_all) Button btn_conver_select_all;
    @BindView(R.id.btn_conver_select_none) Button btn_conver_select_none;
    @BindView(R.id.btn_conver_delete_msg) Button btn_conver_delete_msg;

    @BindView(R.id.btn_edit) TextView btn_edit;
    @BindView(R.id.toolbar_title) TextView toolbar_title;

    private Unbinder unbinder;
    private String groupName;

    /**
     * 会话列表所需要的字段
     */
    private final String[] projection = new String[]
            {
                    "sms.body AS snippet",
                    "sms.thread_id AS _id",
                    "groups.msg_count AS msg_count",
                    "address as address",
                    "date as date"
            };
    /**
     * 用于保存，选中的会话的ID
     */
    private HashSet<String> selectedSet;

    private static final int INDEX_BODY=0;
    private static final int INDEX_THREAD_ID=1;
    private static final int INDEX_MSG_COUNT=2;
    private static final int INDEX_ADDRESS=3;
    private static final int INDEX_DATE=4;

    /**
     * listView 的adapter
     */
    private MyListAdapter adapter;
    /**
     * 当前Fragment上下文的引用
     */
    private Context context;
    /**
     * 用于判断 当前的状态
     * true 为编辑状态
     * false 不是编辑状态
     */
    private boolean isEditState = false;
    private boolean isCancelDeleteMsg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);
        unbinder = ButterKnife.bind(this);

        context = this;

        long groupId = getIntent().getLongExtra("groupId",-1);
        groupName = App.getGroupManager().getGroupNameById(groupId);
        toolbar_title.setText(groupName);

        selectedSet = new HashSet<>();
        adapter = new MyListAdapter(context,null);
        listView.setAdapter(adapter);
        prepareData();

    }

    static class ViewHolder{
        @BindView(R.id.iv_conversation_list_item)
        ImageView image;
        @BindView(R.id.iv_checkbox_list_item) ImageView checkbox;
        @BindView(R.id.tv_conversation_list_item_adress) TextView address;
        @BindView(R.id.tv_conversation_list_item_msg) TextView msg;
        @BindView(R.id.tv_conversation_list_item_date) TextView date;

        public ViewHolder(View view)
        {
            ButterKnife.bind(this,view);
        }
    }

    /**
     * 准备数据
     */
    private void prepareData()
    {
        /**
         * 通常做法，开子线程取数据，handler回传
         */
        CommonQueryHandler commonQueryHandler = new CommonQueryHandler(context.getContentResolver());
        String where = getIntent().getStringExtra("where");
        //开始查询
        commonQueryHandler.startQuery(10,adapter, Constants.URI_CONVERSATION,projection,
                where,null,"date desc");
    }


    private static class MyOnClickListener implements DialogInterface.OnClickListener {
        private final long[] groupIds;
        private final int threadId;

        public MyOnClickListener(long[] groupIds, int threadId) {
            this.groupIds = groupIds;
            this.threadId = threadId;
        }

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            //选择群组的ID值为
            long selectGroupId = groupIds[i];
            //将会话ID与对应的群组ID，添加至会话群组关系表
            App.getThreadGroupManager().addThreadGroupValue(threadId,selectGroupId);
            dialogInterface.dismiss();
        }
    }

    private class MyListAdapter extends CursorAdapter {
        public MyListAdapter(Context context, Cursor c) {
            super(context, c, true);
        }

        /**
         * 创建并返回新的ItemView
         * @param context
         * @param cursor
         * @param viewGroup
         * @return
         */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            View view = View.inflate(context,R.layout.conversation_list_item,null);
            view.setTag(new ViewHolder(view));
            return view;
        }

        /**
         * 给ItemView绑定数据
         * @param view
         * @param context
         * @param cursor
         */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            //给短信设置内容
            holder.msg.setText(cursor.getString(INDEX_BODY));

            //设置日期
            long when = cursor.getLong(INDEX_DATE);
            UiUtils.setDate(context,holder.date,when);

            //设置联系人电话，或者是姓名
            //获得手机号码
            String address = cursor.getString(INDEX_ADDRESS);
            String thread_id = cursor.getString(INDEX_THREAD_ID);
            UiUtils.setPhoneNumber(holder.address,address,thread_id);

            //显示头像  获取联系人头像的ID
            String id = Utils.getIDByNum(address);
            UiUtils.setPhotoBitmap(holder.image,id);

            //对checkbox进行设置
            //1.是否是编辑状态，来决定是否显示
            //2.是否选中状态
            if(isEditState)
            {
                holder.checkbox.setVisibility(View.VISIBLE);
                holder.checkbox.setEnabled(selectedSet.contains(thread_id));
            }
            else
            {
                holder.checkbox.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 根据当前的状态刷新界面
     */
    private void flushState() {
        if(isEditState)
        {
            btn_edit.setText("取消");
            btn_conver_select_all.setVisibility(View.VISIBLE);
            btn_conver_select_none.setVisibility(View.VISIBLE);
            btn_conver_delete_msg.setVisibility(View.VISIBLE);
        }
        else
        {
            btn_edit.setText("编辑");
            btn_conver_select_all.setVisibility(View.GONE);
            btn_conver_select_none.setVisibility(View.GONE);
            btn_conver_delete_msg.setVisibility(View.GONE);
        }
        btn_conver_select_none.setEnabled(selectedSet.size()>0);
        btn_conver_delete_msg.setEnabled(selectedSet.size()>0);
        btn_conver_select_all.setEnabled(selectedSet.size()<adapter.getCount());
    }

    @Override
    @OnClick({R.id.btn_conver_select_all,R.id.btn_conver_select_none,R.id.btn_conver_delete_msg,R.id.btn_edit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_edit:
                if(!isEditState)
                {
                    isEditState = true;
                    btn_edit.setText("取消");
                }
                else
                {
                    isEditState = false;
                    btn_edit.setText("编辑");
                }
                flushState();
                break;
            case R.id.btn_conver_select_all:
                //全选
                Cursor cursor = adapter.getCursor();
                //让cursor移动到第一行的上一行
                cursor.moveToPosition(-1);
                while(cursor.moveToNext())
                {
                    selectedSet.add(cursor.getString(INDEX_THREAD_ID));
                }
                flushState();
                adapter.notifyDataSetChanged();
                break;
            case R.id.btn_conver_select_none:
                //取消全选
                selectedSet.clear();
                flushState();
                adapter.notifyDataSetChanged();
                break;
            case R.id.btn_conver_delete_msg:
                //删除
                showConfirmDeleteDialog();
            default:
                break;
        }
    }

    /**
     * 确认删除对话框
     */
    private void showConfirmDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("确认");
        builder.setMessage("确认要删除选中的短信会话吗？");
        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                showDeleteProgressDialog();
                //开启子线程，执行删除的操作
                App.getExecutor().execute(new DeleteMessageRunnable());
            }
        });
        builder.setNegativeButton("取消",null);
        builder.show();
    }

    private void showDeleteProgressDialog() {
        proDialog = new ProgressDialog(context);
        proDialog.setTitle("正在删除");
        //设置进度框为进度条的样式
        proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //设置进度条的最大值
        proDialog.setMax(selectedSet.size());
        proDialog.setButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                isCancelDeleteMsg = true;
                proDialog.dismiss();
            }
        });
        //给进度条添加监听,当对话框关闭的时候触发
        proDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //设置编辑状态为false
                isEditState = false;
                flushState();
            }
        });
        proDialog.show();
    }

    //响应ListView条目点击事件
    @Override
    @OnItemClick(R.id.lv_conversation)
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Cursor cursor = adapter.getCursor();
        cursor.moveToPosition(i);
        String threadId = cursor.getString(INDEX_THREAD_ID);
        if(isEditState)
        {
            //编辑页面
            if(selectedSet.contains(threadId))
            {
                selectedSet.remove(threadId);
            }
            else
            {
                selectedSet.add(threadId);
            }
            flushState();
            adapter.notifyDataSetChanged();
        }
        else
        {
            //打开会话详情的Activity
            //获得对应的电话号码
            String address = cursor.getString(INDEX_ADDRESS);
            if(null == address)
            {
                address = Utils.getPhoneNumByThreadId(threadId);
            }
            Intent intent = new Intent(context, ConversationDetailUI.class);
            intent.putExtra("thread_id",threadId);
            intent.putExtra("address",address);
            startActivity(intent);
        }
    }

    //ListView 条目长按事件
    @Override
    @OnItemLongClick(R.id.lv_conversation)
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        Toast.makeText(this,"该会话已添加至"+groupName,Toast.LENGTH_SHORT).show();
        return true;
    }


    private class DeleteMessageRunnable implements Runnable {
        @Override
        public void run() {
            for(String threadId : selectedSet)
            {
                //判断是否取消
                if(isCancelDeleteMsg)
                {
                    break;
                }
                int deleteNum = Utils.deleteThreadById(threadId);
                if(1 == deleteNum)
                {
                    //通知进度条改变进度
                    proDialog.incrementProgressBy(1);
                }
            }
            isCancelDeleteMsg = false;
            //清空集合
            selectedSet.clear();
            //关闭对话框
            proDialog.dismiss();
            //刷新状态
            GroupDetailActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    flushState();
                }
            });

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        App.getRefWatcher().watch(this);
    }
}
