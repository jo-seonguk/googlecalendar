package com.googlecalendar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class Fragment2Dialog extends Fragment {
    private static final String TAG = "dialog2";
    private SharedViewModel sharedViewModel;
    private Fragment fragment;

    private WebView webView;        // 웹뷰
    WebSettings settings;
    private TextView txt_address;
    private Handler handler;

    Button negabutton;

    OnDatabaseCallback callback;
    Context ct;

    String shareInfo;
    String[] share;

    String s01, s02, s03;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callback = (OnDatabaseCallback) getActivity();
    }
    class MyJavaScriptInterface {
        @JavascriptInterface
        @SuppressWarnings("unused")
        public void processDATA(String data) {
            /*
            Bundle extra = new Bundle();
            Intent intent = new Intent();
            extra.putString("data", data);
            intent.putExtras(extra);
            setResult(RESULT_OK, intent);
            finish();*/
        }
    }
    public Fragment2Dialog() {

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment2_dialog, container, false);

        negabutton = view.findViewById(R.id.negabutton);

        webView = (WebView) view.findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        txt_address = view.findViewById(R.id.txt_address);
        // WebView 초기화
        init_webView();

        // 핸들러를 통한 JavaScript 이벤트 반응
        handler = new Handler();

        negabutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity)getActivity()).onFragmentSelected(2, null);
            }
        });



        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getLiveData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
/*
                shareInfo = s;
                share = shareInfo.split("˘");
                s01 = share[0];
                s02 = share[2];
                s03 = share[3];*/
            }
        });
    }

    public void init_webView() {

        settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        // JavaScript 허용

        // JavaScript의 window.open 허용
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(true);

        // JavaScript이벤트에 대응할 함수를 정의 한 클래스를 붙여줌
        webView.addJavascriptInterface(new AndroidBridge(), "TestApp");

        // web client 를 chrome 으로 설정
        webView.setWebChromeClient(new WebChromeClient());

        // webview url load. php 파일 주소
        webView.loadUrl("wewill10.dothome.co.kr/daum.html");
        //webView.loadUrl("file:///android_asset/daum.html");
        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverriedUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
        });
        Bundle bundle = getArguments();  //번들 받기. getArguments() 메소드로 받음.
        String name = null;
        if(bundle != null){
            name = bundle.getString("log"); //Name 받기.
            Log.i("log", name);
        }
    }

    private class AndroidBridge {
        @JavascriptInterface
        public void setAddress(final String arg1, final String arg2, final String arg3) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    txt_address.setText(String.format("%s %s", arg2, arg3));

                    String sendtext = s01 + "⌒" + txt_address + "⌒" + s02 + "˘" + s03;

                    sharedViewModel.setLiveData(txt_address.getText().toString());
                    //sharedViewModel.setLiveData(sendtext);
                    ((MainActivity)getActivity()).onFragmentSelected(2, null);
                    // WebView를 초기화 하지않으면 재사용할 수 없음
                    init_webView();
                }
            });
        }
    }



    /*
    https://blog.naver.com/dla210/221657051563
    https://jeonghoe9.tistory.com/21
    https://onedaycodeing.tistory.com/62
    https://www.masterqna.com/android/90457/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EC%9B%B9%EB%B7%B0%EC%97%90%EC%84%9C-%ED%8C%9D%EC%97%85-%EB%9D%84%EC%9B%A0%EC%9D%84-%EA%B2%BD%EC%9A%B0-%EB%A1%9C%EC%A7%81%EC%97%90-%EB%8C%80%ED%95%B4-%EA%B6%81%EA%B8%88%ED%95%9C%EC%A0%90%EC%9D%B4-%EC%9E%88%EC%8A%B5%EB%8B%88%EB%8B%A4
    https://www.masterqna.com/android/91571/%EC%9B%B9%EB%B7%B0-%EB%8B%A4%EC%9D%8C-%EC%9A%B0%ED%8E%B8-%EB%B2%88%ED%98%B8-window-open-%EC%A7%88%EB%AC%B8%EC%9E%88%EC%8A%B5%EB%8B%88%EB%8B%A4
    https://hoyi327.tistory.com/27


    https://jeongupark-study-house.tistory.com/191
     */
}
