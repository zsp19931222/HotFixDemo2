package com.zsp.hotfixdemo;

import android.app.Application;
import android.content.Context;
import android.os.Environment;

import com.zsp.hotfixdemo.hotfix.FixDexUtils;


/**
 * author：Andy on 2019/4/9 0009 10:39
 * email：zsp872126510@gmail.com
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        FixDexUtils.loadFixedDex(this, Environment.getExternalStorageDirectory());
    }
}
