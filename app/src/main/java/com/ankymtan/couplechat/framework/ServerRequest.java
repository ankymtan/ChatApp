package com.ankymtan.couplechat.framework;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.ankymtan.couplechat.entity.User;
import com.ankymtan.couplechat.fragment.FragmentLogin;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

/**
 * Created by ankym on 23/6/2015.
 */
public class ServerRequest {

    ProgressDialog progressDialog;
    private Socket mSocket;

    {
        try {
            mSocket = IO.socket(FragmentLogin.ADDRESS);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public ServerRequest(Context context){
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Connecting...");
        progressDialog.setMessage("Just a moment...");
    }

    public void storeUserInBackground(User user,serverCallback serverCallback){
        progressDialog.show();
        new StoraUserAsyncTask(user, serverCallback).execute();
    }

    public void fetchUserInBackground(User user, serverCallback serverCallback){

    }

    public class StoraUserAsyncTask extends AsyncTask<Void, Void, Void> {
        User user;
        serverCallback serverCallback;
        public StoraUserAsyncTask(User user, serverCallback serverCallback) {
            super();
            this.user = user;
            this.serverCallback = serverCallback;
        }

        @Override
        protected Void doInBackground(Void... params) {
            JSONObject userData = new JSONObject();
            try {
                userData.put("username", user.getName());
                userData.put("email", user.getEmail());
                userData.put("password", user.getPassword());
                mSocket.emit("register", userData.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
            serverCallback.done(null);
        }

    }
}
