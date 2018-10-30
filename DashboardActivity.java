package com.downriverdesign.snapandpack.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.downriverdesign.snapandpack.R;
import com.downriverdesign.snapandpack.common.Common;
import com.downriverdesign.snapandpack.database.DBHelper;
import com.downriverdesign.snapandpack.model.ItemDetailsModel;

import java.io.File;
import java.util.ArrayList;

public class DashboardActivity extends Activity implements View.OnClickListener {
    private Button mbtnPackNewBox = null;
    private Button mbtnPrintQRCode = null;
    private Button mbtnUnpackBox = null;
    private Button mbtnLogout = null;
    private Button mbtnFacebook = null;
    private Button mbtnTwitter = null;
    //Preference for BOxName
    private SharedPreferences mPref = null;
    private SharedPreferences.Editor mEditor = null;
    private DBHelper mDBHelper = null;
    private ArrayList<ItemDetailsModel> alItemDetailsModel = null;
    ///Login Preference variables
    private String strLoginPreferences = "PrefLogin";
    private SharedPreferences.Editor mEditorLogin = null;
    private SharedPreferences mLoginPref = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        initialize();
    }

    /*
    * Initialization of Variables.
    * */
    public void initialize() {
        mbtnPackNewBox = (Button) findViewById(R.id.btnPackNewBox);
        mbtnPackNewBox.setOnClickListener(DashboardActivity.this);
        mbtnPrintQRCode = (Button) findViewById(R.id.btnPrintQrCode);
        mbtnPrintQRCode.setOnClickListener(DashboardActivity.this);
        mbtnUnpackBox = (Button) findViewById(R.id.btnUnpackBox);
        mbtnUnpackBox.setOnClickListener(DashboardActivity.this);
        mbtnLogout = (Button) findViewById(R.id.btnLogout);
        mbtnLogout.setOnClickListener(DashboardActivity.this);
        mbtnFacebook = (Button) findViewById(R.id.btnFacebook);
        mbtnFacebook.setOnClickListener(DashboardActivity.this);
        mbtnTwitter = (Button) findViewById(R.id.btnTwitter);
        mbtnTwitter.setOnClickListener(DashboardActivity.this);
        mPref = getSharedPreferences("QrInputPref", MODE_PRIVATE);
        mEditor = mPref.edit();
        mLoginPref = getApplicationContext().getSharedPreferences(strLoginPreferences, MODE_PRIVATE);
        mEditorLogin = mLoginPref.edit();
        mDBHelper = new DBHelper(DashboardActivity.this);
        alItemDetailsModel = new ArrayList<ItemDetailsModel>();
    }

    /*
    * handle back button of device
    * */
    @Override
    public void onBackPressed() {
        finishAppDialog();
    }


    /*
    * Below function will open the Alert dialog for closing the Application.
    * */
    private void finishAppDialog() {
        AlertDialog.Builder alertForSaveBox = new AlertDialog.Builder(DashboardActivity.this);
        alertForSaveBox.setTitle(getResources().getString(R.string.strAlertTitle));
        alertForSaveBox.setMessage(getResources().getString(R.string.strExitConfirmation));
        alertForSaveBox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        alertForSaveBox.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

            }

        });
        alertForSaveBox.show();
    }

    /*
    * Below function will prompts the Dialog to take Boxname as input and Save BoxName to Preferences.
    * */
    public void dialogBoxName() {
        final Dialog dialog = new Dialog(DashboardActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.custom_dialog_addnewbox);
        final EditText edBoxName = (EditText) dialog.findViewById(R.id.edBoxName);
        Button btnDissmissDialog = (Button) dialog.findViewById(R.id.btnOk);
        btnDissmissDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog != null) {
                    if (dialog.isShowing()) {
                        if (Common.isValidLength(edBoxName.getText().toString().trim())) {
                            String strBoxName = edBoxName.getText().toString().trim();
                            strBoxName = strBoxName.replace(" ", "");
                            edBoxName.setText(strBoxName);
                            dialog.dismiss();
                            Intent intentAddNewBox = new Intent(DashboardActivity.this, AddNewBoxActivity.class);
                            mEditor.putString("QrInput", edBoxName.getText().toString().trim());
                            mEditor.commit();
                            alItemDetailsModel = mDBHelper.getBoxItems();
                            if (alItemDetailsModel.size() > 0) {
                                mDBHelper.removeBoxItems();
                            }
                            startActivity(intentAddNewBox);
                            overridePendingTransition(R.anim.in_from_right_activity,
                                    R.anim.out_to_left_activity);
                            finish();
                        } else {
                            edBoxName.setError(getResources().getString(R.string.strErrorBoxName));
                        }
                    }
                }
            }
        });
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPackNewBox:
                dialogBoxName();
                break;
            case R.id.btnUnpackBox:
                if (Common.isOnline(DashboardActivity.this)) {
                    Intent intentUnpack = new Intent(DashboardActivity.this, ScanQRCodeActivity.class);
                    startActivity(intentUnpack);
                    overridePendingTransition(R.anim.in_from_right_activity,
                            R.anim.out_to_left_activity);
                    finish();
                } else {
                    Common.displayToast(DashboardActivity.this, getResources().getString(R.string.strErrorInternetConnection));
                }
                break;
            case R.id.btnPrintQrCode:
                if (Common.isOnline(DashboardActivity.this)) {
                    Intent intentPrintQR = new Intent(DashboardActivity.this, QRCodeListActivity.class);
                    startActivity(intentPrintQR);
                    overridePendingTransition(R.anim.in_from_right_activity,
                            R.anim.out_to_left_activity);
                    finish();
                } else {
                    Common.displayToast(DashboardActivity.this, getResources().getString(R.string.strErrorInternetConnection));
                }

                break;
            case R.id.btnFacebook:
                if (Common.isOnline(DashboardActivity.this)) {
                    try {
                        String url = "https://www.facebook.com/snapnpack/?ref=hl";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Common.displayToast(DashboardActivity.this, getResources().getString(R.string.strErrorInternetConnection));
                }

                break;
            case R.id.btnTwitter:
                if (Common.isOnline(DashboardActivity.this)) {
                    try {
                        String url = "https://twitter.com/snapnpackapp1";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Common.displayToast(DashboardActivity.this, getResources().getString(R.string.strErrorInternetConnection));
                }
                break;
            case R.id.btnLogout:
                AlertDialog.Builder alertForLogout = new AlertDialog.Builder(DashboardActivity.this);
                alertForLogout.setMessage("Are you want to logout ?");
                alertForLogout.setTitle(getResources().getString(R.string.strAlertTitle));
                alertForLogout.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        mEditorLogin.remove("isLogin");
                        mEditorLogin.remove("userId");
                        mEditorLogin.clear();
                        mEditorLogin.commit();
                        try {
                            File deleteDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snap 'n Pack");
                            Common.deleteDirectory(deleteDir);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                        startActivity(intent);
                        DashboardActivity.this.overridePendingTransition(R.anim.in_from_left_activity,
                                R.anim.out_to_right_activity);
                        finish();

                    }
                });
                alertForLogout.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                });
                alertForLogout.show();
                break;
        }
    }
}
