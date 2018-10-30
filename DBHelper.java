package com.downriverdesign.snapandpack.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.downriverdesign.snapandpack.R;
import com.downriverdesign.snapandpack.common.Common;
import com.downriverdesign.snapandpack.model.ItemDetailsModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by jaimin on 30/11/15.
 */
public class DBHelper extends SQLiteOpenHelper {

    //http://216.55.169.45/~snapnpack/master/webservice.php?func=savebox&userid=&boxname=Box1&qr_code_id=12321&qr_code_image=qr_code_image&count=5&item_name1=name1&tag_item1=tag1&note1=note1&image1=image1
    private static final String COL_ITEMID = "itemid";
    private static final String COL_ITEMNAME = "itemname";
    private static final String COL_ITEMIMAGE = "itemImage";
    private static final String COL_ITEMTAG = "tag";
    private static final String COL_ITEMCOMMENT = "itemcomment";
    private static final String COL_CAPTURED = "CapturedImages";
    private static final String TB_BOXITEMS = "BoxItems";
    private static final String TB_CapturedItemImages = "TB_Captured";
    private static SQLiteDatabase myDataBase;
    private static Context myContext;

    @SuppressWarnings("static-access")
    public DBHelper(Context context) {

        super(context, context.getResources().getString(R.string.DB_NAME),
                null, 1);
        this.myContext = context;
    }

    public static void openDataBase() throws SQLException {

        // --- Open the database---
        String myPath = myContext.getString(R.string.DB_PATH)
                + myContext.getString(R.string.DB_NAME);
        myDataBase = SQLiteDatabase.openDatabase(myPath, null,
                SQLiteDatabase.OPEN_READWRITE);


    }

    // ---Create the database---
    public void createDataBase() throws Exception {

        // ---Check whether database is already created or not---
        boolean dbExist = checkDataBase();

        if (!dbExist) {
            this.getReadableDatabase();
            try {
                // ---If not created then copy the database---
                copyDataBase();
            } catch (Exception e) {
                throw new Error("Error " + " database" + e.getMessage());
            }
        }

    }

    private void copyDataBase() throws Exception {

        InputStream myInput = myContext.getAssets().open(
                myContext.getString(R.string.DB_NAME));

        String outFileName = myContext.getString(R.string.DB_PATH)
                + myContext.getString(R.string.DB_NAME);

        OutputStream myOutput = new FileOutputStream(outFileName);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    // --- Check whether database already created or not---
    private boolean checkDataBase() {

        try {
            String myPath = myContext.getString(R.string.DB_PATH)
                    + myContext.getString(R.string.DB_NAME);
            File f = new File(myPath);
            if (f.exists())
                return true;
            else
                return false;

        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }

    }

    @Override
    public synchronized void close() {

        if (myDataBase != null)
            myDataBase.close();

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }

    public long insertItem(String strItemName, String strItemImage, String strItemTag, String strItemComment) {
        long rowId = -2;
        try {
            openDataBase();
            myDataBase = getWritableDatabase();
            ContentValues mContent = new ContentValues();

            mContent.put(COL_ITEMNAME, strItemName);
            mContent.put(COL_ITEMIMAGE, strItemImage);
            mContent.put(COL_ITEMTAG, strItemTag);
            mContent.put(COL_ITEMCOMMENT, strItemComment);

            rowId = myDataBase.insert(TB_BOXITEMS, null, mContent);

            Common.displayLog("RowID>>>", "" + rowId);
            myDataBase.close();
            SQLiteDatabase.releaseMemory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowId;
    }

    public long insertCapturedImagePath(String strCaptured) {
        long rowId = -2;
        try {
            openDataBase();
            myDataBase = getWritableDatabase();
            ContentValues mContent = new ContentValues();
            mContent.put(COL_CAPTURED, strCaptured);
            rowId = myDataBase.insert(TB_CapturedItemImages, null, mContent);
            Common.displayLog("RowID>>>", "" + rowId);
            myDataBase.close();
            SQLiteDatabase.releaseMemory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rowId;
    }

    public ArrayList<ItemDetailsModel> getBoxItems() {
        ArrayList<ItemDetailsModel> al = new ArrayList<>();
        try {
            openDataBase();
            String strFetchItem = "SELECT * FROM " + TB_BOXITEMS;
            Cursor cursor = myDataBase.rawQuery(strFetchItem, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        ItemDetailsModel itemModel = new ItemDetailsModel();
                        String ItemId = cursor.getString(cursor.getColumnIndex(COL_ITEMID));
                        String ItemName = cursor.getString(cursor.getColumnIndex(COL_ITEMNAME));
                        String ItemImage = cursor.getString(cursor.getColumnIndex(COL_ITEMIMAGE));
                        String ItemTag = cursor.getString(cursor.getColumnIndex(COL_ITEMTAG));
                        String ItemComment = cursor.getString(cursor.getColumnIndex(COL_ITEMCOMMENT));

                        itemModel.setStrItemId(ItemId);
                        itemModel.setStrItemName(ItemName);
                        itemModel.setStrItemImage(ItemImage);
                        itemModel.setStrItemTags(ItemTag);
                        itemModel.setStrItemDescription(ItemComment);

                        al.add(itemModel);
                    } while (cursor.moveToNext());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        myDataBase.close();
        SQLiteDatabase.releaseMemory();
        return al;
    }

    public ArrayList<String> getCapturedImagesPath() {
        ArrayList<String> al = new ArrayList<>();
        try {
            openDataBase();
            String strFetchItem = "SELECT * FROM " + TB_CapturedItemImages;
            Cursor cursor = myDataBase.rawQuery(strFetchItem, null);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        String strCapturedImages = cursor.getString(cursor.getColumnIndex(COL_CAPTURED));
                        al.add(strCapturedImages);
                    } while (cursor.moveToNext());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        myDataBase.close();
        SQLiteDatabase.releaseMemory();
        return al;
    }

    public int totalItems() {
        int noItems = 999;
        try {
            openDataBase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String strFetchItem = "SELECT * FROM " + TB_BOXITEMS;
        Cursor cursor = myDataBase.rawQuery(strFetchItem, null);
        noItems = cursor.getCount();
        myDataBase.close();
        SQLiteDatabase.releaseMemory();
        Common.displayLog("Total Items : ", "" + noItems);
        return noItems;
    }

    public void removeBoxItems() {
        long flag = -2;
        try {
            openDataBase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        myDataBase.delete(TB_BOXITEMS, null, null);
        myDataBase.close();
        SQLiteDatabase.releaseMemory();
    }

    public void removeCapturedImages() {
        long flag = -2;
        try {
            openDataBase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        myDataBase.delete(TB_CapturedItemImages, null, null);
        myDataBase.close();
        SQLiteDatabase.releaseMemory();
    }

    public float removeBoxItem(String id) {
        long flag = -2;
        try {
            openDataBase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        flag = myDataBase.delete(TB_BOXITEMS, COL_ITEMID + " = ?", new String[]{id});
        myDataBase.close();
        SQLiteDatabase.releaseMemory();
        return flag;
    }
}

