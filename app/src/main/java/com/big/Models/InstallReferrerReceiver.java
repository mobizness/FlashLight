package com.big.Models;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;

import cz.msebera.android.httpclient.Header;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by scs on 6/13/17.
 */

public class InstallReferrerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String referrer = intent.getStringExtra("referrer");
//        Toast.makeText(context, referrer, Toast.LENGTH_LONG).show();

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
        //Use the referrer
        SharedPreferences prefs = context.getSharedPreferences("flashlight_Pref", MODE_PRIVATE);

        prefs.edit().putString("referrer", referrer).commit();

        String android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);


        String packageName = context.getPackageName();

        RequestParams params = new RequestParams();
        params.put("android_id", android_id);
        params.put("referrer", referrer);
        params.put("package", packageName);
        callAPI(Constant.URL_BOOT, params);

        Intent i = new Intent();
        i.setClassName("com.big.light", "com.big.light.MainActivity");
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    public void callAPI(String url, final RequestParams params) {

        HttpUtils.post(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray arrResponse) {
                // Pull out the first event on the public timeline
                Log.d("response", "---------------- this is response : " + arrResponse);

                if (Global.g_main != null) {
                    Global.g_main.callAPI(Constant.URL_BOOT, params);
                }
            }

            @Override
            public void onStart() {
                // called before request is started
            }


            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
    }
}