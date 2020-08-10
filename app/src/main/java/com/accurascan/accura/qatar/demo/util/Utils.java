package com.accurascan.accura.qatar.demo.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Created by richa on 27/4/17.
 */

public class Utils {


    public static void hideKeyboard(Activity context) {
        // Check if no view has focus
        View view = context.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static File fileFromBitmap(Context context, Bitmap bitmap, String filename) {
        //create a file to write bitmap data
        File f = new File(context.getExternalCacheDir(), filename);
        try {
            if (f.exists()) {
                f.delete();
            }
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bitmapdata = bos.toByteArray();

        //write the bytes in file
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return f;
    }

    public static Uri getOutputMediaFilee(Context context) {
        File mediaStorageDir = new File(context.getFilesDir(), "Temp");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(System.currentTimeMillis());
        File file = null;
        try {
            file = File.createTempFile(
                    "IMG_",  // prefix
                    ".jpg",         // suffix
                    mediaStorageDir      // directory
            );
            return Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
            file = new File(mediaStorageDir, "IMG_" + timeStamp + ".jpg");
            return Uri.fromFile(file);
        }

    }

}