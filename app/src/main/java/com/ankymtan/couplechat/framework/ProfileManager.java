package com.ankymtan.couplechat.framework;

import android.content.Context;
import android.widget.Toast;

import com.ankymtan.couplechat.FragmentLogin;
import com.ankymtan.couplechat.UserLocal;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
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
