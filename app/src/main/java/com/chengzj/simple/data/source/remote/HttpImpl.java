package com.chengzj.simple.data.source.remote;

import android.util.Log;

import com.google.gson.Gson;
import com.chengzj.simple.model.entity.Profile;
import com.chengzj.simple.model.entity.RefreshRequest;
import com.chengzj.simple.model.entity.Token;
import org.greenrobot.eventbus.EventBus;

import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by mitnick.cheng on 2016/7/24.
 */

public class HttpImpl {
    private final static String TAG = "HttpImpl";

    private static volatile HttpImpl sInstance;
    private static volatile Http mApiClient;

    private static CompositeDisposable mCompositeDisposable;

    private HttpImpl() {
    }

    public Http getApiClient() {
        if (mApiClient == null) {
            synchronized (this) {
                Log.i(TAG, "Http.newInstance() excute ");
                mApiClient = HttpFactory.createRetrofit2RxJavaService(Http.class);
            }
        }
        return mApiClient;
    }

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

    private final void postEvent(Object object) {
        EventBus.getDefault().post(object);
    }

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

    public void getProfiles(String accessToken) {
        Call<Profile> call = getApiClient().getProfiles(accessToken);
        call.enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(Call<Profile> call, Response<Profile> response) {
                if (response.isSuccessful()) {
                    postEvent(response.body());
                } else {
                    Log.e(TAG , "getProfiles 请求失败！" +  response.code());
                    postEvent(new FailedEvent(MessageType.PROFILE));
                }
            }

            @Override
            public void onFailure(Call<Profile> call, Throwable throwable) {
                Log.e(TAG , "getProfiles 请求失败！" + throwable.toString());
                postEvent(new FailedEvent(MessageType.PROFILE));
            }
        });
    }

    public void getProfile(String accessToken) {
        getApiClient().getProfile(accessToken)
        //               .debounce(400, TimeUnit.MILLISECONDS)//限制400毫秒的频繁http操作
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<Profile>() {
                            @Override
                            public void onError(Throwable throwable) {
                                Log.e(TAG , "getProfile 请求失败！" + throwable.toString());
                                postEvent(new FailedEvent(MessageType.PROFILE));
                                mCompositeDisposable.clear();

                            }

                            @Override
                            public void onComplete() {

                            }

                            @Override
                            public void onSubscribe(@NonNull Disposable d) {
                                mCompositeDisposable.add(d);
                            }

                            @Override
                            public void onNext(Profile profile) {
                                postEvent(profile);
                                mCompositeDisposable.clear();

                            }
                        });
    }

    public void refresh(String refreshToken) {
        Call<Token> call = getApiClient().refresh(new RefreshRequest(refreshToken));
        call.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                if (response.isSuccessful()) {
                    postEvent(response.body());
                } else {
                    Log.e(TAG,"refresh 请求失败！" +  response.code());
                    postEvent(new FailedEvent(MessageType.REFRESH));
                }
            }

            @Override
            public void onFailure(Call<Token> call, Throwable throwable) {
                Log.e(TAG,"refresh 请求失败！" + throwable.toString());
                postEvent(new FailedEvent(MessageType.REFRESH));
            }
        });
    }

    //先登录，然后马上请求信息，连续请求2次网络请求
    public void loginAndGetProfile(String auth){
        getApiClient().login(auth)
                .flatMap(new Function<Token, ObservableSource<Profile>>() {
                    @Override
                    public ObservableSource<Profile> apply(@NonNull Token token) throws Exception {
                        Log.e(TAG,"loginAndGetProfile flatMap 请求成功！ " + new Gson().toJson(token));
                        return getApiClient().getProfile(token.getAccess_token());
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Profile>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        mCompositeDisposable.add(d);
                    }

                    @Override
                    public void onNext(@NonNull Profile profile) {
                        Log.e(TAG,"loginAndGetProfile 请求成功！" + new Gson().toJson(profile));
                        postEvent(profile);
                        mCompositeDisposable.clear();
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e(TAG,"loginAndGetProfile 请求失败！" +e.toString());
                        postEvent(new FailedEvent(MessageType.PROFILE));
                        mCompositeDisposable.clear();
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG , "onComplete！");
                    }
                });

    }
}
