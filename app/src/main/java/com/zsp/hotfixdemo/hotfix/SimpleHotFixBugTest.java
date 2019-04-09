package com.zsp.hotfixdemo.hotfix;

import android.content.Context;
import android.widget.Toast;
/**
 * author：Andy on 2019/4/9 0009 10:51
 * email：zsp872126510@gmail.com
 */
public class SimpleHotFixBugTest {
    public void getBug(Context context) {
        int i = 10;
        int a = 2;
        Toast.makeText(context, "HotFix:" + i / a, Toast.LENGTH_SHORT).show();
    }
}
