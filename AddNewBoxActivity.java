package com.downriverdesign.snapandpack.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.downriverdesign.snapandpack.R;
import com.downriverdesign.snapandpack.adapter.ItemListAdapter;
import com.downriverdesign.snapandpack.common.Common;
import com.downriverdesign.snapandpack.database.DBHelper;
import com.downriverdesign.snapandpack.model.ItemDetailsModel;
import com.downriverdesign.snapandpack.qrcode.QRCodeEncoder;
import com.downriverdesign.snapandpack.utils.HttpUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.android.Contents;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.RequestBody;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AddNewBoxActivity extends Activity implements View.OnClickListener {
    public static Bitmap bitmap = null;
    private ImageButton mimgBtnBack = null;
    private ImageButton mimgBtnAddNewItem = null;
    private TextView mtxtAddNewBoxHeader = null;
    private ListView mlistViewBoxItems = null;
    private Button mbtnGenerateQRCode = null;
    private String str = "";
    private DBHelper mDBHelper = null;
    private String strQRCodeId1 = null;
    //Preference for BOxName
    private SharedPreferences mPref = null;
    private SharedPreferences.Editor mEditor = null;
    private String mstrQRResponse = null;
    ////Login Preferences
    private String strLoginPreferences = "PrefLogin";
    private SharedPreferences.Editor mEditorLogin = null;
    private SharedPreferences mLoginPref = null;
    private int userId = -1;
    private String strBoxName = "";
    private int noOfItems = -1;
    private ArrayList<ItemDetailsModel> alItems = null;
    private ArrayList<String> alCapturedImagesPath = null;
    private File qrImagePath = null;
    private File tempItemImagePath = null;
    private String strQrcodeId = "";
    private String strCurDateTime = "";
    private ItemListAdapter adpt = null;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_box);
        initialize();
        try {
            new AdaptAsync().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    * Initialization of Variables.
    * */
    public void initialize() {
        mPref = getSharedPreferences("QrInputPref", MODE_PRIVATE);
        mEditor = mPref.edit();
        strBoxName = mPref.getString("QrInput", "");
        mimgBtnBack = (ImageButton) findViewById(R.id.imgBtnBackAddNewBoxName);
        mimgBtnBack.setOnClickListener(AddNewBoxActivity.this);
        mimgBtnAddNewItem = (ImageButton) findViewById(R.id.imgBtnAddBoxItem);
        mimgBtnAddNewItem.setOnClickListener(AddNewBoxActivity.this);
        mbtnGenerateQRCode = (Button) findViewById(R.id.btnConfirmNGenerateQRCode);
        mbtnGenerateQRCode.setOnClickListener(AddNewBoxActivity.this);
        mtxtAddNewBoxHeader = (TextView) findViewById(R.id.txtAddNewBoxHeader);
        mtxtAddNewBoxHeader.setText(strBoxName);
        mDBHelper = new DBHelper(AddNewBoxActivity.this);
        mlistViewBoxItems = (ListView) findViewById(R.id.listViewBoxItem);
        mLoginPref = getApplicationContext().getSharedPreferences(strLoginPreferences, MODE_PRIVATE);
        mEditorLogin = mLoginPref.edit();
        userId = mLoginPref.getInt("userId", -1);
        alItems = new ArrayList<>();
        alCapturedImagesPath = new ArrayList<>();
        strCurDateTime = generateCurrentTime();
        strQrcodeId = strBoxName + "_" + String.valueOf(mLoginPref.getInt("userId", 1)) + "_" + strCurDateTime;
    }

    /*
   * handle back button of device
   * */
    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertForRemoveBox = new AlertDialog.Builder(AddNewBoxActivity.this);
        alertForRemoveBox.setTitle(getResources().getString(R.string.strAlertTitle));
        alertForRemoveBox.setMessage("Your Box : " + strBoxName + " will be removed. Are You sure ?");
        alertForRemoveBox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                try {
                    mDBHelper.removeBoxItems();

                    // deleteCapturedImages();
                    mDBHelper.removeCapturedImages();
                    // Common.displayToast(AddNewBoxActivity.this, "Box Removed: " + strBoxName);
                    mEditor.remove("QrInput");
                    mEditor.clear();
                    mEditor.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(AddNewBoxActivity.this, DashboardActivity.class);
                startActivity(intent);
                AddNewBoxActivity.this.overridePendingTransition(R.anim.in_from_left_activity,
                        R.anim.out_to_right_activity);
                finish();
            }

        });
        alertForRemoveBox.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                //nothing to do here, created just to guide user
            }

        });
        alertForRemoveBox.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgBtnAddBoxItem:
                Intent intent = new Intent(AddNewBoxActivity.this, AddBoxItemActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.in_from_right_activity,
                        R.anim.out_to_left_activity);
                finish();
                break;
            case R.id.btnConfirmNGenerateQRCode:
                try {
                    if (Common.isOnline(AddNewBoxActivity.this)) {
                        if (mDBHelper.totalItems() > 0) {
                            AlertDialog.Builder alertForSaveBox = new AlertDialog.Builder(AddNewBoxActivity.this);
                            alertForSaveBox.setTitle(getResources().getString(R.string.strAlertTitle));
                            alertForSaveBox.setMessage(getResources().getString(R.string.strSaveBoxConfirmation) + " " + strBoxName + " ?");
                            alertForSaveBox.setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    generateQRCode();//will generate QRCode
                                    mDBHelper.removeBoxItems();
                                    mEditor.remove("QrInput");
                                    mEditor.clear();
                                    mEditor.commit();

                                    if (Common.isOnline(AddNewBoxActivity.this)) {
                                        new QRGeneratorTask().execute();
                                    } else {
                                        Common.displayToast(AddNewBoxActivity.this, getResources().getString(R.string.strErrorInternetConnection));
                                    }

                                }
                            });
                            alertForSaveBox.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    //nothing to do here , created just to guide user.
                                }

                            });
                            alertForSaveBox.show();
                        } else {
                            AlertDialog.Builder alertForEmptyBox = new AlertDialog.Builder(AddNewBoxActivity.this);
                            alertForEmptyBox.setTitle(getResources().getString(R.string.strAlertTitle));
                            alertForEmptyBox.setMessage("Box should not be empty.\nPlease, add an item to generate QRCode.");
                            alertForEmptyBox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    //nothing to do here , created just to guide user.
                                }
                            });
                            alertForEmptyBox.show();
                        }
                    } else {
                        Common.displayToast(AddNewBoxActivity.this, getResources().getString(R.string.strErrorInternetConnection));
                    }

                    //  deleteCapturedImages();
                    mDBHelper.removeCapturedImages();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.imgBtnBackAddNewBoxName:
                AlertDialog.Builder alertForRemoveBox = new AlertDialog.Builder(AddNewBoxActivity.this);
                alertForRemoveBox.setTitle(getResources().getString(R.string.strAlertTitle));
                alertForRemoveBox.setMessage("Your Box : " + strBoxName + " will be removed. Are You sure ?");
                alertForRemoveBox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        try {
                            mDBHelper.removeBoxItems();
                            // deleteCapturedImages();
                            mDBHelper.removeCapturedImages();
                            // Common.displayToast(AddNewBoxActivity.this, "Box Removed: " + strBoxName);
                            mEditor.remove("QrInput");
                            mEditor.clear();
                            mEditor.commit();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Intent intent = new Intent(AddNewBoxActivity.this, DashboardActivity.class);
                        startActivity(intent);
                        AddNewBoxActivity.this.overridePendingTransition(R.anim.in_from_left_activity,
                                R.anim.out_to_right_activity);
                        finish();
                    }

                });
                alertForRemoveBox.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                    }

                });
                alertForRemoveBox.show();
                break;
        }
    }

    /*
    * Below function will generate QRCode according to given Input i.e.strQrcodeId
    * */
    public void generateQRCode() {
        WindowManager manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3 / 4;
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder(strQrcodeId,
                null,
                Contents.Type.TEXT,
                BarcodeFormat.QR_CODE.toString(),
                smallerDimension);
        try {
            bitmap = qrCodeEncoder.encodeAsBitmap();
            //inorder to save QRImage to SDCard
            saveQRBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    /*
    * Below function will save Bitmap for generated QRCode to SDCard.
    * */
    private void saveQRBitmap(Bitmap thumbnail) {
        String QRfilePath = Environment.getExternalStorageDirectory().toString() + "/Snap 'n Pack/SnapNpackQR/";
        File snapNpackDirectory = new File(QRfilePath);
        String filename = strBoxName + ".jpg";
        String tempFile = filename.replaceAll("[|/?*<\":>+\\[\\]/'#]", "");
        String tempRemoveModulo = tempFile.replaceAll("%", "");
        //make directory in sd card
        snapNpackDirectory.mkdirs();
        //combine filename with snapNpackdirectory
        qrImagePath = new File(snapNpackDirectory, tempRemoveModulo);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(qrImagePath);
            thumbnail.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*
    * Below function will generate current date and time that will be appended to the QRCode input.
    * */
    public String generateCurrentTime() {
        SimpleDateFormat s = new SimpleDateFormat(getResources().getString(R.string.strDateFormat));
        String format = s.format(new Date());
        return format;
    }

    public void deleteBoxItem(View v) {
        position = mlistViewBoxItems.getPositionForView(v);
        long _id = mlistViewBoxItems.getAdapter().getItemId(position);
        AlertDialog.Builder alertForLogout = new AlertDialog.Builder(AddNewBoxActivity.this);
        alertForLogout.setMessage("Box item will be deleted. Are you sure ?");
        alertForLogout.setTitle(getResources().getString(R.string.strAlertTitle));
        alertForLogout.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

                float flag = mDBHelper.removeBoxItem(String.valueOf(alItems.get(position).getStrItemId()));
                try {
                    new AdaptAsync().execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        alertForLogout.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        alertForLogout.show();
    }

    /*
    * Below Class is Asynchronous task for displaying ItemList in ListView.
    * */
    public class AdaptAsync extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Common.loadProgressBar(AddNewBoxActivity.this);
        }

        @Override
        protected Void doInBackground(Void... params) {
            alItems = mDBHelper.getBoxItems();
            noOfItems = mDBHelper.totalItems();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Common.dismissDialog();
            adpt = new ItemListAdapter(AddNewBoxActivity.this, alItems, 1);
            mlistViewBoxItems.setAdapter(adpt);
        }
    }

    /*
    * Below class will post the Box containing ItemDetails to Server.
    * */
    public class QRGeneratorTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            try {
                Common.loadProgressBar(AddNewBoxActivity.this);
            } catch (Exception e) {

            }

        }

        @Override
        protected Void doInBackground(Void... params) {


            String strQRImage = strQrcodeId.replaceAll("[|/?*<\":>+\\[\\]/'#]", "");
            String strRemoveModulo = strQRImage.replaceAll("%", "");
            //Log.e(">>>>>>>>QRCodeId", ""+strQrcodeId);
            File fImagePath = null;
            RequestBody requestBody = null;
            MultipartBuilder multipartBuilder = new MultipartBuilder().type(MultipartBuilder.FORM);
            multipartBuilder
                    .addFormDataPart("func", "savebox")
                    .addFormDataPart("userid", String.valueOf(mLoginPref.getInt("userId", 1)))
                    .addFormDataPart("boxname", strBoxName)
                    .addFormDataPart("qr_code_id", strQrcodeId)
                    .addFormDataPart("qr_code_image", String.valueOf(strRemoveModulo) + ".png", RequestBody.create(MediaType.parse("image/png"), qrImagePath))
                    .addFormDataPart("count", String.valueOf(noOfItems));
            strQRCodeId1 = String.valueOf(strQrcodeId);
            for (int i = 0; i < noOfItems; i++) {
                if (alItems.get(i).getStrItemImage().length() > 0) {
                    fImagePath = new File(String.valueOf(alItems.get(i).getStrItemImage()));
                }
                multipartBuilder.addFormDataPart("item_name" + String.valueOf(i + 1), alItems.get(i).getStrItemName())
                        .addFormDataPart("tag_item" + String.valueOf(i + 1), alItems.get(i).getStrItemTags())
                        .addFormDataPart("note" + String.valueOf(i + 1), alItems.get(i).getStrItemDescription());
                if (alItems.get(i).getStrItemImage().length() > 0) {
                    multipartBuilder.addFormDataPart("image" + String.valueOf(i + 1), String.valueOf(alItems.get(i).getStrItemName() + ".png"), RequestBody.create(MediaType.parse("image/png"), fImagePath));
                } else {
                    multipartBuilder.addFormDataPart("image" + String.valueOf(i + 1), "");
                }

                str = String.valueOf(alItems.get(i).getStrItemImage());
            }
            requestBody = multipartBuilder.build();
            try {
                /*
                * Posting data using below function..
                * */
                mstrQRResponse = HttpUtils.postRun("savebox", requestBody);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Common.dismissDialog();
            ByteArrayOutputStream bStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bStream);
            byte[] byteArray = bStream.toByteArray();

            Intent intentPrintQR = new Intent(AddNewBoxActivity.this, PrintQRCodeActivity.class);
            intentPrintQR.putExtra("image", byteArray);
            intentPrintQR.putExtra("BoxName", strBoxName);
            intentPrintQR.putExtra("QRCodeId", strQRCodeId1);
            startActivity(intentPrintQR);
            overridePendingTransition(R.anim.in_from_right_activity,
                    R.anim.out_to_left_activity);
            finish();
        }
    }
}
