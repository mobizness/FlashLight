package com.big.light;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Switch;

public class SettingActivity extends ActionBarActivity {

    private Camera camera;
    private Camera.Parameters parameters;
    private ImageButton flashLightButton;
    private RelativeLayout layoutWebsite;
    private RelativeLayout layoutWebsite2;
    private Switch switchview;

    WifiManager wifiManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        layoutWebsite= (RelativeLayout)findViewById(R.id.relativeLayout3);
        layoutWebsite.setOnClickListener(new WebsiteListener());

        layoutWebsite2= (RelativeLayout)findViewById(R.id.relativeLayout2);
        layoutWebsite2.setOnClickListener(new SMSListener());

        switchview = (Switch)findViewById(R.id.switch1);
        switchview.setOnClickListener(new WifiOnOffListener());

//        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        boolean wifiEnabled = wifiManager.isWifiEnabled();

        if (wifiEnabled)
            switchview.setChecked(true);
        else
            switchview.setChecked(false);
    }

    private class WebsiteListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(SettingActivity.this, WebviewActivity.class);
            startActivity(intent);
        }

    }

    private class SMSListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Intent sendIntent = new Intent(Intent.ACTION_VIEW);
            sendIntent.setData(Uri.parse("sms:"));
            startActivity(sendIntent);
        }

    }

    private class WifiOnOffListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            if (switchview.isChecked()) {
                wifiManager.setWifiEnabled(true);
            }
            else {
                wifiManager.setWifiEnabled(false);

            }
        }

    }

    private void showNoFlashAlert() {
        new AlertDialog.Builder(this)
                .setMessage("Your device hardware does not support flashlight!")
                .setIcon(android.R.drawable.ic_dialog_alert).setTitle("Error")
                .setPositiveButton("Ok", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
//                        finish();
                    }
                }).show();
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
