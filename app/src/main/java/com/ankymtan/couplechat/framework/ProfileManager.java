package com.ankymtan.couplechat.framework;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.ankymtan.couplechat.fragment.FragmentLogin;
import com.github.nkzawa.socketio.androidchat.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.koushikdutta.ion.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.concurrent.Future;

/**
 * Created by ankym on 15/7/2015.
 */
public class ProfileManager {

    private final String LOG_TAG = "by me ";
    private Context context;
    private UserLocal userLocal;

    public ProfileManager(Context context){
        this.context = context;
        userLocal = new UserLocal(context);
    }

    public void upload(String path){
        File originalFile = new File(path);
        File croppedFile = new File(context.getFilesDir(), healTheWords(userLocal.getLoggedInUser().getName()) + ".jpg");

        cropFile(originalFile, croppedFile);

        Ion.with(context)
                .load(FragmentLogin.ADDRESS+"/upload")
                .setMultipartFile("image", croppedFile)
                .asString()
                .withResponse()
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> result) {
                        try {
                            JSONObject jobj = new JSONObject(result.getResult());
                            Toast.makeText(context, jobj.getString("response"), Toast.LENGTH_SHORT).show();

                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }

                    }
                });
    }

    public void download(String username){
        File newFile = new File(context.getFilesDir(), username+".jpg");
        OutputStream out = null;
        try {
            out = new FileOutputStream(newFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Future downloading = Ion.with(context)
                .load(FragmentLogin.ADDRESS + "/profile/" + username + ".jpg")
                .progressHandler(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {

                    }
                })
                .write(out)
                .setCallback(new FutureCallback<OutputStream>() {
                    @Override
                    public void onCompleted(Exception e, OutputStream result) {

                    }
                });
    }

    public void lazyLoad(final ImageView imageView, final String username, final boolean isSecondCall){

        Log.d(LOG_TAG, username + " lazy load get call");
        final String URL = FragmentLogin.ADDRESS + "/profile/" + healTheWords(username) + ".jpg";
        File file = new File(context.getFilesDir(), healTheWords(username) +".jpg");

        //check if this file is already downloaded
        if(file.exists() && file.length() > 10000){
            Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
            Drawable drawable = new BitmapDrawable(context.getResources(), bm);
            Log.d(LOG_TAG, username+ " avatar is loading. size = "+file.length());
            imageView.setBackground(drawable);
            return;
        }else{
            imageView.setBackground(context.getResources().getDrawable(R.drawable.profile_empty));
            file.delete();
        }

        //check if this time called after download (second call)
        if(isSecondCall) return;

        File file2 = new File(context.getFilesDir(), healTheWords(username) +".jpg");
        //if not yet then download and display
        OutputStream out = null;
        try {
            out = new FileOutputStream(file2);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Ion.with(context)
                .load(URL)
                .progressHandler(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {

                    }
                })
                .write(out)
                .setCallback(new FutureCallback<OutputStream>() {
                    @Override
                    public void onCompleted(Exception e, OutputStream result) {
                            lazyLoad(imageView, username, true);
                    }
                });
    }


    private void cropFile(File srcFile, File dstFile){
        Bitmap srcBitmap = decodeSampledBitmapFromResource(srcFile,  500, 500);

        //crop bitmap
        int squareLength = Math.min(srcBitmap.getWidth(), srcBitmap.getHeight());
        Bitmap dstBitmap = Bitmap.createBitmap(
                srcBitmap,
                srcBitmap.getWidth()/2 - squareLength/2,
                srcBitmap.getHeight()/2 -squareLength/2,
                squareLength,
                squareLength
        );

        //resize
        dstBitmap = Bitmap.createScaledBitmap(dstBitmap, 200,200,false);

        //save to file
        try {
            FileOutputStream out = new FileOutputStream(dstFile);
            dstBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        dstBitmap.recycle();
        srcBitmap.recycle();
    }

    //if the limited is 1000x1000 but image size is 2000x2000 then inSampleSize must be 2 at least
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(File file,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    //replace space in original string with '_'
    private String healTheWords(String string){
        return  string.replace(" ", "_");
    }
}
