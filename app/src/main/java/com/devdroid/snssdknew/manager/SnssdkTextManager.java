package com.devdroid.snssdknew.manager;

import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.devdroid.snssdknew.R;
import com.devdroid.snssdknew.application.LauncherModel;
import com.devdroid.snssdknew.application.SnssdknewApplication;
import com.devdroid.snssdknew.eventbus.OnSnssdkLoadedEvent;
import com.devdroid.snssdknew.preferences.IPreferencesIds;
import com.devdroid.snssdknew.remote.LoadListener;
import com.devdroid.snssdknew.remote.RemoteSettingManager;
/**
 * 广告业务管理类<br>
 * @author laojiale
 */
public class SnssdkTextManager implements LoadListener {

    private static SnssdkTextManager sInstance;
    private List<String> mSnssdks;
    private RemoteSettingManager mRemoteSettingManager;

    private SnssdkTextManager() {
        mSnssdks = new ArrayList<>();
        init();
    }

    private void init() {
        mRemoteSettingManager = new RemoteSettingManager(this);
        mSnssdks = loadMore();
    }
    /**
     * 初始化单例,在程序启动时调用<br>
     */
    public static void initSingleton(Context context) {
        sInstance = new SnssdkTextManager();
    }

    /**
     * 获取单例.<br>
     */
    public static SnssdkTextManager getInstance() {
        return sInstance;
    }

    /**
     * 下拉刷新
     */
    public void freshMore(Context context) {
        if( LauncherModel.getInstance().getSharedPreferencesManager().getBoolean(IPreferencesIds.DEFAULT_SHAREPREFERENCES_OFFLINE_MODE, false)) {
            SnssdknewApplication.getGlobalEventBus().post(new OnSnssdkLoadedEvent(0));
            return;
        }
        if(checkPermissions(context)) {
            mRemoteSettingManager.connectToServer(context);
        }
        return ;
    }

    private boolean checkPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= 23) {
            if(!(context instanceof Activity)) return false;
            boolean readPhoneStateable = LauncherModel.getInstance().getSharedPreferencesManager().getBoolean(IPreferencesIds.DEFAULT_SHAREPREFERENCES_READ_PHONE_STATE,false);
            if(!readPhoneStateable) {
                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_PHONE_STATE}, 10010);
                int i = 0;
                do {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    int checkCallPhonePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE);
                    if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED && i >= 25) {
                        Toast.makeText(context, context.getResources().getString(R.string.string_no_phone_state), Toast.LENGTH_SHORT).show();
                        SnssdknewApplication.getGlobalEventBus().post(new OnSnssdkLoadedEvent(0));
                        return false;
                    } else if (checkCallPhonePermission == PackageManager.PERMISSION_GRANTED) {
                        LauncherModel.getInstance().getSharedPreferencesManager().commitBoolean(IPreferencesIds.DEFAULT_SHAREPREFERENCES_READ_PHONE_STATE, true);
                        break;
                    }
                } while (i++ < 25);
            }
        }
        return true;
    }

    /**
     * 上拉数据库加载
     */
    public List<String> loadMore() {
        List<String> cacheSnssdk = LauncherModel.getInstance().getSnssdkTextDao().queryLockerInfo();
        mSnssdks.addAll(cacheSnssdk);
        SnssdknewApplication.getGlobalEventBus().post(new OnSnssdkLoadedEvent(0));
        return cacheSnssdk;
    }

    /**
     * 数据联网加载成功
     */
    @Override
    public void loadLoaded(List<String> snssdks) {
        mSnssdks.clear();
        loadMore();
//        mLoadingAd = false;
        SnssdknewApplication.getGlobalEventBus().post(new OnSnssdkLoadedEvent(0));
    }

    public List<String> getmSnssdks() {
        return mSnssdks;
    }
}
