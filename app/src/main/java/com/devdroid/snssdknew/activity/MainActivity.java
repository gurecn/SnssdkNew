package com.devdroid.snssdknew.activity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;

import com.devdroid.snssdknew.R;
import com.devdroid.snssdknew.adapter.SnssdkTextAdapter;
import com.devdroid.snssdknew.application.LauncherModel;
import com.devdroid.snssdknew.application.SnssdknewApplication;
import com.devdroid.snssdknew.base.BaseActivity;
import com.devdroid.snssdknew.eventbus.OnSnssdkLoadedEvent;
import com.devdroid.snssdknew.listener.NavigationItemSelectedListener;
import com.devdroid.snssdknew.manager.SnssdkTextManager;
import com.devdroid.snssdknew.preferences.IPreferencesIds;
import com.devdroid.snssdknew.utils.DividerItemDecoration;
import com.devdroid.snssdknew.utils.SimpleItemTouchHelperCallback;

/**
 * 主界面
 */
public class MainActivity extends BaseActivity implements SwipeRefreshLayout.OnRefreshListener {
    private RecyclerView mRecyclerView;
    private SnssdkTextAdapter mSnssdkAdapter;
    /**
     * 事件监听
     */
    private final Object mEventSubscriber = new Object() {
        //Snssdk下载完成事件
        @SuppressWarnings("unused")
        public void onEventMainThread(OnSnssdkLoadedEvent event) {
            mSnssdkAdapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(false);
        }
    };
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private SwitchCompat mSwNetSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        intData();
        SnssdknewApplication.getGlobalEventBus().register(mEventSubscriber);
    }

    private void intData() {
        if( !LauncherModel.getInstance().getSharedPreferencesManager().getBoolean(IPreferencesIds.DEFAULT_SHAREPREFERENCES_OFFLINE_MODE, false)) {
            mSwNetSetting.setChecked(false);
        } else {
            mSwNetSetting.setChecked(true);
        }
        mSwNetSetting.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    LauncherModel.getInstance().getSharedPreferencesManager().commitBoolean(IPreferencesIds.DEFAULT_SHAREPREFERENCES_OFFLINE_MODE, true);
                } else {
                    LauncherModel.getInstance().getSharedPreferencesManager().commitBoolean(IPreferencesIds.DEFAULT_SHAREPREFERENCES_OFFLINE_MODE, false);
                }
            }
        });
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mSnssdkAdapter);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.main_navigation_view);
        mSwNetSetting = (SwitchCompat)navigationView.getHeaderView(0).findViewById(R.id.switch_nav_header_net);
        NavigationItemSelectedListener navigationItemSelectedListener = new NavigationItemSelectedListener(this);
        navigationView.setNavigationItemSelectedListener(navigationItemSelectedListener);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefreshlayout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        mSnssdkAdapter = new SnssdkTextAdapter(this, SnssdkTextManager.getInstance().getmSnssdks());
        mRecyclerView.setAdapter(mSnssdkAdapter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SnssdknewApplication.getGlobalEventBus().unregister(mEventSubscriber);
    }

    @Override
    public void onRefresh() {
        mSwipeRefreshLayout.setRefreshing(true);
        SnssdkTextManager.getInstance().freshMore(this);
    }
}
