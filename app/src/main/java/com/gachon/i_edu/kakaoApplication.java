package com.gachon.i_edu;

import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;

public class kakaoApplication extends Application {

    private int count = 0;

    public int getCount(){
        return count;
    }
    public void setCount(int count){
        this.count = count;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        KakaoSdk.init(this,"832bdd088c9346274a5416f4a45f36c1");
    }
}
