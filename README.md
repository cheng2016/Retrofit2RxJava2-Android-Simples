# **Retrofit2RxJava2-Android-Simples**

这是一个Retrofit2+RxJava2的一个例子，是Retrofit2RxJava-Android-Simples的改版



#### 代码示例

```
//获取唯一单列
public static HttpImpl getInstance() {
    if (sInstance == null || mCompositeDisposable == null) {
        synchronized (HttpImpl.class) {
            Log.i(TAG, "HttpImpl.newInstance() excute ");
            if(sInstance == null)
                sInstance = new HttpImpl();
            if(mCompositeDisposable == null)
                mCompositeDisposable = new CompositeDisposable();
        }
    }

    return sInstance;
}
```

```
public void login(String auth) {
        getApiClient().login(auth)
                        .doOnNext(new Consumer<Token>(){//该方法执行请求成功后的耗时操作，比如数据库读写
                            @Override
                            public void accept(@NonNull Token token) throws Exception {
                                Log.i(TAG,"doOnNext() " + new Gson().toJson(token));
                            }

                        })
//                      .debounce(400, TimeUnit.MILLISECONDS)//限制400毫秒的频繁http操作
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<Token>() {
                            @Override
                            public void onError(Throwable throwable) {
                                Log.e(TAG , "login 请求失败！" + throwable.toString());
                                postEvent(new FailedEvent(MessageType.LOGIN));
                                mCompositeDisposable.clear();
                            }

                            @Override
                            public void onComplete() {
                                Log.i(TAG, "onComplete");
                            }

                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                mCompositeDisposable.add(d);
                            }

                            @Override
                            public void onNext(Token token) {
                                Log.i(TAG,"onNext()");
                                postEvent(token);
                                mCompositeDisposable.clear();
                            }
                        });
    }
```

#### 自动化构建

```
android {
    compileSdkVersion 25
    buildToolsVersion "25.0.3"
    defaultConfig {
        applicationId "com.chengzj.simple"
        minSdkVersion 15
        targetSdkVersion 25
        versionCode 1
        versionName "1.0"
        manifestPlaceholders = [app_label:"@string/app_name"]
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        test{
            keyAlias 'key'
            keyPassword '123456'
            storeFile file('chengzj.jks')
            storePassword '123456'
        }
    }

    buildTypes {
        release {
            //是否开启混淆
            minifyEnabled false
            useProguard true
//            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
            signingConfig signingConfigs.test
            manifestPlaceholders = [app_label:"@string/app_name"]
        }
        debug {
            minifyEnabled false
            useProguard false
            debuggable true
        }
    }

    lintOptions {
        abortOnError false //发生错误也继续打包
    }

    packagingOptions {//设置APK包的相关属性，exclude排除排除路径列表
//        exclude 'project.properties'
//        exclude 'META-INF/DEPENDENCIES'
//        exclude 'META-INF/NOTICE'
//        exclude 'META-INF/LICENSE'
//        exclude 'META-INF/LICENSE.txt'
//        exclude 'META-INF/NOTICE.txt'
    }

    //生成不同渠道包
    productFlavors {
        mock {
            applicationIdSuffix = ".mock"
        }
        prod {

        }
    }

    //修改生成包的包名
    android.variantFilter { variant ->
        if(variant.buildType.name.equals('release')
                && variant.getFlavors().get(0).name.equals('mock')) {
            variant.setIgnore(true);
        }
    }
    dexOptions {
        incremental true //优化编译速度
    }
}
```

### 参考

[【Android Studio】入门系列6.1。打包 - 详解build.gradle](http://www.jianshu.com/p/7e3a69dbd20e)

[Android Studio下项目构建的Gradle配置及打包](http://www.cnblogs.com/cppys/p/6640284.html)