package com.njust.smsmanager.fragment;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.njust.smsmanager.App;
import com.njust.smsmanager.R;
import com.njust.smsmanager.utils.CommonQueryHandler;
import com.njust.smsmanager.utils.Constants;
import com.njust.smsmanager.FolderDetail;
import com.njust.smsmanager.utils.Utils;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;
import butterknife.Unbinder;

public class FolderFragment extends Fragment implements AdapterView.OnItemClickListener,
        CommonQueryHandler.OnCursorChangedListener
{

    private Context context;
    @BindView(R.id.folder_list) ListView listView;
    private Bitmap[] bitmaps;
    private final String[] descs = new String[]{"收件箱","发件箱","草稿箱","已发送"};
    //存储各个箱子里面短信的数目
    private ArrayMap<Integer,Integer> boxNum;
    private FolderListAdapter adapter;
    public FolderFragment() {

    }

    private Unbinder unbinder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folder, container, false);
        unbinder = ButterKnife.bind(this,view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getContext();
        bitmaps = new Bitmap[]{Utils.decodeBitmap(getResources(),R.drawable.a_f_inbox)
                ,Utils.decodeBitmap(getResources(),R.drawable.a_f_outbox),Utils.decodeBitmap(getResources(),R.drawable.a_f_draft)
                ,Utils.decodeBitmap(getResources(),R.drawable.a_f_sent)};
        boxNum = new ArrayMap<>();
        adapter = new FolderListAdapter();
        listView.setAdapter(adapter);

        prepareData();
    }

    /**
     * 根据下标获得Uri
     * @param i
     * @return
     */
    private Uri getUri(int i)
    {
        switch (i)
        {
            case 0:
                return Constants.URI_INBOX;
            case 1:
                return Constants.URI_OUTBOX;
            case 2:
                return Constants.URI_DRAFT;
            case 3:
                return Constants.URI_SEND;
        }
        return null;
    }


    private void prepareData() {
        CommonQueryHandler queryHandler = new CommonQueryHandler(context.getContentResolver());
        queryHandler.setOnCursorChangedListener(this);
        for(int i=0;i<bitmaps.length;i++){
            boxNum.put(i, 0);
            queryHandler.startQuery(i, null, getUri(i), new String[]{"count(*) as count"}, null, null, null);
        }
    }

    static class ViewHolder
    {
        @BindView(R.id.iv_icon_folder_list_item) ImageView icon;
        @BindView(R.id.tv_box_name_folder) TextView boxName;
        @BindView(R.id.tv_box_num_folder) TextView boxNum;

        public ViewHolder(View view)
        {
            ButterKnife.bind(this,view);
        }
    }
    private class FolderListAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return bitmaps.length;
        }

        @Override
        public Object getItem(int i) {
            return bitmaps[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertview, ViewGroup viewGroup) {
            ViewHolder holder;
            View view;
            if(null == convertview)
            {
                view = View.inflate(context,R.layout.list_item_folder,null);
                holder = new ViewHolder(view);
                view.setTag(holder);
            }
            else
            {
                view = convertview;
                holder = (ViewHolder) view.getTag();
            }
            holder.icon.setImageBitmap(bitmaps[i]);
            holder.boxName.setText(descs[i]);
            holder.boxNum.setText(String.valueOf(boxNum.get(i)));
            return view;
        }
    }

    @Override
    @OnItemClick(R.id.folder_list)
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        Intent intent = new Intent(context,FolderDetail.class);
        intent.putExtra("position", position);
        startActivity(intent);
    }

    @Override
    public void onCursorChanged(int token, Object cookie, Cursor cursor) {
        cursor.moveToFirst();
        boxNum.put(token, cursor.getInt(0));
        //刷新页面
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        App.getRefWatcher().watch(this);
    }
}
