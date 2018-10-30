package com.downriverdesign.snapandpack.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.downriverdesign.snapandpack.R;
import com.downriverdesign.snapandpack.database.DBHelper;

public class
SplashActivity extends AppCompatActivity {
    private static final int PARAM_REQ_CODE = 100;
    private static int SPLASH_TIME_OUT = 2000;
    public SharedPreferences mLoginPref = null;
    private String strLoginPreferences = "PrefLogin";
    private SharedPreferences.Editor mEditorLogin = null;
    private DBHelper dbHelper = null;
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private boolean permissionCheck = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mLoginPref = getApplicationContext().getSharedPreferences(strLoginPreferences, MODE_PRIVATE);
        mEditorLogin = mLoginPref.edit();
        init();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(SplashActivity.this, permissions, PARAM_REQ_CODE);
        } else {
            setSplashTimeOut(SPLASH_TIME_OUT);
        }
    }

    void init() {
        dbHelper = new DBHelper(SplashActivity.this);
        try {
            dbHelper.createDataBase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setSplashTimeOut(int SPLASH_TIME_OUT) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String strIsLogin = mLoginPref.getString("isLogin", "");
                if (strIsLogin.equals("yes")) {
                    Intent i = new Intent(SplashActivity.this, DashboardActivity.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.in_from_right_activity,
                            R.anim.out_to_left_activity);
                    finish();
                } else {
                    Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.in_from_right_activity,
                            R.anim.out_to_left_activity);
                    finish();
                }
            }
        }, SPLASH_TIME_OUT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PARAM_REQ_CODE:
                for (int i = 0; i < grantResults.length; i++) {
                    boolean flag = grantResults[i] == PackageManager.PERMISSION_GRANTED;
                    if (flag) {
                        permissionCheck = flag;
                    } else {
                        permissionCheck = flag;
                        break;
                    }
                }
                if (permissionCheck) {
                    setSplashTimeOut(SPLASH_TIME_OUT);
                }
                break;
            default:
                break;
        }

    }

}