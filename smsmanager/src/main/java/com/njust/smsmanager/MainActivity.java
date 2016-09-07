package com.njust.smsmanager;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.njust.smsmanager.fragment.ConversationFragment;
import com.njust.smsmanager.fragment.FolderFragment;
import com.njust.smsmanager.fragment.GroupFragment;
import com.njust.smsmanager.utils.UiUtils;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabClickListener;

import java.lang.ref.WeakReference;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MainActivity extends AppCompatActivity{
    @BindView(R.id.tl_custom) Toolbar toolbar;
    @BindView(R.id.myCoordinator) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.pager) ViewPager mViewPager;
    @BindArray(R.array.title_names) String[] title_names;
    @BindView(R.id.toolbar_title) TextView toolBar_title;
    @BindView(R.id.btn_edit) TextView btn_edit;
    private BottomBar mBottomBar;
    private Unbinder unbinder;
    //为true时表示当前显示新建信息按钮，否则显示添加分组按钮
    private boolean isAddSmsOrGroup = true;

    private MainAdapter mainAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);

        initBottomBar(savedInstanceState);
        initToolBar();
        initViewPager();

    }

    private void initViewPager() {
        mViewPager.setOffscreenPageLimit(3);
        mainAdapter = new MainAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mainAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                refreshToolBar(position);
                toolBar_title.setText(title_names[position]);
                mBottomBar.selectTabAtPosition(position,false);
                invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void refreshToolBar(int position)
    {
        isAddSmsOrGroup = (position != 2);
        if(position == 0)
        {
            btn_edit.setVisibility(View.VISIBLE);
        }
        else {
            btn_edit.setVisibility(View.GONE);
        }
    }

    private void initToolBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(null != actionBar) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }


    private void initBottomBar(Bundle savedInstanceState) {
        mBottomBar = BottomBar.attachShy(mCoordinatorLayout,null,savedInstanceState);
        mBottomBar.setItems(R.menu.bottombar_menu);
        mBottomBar.setOnTabClickListener(new OnTabClickListener() {
            @Override
            public void onTabSelected(int position) {
                refreshToolBar(position);
                mViewPager.setCurrentItem(position,false);
                toolBar_title.setText(title_names[position]);
                invalidateOptionsMenu();
            }

            @Override
            public void onTabReSelected(int position) {

            }
        });
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

    private class MainAdapter extends FragmentStatePagerAdapter
    {
        private SparseArray<WeakReference<Fragment>> mFragments = new SparseArray<>();
        public MainAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            mFragments.remove(position);
        }

        @Override
        public Fragment getItem(int position) {
            WeakReference<Fragment> ref = mFragments.get(position);
            Fragment f = ref != null ? ref.get() : null;
            if(f == null)
            {
                switch (position)
                {
                    case 0:
                        f = new ConversationFragment();
                        break;
                    case 1:
                        f = new FolderFragment();
                        break;
                    case 2:
                        f = new GroupFragment();
                        break;
                }
                mFragments.put(position,new WeakReference<>(f));
            }
            return f;
        }

        @Override
        public int getCount() {
            return title_names.length;
        }
    }
    private void showAddGroupDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        View view = View.inflate(this,R.layout.view_in_group_dialog,null);
        //给对话框设置title，并且使title居中
        TextView title = new TextView(this);
        title.setText("新建分组");
        title.setPadding(10, 10, 10, 10);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(24);
        title.setTextColor(Color.BLACK);
        dialog.setCustomTitle(title);
        final EditText input = (EditText) view.findViewById(R.id.et_new_group_dialog);
        Button btnOk = (Button) view.findViewById(R.id.btn_ok_new_group_dialog);
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel_new_group_dialog);
        btnCancel.setOnClickListener(new MyOnClickListener(dialog));
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = input.getText().toString();
                if(TextUtils.isEmpty(name))
                {
                    Toast.makeText(MainActivity.this,"请输入内容",Toast.LENGTH_SHORT).show();
                    return;
                }
                //如果有内容，将群组名添加至数据库
                //判断群组名称是否已经存在
                if(App.getGroupManager().isExist(name))
                {
                    //表明群组已经存在，需要重新输入
                    Toast.makeText(MainActivity.this,"该群组名称已经存在，请重新输入",Toast.LENGTH_SHORT).show();
                    input.setText("");
                    return;
                }
                else
                {
                    App.getGroupManager().createGroup(name);
                    GroupFragment groupFragment = (GroupFragment) mainAdapter.getItem(2);
                    groupFragment.updateDatas();
                }

                dialog.dismiss();
            }
        });
        dialog.setView(view,0,0,0,0);
        dialog.show();
    }
    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mBottomBar.onSaveInstanceState(outState);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_add_sms).setVisible(isAddSmsOrGroup);
        menu.findItem(R.id.action_search).setVisible(isAddSmsOrGroup);
        menu.findItem(R.id.action_add_group).setVisible(!isAddSmsOrGroup);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        }
        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.action_search:
                onSearchRequested();
                break;
            case R.id.action_add_sms:
                startActivity(new Intent(this, NewMessageUI.class));
                break;
            case R.id.action_add_group:
                showAddGroupDialog();
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        UiUtils.fixInputMethodManagerLeak(this);
    }
}
