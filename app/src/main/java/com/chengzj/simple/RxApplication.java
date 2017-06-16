package com.chengzj.simple;

import android.app.Application;

import com.chengzj.simple.base.BaseApplication;


/**
 * Created by mitnick.cheng on 2016/7/21.
 */

public class RxApplication extends BaseApplication {

    private static RxApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;

    }

    public  synchronized static  RxApplication getInstance(){
        return sInstance;
    }
}
