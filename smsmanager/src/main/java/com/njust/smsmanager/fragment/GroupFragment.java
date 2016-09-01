package com.njust.smsmanager.fragment;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.njust.smsmanager.App;
import com.njust.smsmanager.R;
import com.njust.smsmanager.dbUtils.Groups;
import com.njust.smsmanager.dbUtils.ThreadGroups;
import com.njust.smsmanager.utils.Utils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import butterknife.Unbinder;

public class GroupFragment extends Fragment implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener
{
    @BindView(R.id.lv_group) ListView listView;
    private Context context;
    private List<Groups> groupsList;

    private GroupListAdapter adapter;
    public GroupFragment() {
    }

    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);
        unbinder = ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        groupsList = App.getGroupManager().getAllGroups();
        context = getContext();
        adapter = new GroupListAdapter();
        listView.setAdapter(adapter);
    }


    public void updateDatas()
    {
        groupsList = App.getGroupManager().getAllGroups();
        adapter.notifyDataSetChanged();
    }

    /**
     * 相应listview条目点击事件
     * @param adapterView
     * @param view
     * @param position
     * @param l
     */
    @Override
    @OnItemClick(R.id.lv_group)
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        //根据POSTION获得群组ID
        Groups groups = (Groups) adapter.getItem(position);
        long groupId = groups.getId();

        //根据群组ID查询群组会话对应关系表，找到相应的会话ID
        List<ThreadGroups> threadGroupses = App.getThreadGroupManager().getThreadIdByGroupId(groupId);

        //说明该群组没有相关的会话,弹出toast，返回
        if(threadGroupses.size() == 0){
            Toast.makeText(context, "该群还是空的，添加点人气吧", Toast.LENGTH_SHORT).show();
            return ;
        }

        //目标：根据threadIdCurosr中的内容，拼凑出类于 thread_id in (8,12) 的字符串
        String destStr = converCursor2MyStr(threadGroupses);
        ViewPager pager = (ViewPager) getActivity().findViewById(R.id.pager);
        pager.setTag(destStr);
        //跳转至会话详情页面
        pager.setCurrentItem(0,false);
    }

    /**
     * 根据cursor 中的threadId 拼添我们想要的字符串 :   thread_id in (8,12)
     * @param threadGroupsList
     * @return
     */
    private String converCursor2MyStr(List<ThreadGroups> threadGroupsList) {
        //带着会话ID，跳转至ConversationUI页面上去显示。
        StringBuilder sb = new StringBuilder("thread_id in (");
        for(ThreadGroups threadGroups : threadGroupsList)
        {
            long threadId = threadGroups.getThread_id();
            sb.append(threadId);  //  thread_id in (8
            sb.append(",");  // thread_id in (8,
        }
        // thread_id in (8,12,11,
        //将最后一个逗号，替换为)
        sb.replace(sb.lastIndexOf(","), sb.length(), ")");

        return sb.toString();
    }

    @Override
    @OnItemLongClick(R.id.lv_group)
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
        showEditGroupDialog(position);
        return true;
    }

    /**
     * 显示编辑群组对话框
     * @param position
     */
    private void showEditGroupDialog(final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setItems(new String[]{"编辑","删除"}, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0)
                {
                    //点击的是编辑
                    showUpdateGroupDialog(position);
                }
                else
                {
                    //点击的是删除
                    showConfirmDeleteDialog(position);
                }

                //关闭对话框
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    /**
     * 显示确认删除对话框
     * @param position
     */
    private void showConfirmDeleteDialog(final int position) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("确认删除");
        dialog.setMessage("您确定要删除该群组吗？");
        dialog.setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteGroup(position);
                dialogInterface.dismiss();
            }
        });
        //监听给空，会按默认操作，即关闭对话框
        dialog.setPositiveButton("取消",null);
        dialog.show();
    }

    private void deleteGroup(int position) {
        //根据点击item的位置，position获得group的id值
        Groups groups = (Groups) adapter.getItem(position);
        App.getGroupManager().deleteGroupById(groups.getId());
        updateDatas();
    }

    /**
     *
     * @param position
     */
    private void showUpdateGroupDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final AlertDialog dialog = builder.create();
        dialog.setTitle("更新群组名称");
        View view = View.inflate(context,R.layout.view_in_group_dialog,null);
        final EditText input = (EditText) view.findViewById(R.id.et_new_group_dialog);
        Button btnOk = (Button) view.findViewById(R.id.btn_ok_new_group_dialog);
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel_new_group_dialog);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = input.getText().toString();
                if(TextUtils.isEmpty(name))
                {
                    Toast.makeText(context,"请输入群组名称",Toast.LENGTH_SHORT).show();
                    return;
                }
                //如果输入的有值，就列新群组名称
                updateGroupName(position,name);
                dialog.dismiss();
            }
        });
        btnCancel.setOnClickListener(new MyOnClickListener(dialog));
        dialog.setView(view, 0, 0, 0, 0);
        dialog.show();
    }

    /**
     * 更新指定位置上的群组名称
     * @param position
     * @param name
     */
    private void updateGroupName(int position, String name) {
        Groups groups = (Groups) adapter.getItem(position);
        App.getGroupManager().updateGroupNameById(groups.getId(),name);
        updateDatas();
    }

    static class ViewHolder
    {
        @BindView(R.id.tv_group_name) TextView groupName;

        public ViewHolder(View view)
        {
            ButterKnife.bind(this,view);
        }
    }

    private static class MyOnClickListener implements View.OnClickListener {
        private final AlertDialog dialog;

        public MyOnClickListener(AlertDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onClick(View view) {
            dialog.dismiss();
        }
    }

    private class GroupListAdapter extends BaseAdapter
    {
        @Override
        public int getCount() {
            return groupsList.size();
        }

        @Override
        public Object getItem(int i) {
            return groupsList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertview, ViewGroup viewGroup) {
            View view;
            ViewHolder holder;
            if(null == convertview)
            {
                view = View.inflate(context,R.layout.list_item_group,null);
                holder = new ViewHolder(view);
                view.setTag(holder);
            }
            else
            {
                view = convertview;
                holder = (ViewHolder) view.getTag();
            }
            holder.groupName.setText(groupsList.get(i).getName());
            holder.groupName.setCompoundDrawablePadding(Utils.dip2px(20));
            return view;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        App.getRefWatcher().watch(this);
    }
}
