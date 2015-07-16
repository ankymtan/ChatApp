package com.ankymtan.couplechat.framework;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.Toast;

import com.ankymtan.couplechat.FragmentLogin;
import com.ankymtan.couplechat.UserLocal;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.koushikdutta.ion.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
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
        File renamedFile = new File(context.getFilesDir(), userLocal.getLoggedInUser().getName() + ".jpg");

        copy(originalFile, renamedFile);

        Future uploading = Ion.with(context)
                .load(FragmentLogin.ADDRESS+"/upload")
                .setMultipartFile("image", renamedFile)
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
        final String URL = FragmentLogin.ADDRESS + "/profile/" + username + ".jpg";
        File file = new File(context.getFilesDir(), username+".jpg");

        //check if this file is already downloaded
        if(file.exists() && file.length() > 0){
            Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
            Drawable drawable = new BitmapDrawable(context.getResources(), bm);
            imageView.setBackground(drawable);
            return;
        }else{
            file.delete();
        }

        //check if this time called after download (second call)
        if(isSecondCall) return;

        //if not yet then download and display
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
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

    //void onDownloadCompleted(Exception e, OutputStream result);

    private void copy(File src, File dst){
        try {

            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dst);

            //transfer bytes from file in to out
            byte[] buf = new byte[1024];
            int len;
            while((len = in.read(buf))>0){
                out.write(buf, 0, len);
            }

            in.close();;
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
