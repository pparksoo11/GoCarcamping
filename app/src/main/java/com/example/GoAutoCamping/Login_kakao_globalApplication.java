package com.example.GoAutoCamping;

import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;

public class Login_kakao_globalApplication extends Application {
    private static Login_kakao_globalApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        //네이티브 앱 키로 초기화
        KakaoSdk.init(this, "6df02d45e8bb02153be4f91e20645bad");
    }
}
