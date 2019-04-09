package com.zsp.hotfixdemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.View;

import com.zsp.hotfixdemo.hotfix.FixDexUtils;
import com.zsp.hotfixdemo.hotfix.SimpleHotFixBugTest;

/**
 * author：Andy on 2019/4/9 0009 10:51
 * email：zsp872126510@gmail.com
 */

public class MainActivity extends Activity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    }

    public void fix(View view) {
        FixDexUtils.loadFixedDex(this, Environment.getExternalStorageDirectory());

    }

    public void show(View view) {
        SimpleHotFixBugTest test = new SimpleHotFixBugTest();
        test.getBug(this);
    }
}
