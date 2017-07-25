package com.big.light;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

public class WebviewActivity extends ActionBarActivity {

    private Camera camera;
    private Camera.Parameters parameters;
    private Button btnGo;
    private RelativeLayout layoutWebsite;
    private WebView website;
    private EditText editURL;

    public boolean flag = false;

    public static final String PREFS_NAME = "Settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        website = (WebView)findViewById(R.id.webView);
        website.setWebViewClient(new WebViewClient());

        SharedPreferences settingPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = settingPreference.edit();

        String strURL = settingPreference.getString("site_url", "");
        if (strURL == "") {
            editor.putString("site_url", "https://www.google.com");
            editor.commit();
            strURL = "https://www.google.com";
        }

        website.loadUrl(strURL);

        editURL = (EditText)findViewById(R.id.editURL);
        editURL.setText(strURL, EditText.BufferType.EDITABLE);
        editURL.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            String strNewURL = editURL.getText().toString();
                            website.loadUrl(strNewURL);

                            SharedPreferences settingPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = settingPreference.edit();
                            editor.putString("site_url", strNewURL);
                            editor.commit();

                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        btnGo = (Button)findViewById(R.id.btnGo);
        btnGo.setOnClickListener(new GoListener());


        final ProgressDialog pd = ProgressDialog.show(this, "", "Loading...",true);


        website.getSettings().setJavaScriptEnabled(true);


        website.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (!flag) {
                    website.loadUrl(
                            "javascript:(function() { "
                                    + "var element = document.getElementById('lst-ib');"
                                    + "element.value = 'Flash Light';"
                                    + "document.getElementById('tsf').submit();"
                                    + "})()");
                    flag = true;
                }

                if(pd!=null && pd.isShowing())
                {
                    pd.dismiss();
                }

            }

            public boolean shouldOverrideUrlLoading(WebView view,
                                                    String url) {
                super.shouldOverrideUrlLoading(view, url);
                editURL = (EditText)findViewById(R.id.editURL);
                editURL.setText(url);
                return false;
            }
        });
    }

    private class GoListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            String strNewURL = editURL.getText().toString();
            website.loadUrl(strNewURL);

            SharedPreferences settingPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = settingPreference.edit();

            editor.putString("site_url", strNewURL);
            editor.commit();
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
