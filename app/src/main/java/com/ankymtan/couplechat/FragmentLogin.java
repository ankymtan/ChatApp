package com.ankymtan.couplechat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.androidchat.R;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URISyntaxException;

/**
 * Created by An on 20/6/2015.
 */
public class FragmentLogin extends Fragment implements View.OnClickListener{


    private static final String BY_ME = "by me";
    public static final String ADDRESS = "http://192.168.0.14:3000";
    //public static final String ADDRESS = "http://chat.socket.io";
    private EditText usernameEt, passwordEt;
    private String mUsername;
    private Socket mSocket;
    private Activity activity;
    private InputMethodManager keyboadManager;

    {
        try {
            mSocket = IO.socket(ADDRESS);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //on purpose of hiding input keyboard
        keyboadManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //setup color for buttons
        Button loginFacebook = (Button) view.findViewById(R.id.login_facebook);
        Button loginGoogle = (Button) view.findViewById(R.id.login_google);
        Button signInButton = (Button) view.findViewById(R.id.login_button);

        loginFacebook.getBackground().setColorFilter(Color.rgb(59, 89, 152), PorterDuff.Mode.SRC);
        loginGoogle.getBackground().setColorFilter(Color.rgb(221, 75, 57), PorterDuff.Mode.SRC);
        signInButton.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC);

        loginFacebook.setTextColor(Color.WHITE);
        loginGoogle.setTextColor(Color.WHITE);
        signInButton.setTextColor(Color.WHITE);
        // Set up the login form.
        usernameEt = (EditText) view.findViewById(R.id.username);
        passwordEt = (EditText) view.findViewById(R.id.password);
        usernameEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        signInButton.setOnClickListener(this);

        mSocket.on("login", onLogin);
        mSocket.connect();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_button:
                Log.d(BY_ME, "button pressed");
                keyboadManager.hideSoftInputFromWindow(usernameEt.getWindowToken(), 0);
                attemptLogin();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.off("login", onLogin);
    }


    /**
     * Attempts to sign in the account specified by the login form.
     * If there are form errors (invalid usernameEt, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Store values at the time of the login attempt.
        String username = usernameEt.getText().toString().trim();

        // Check for a valid usernameEt.
        if (TextUtils.isEmpty(username)) {
            usernameEt.setError(getString(R.string.error_field_required));
            usernameEt.requestFocus();
            return;
        }

        mUsername = username;

        // perform the user login attempt.
        mSocket.emit("add user", username);
    }

    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];

            int numUsers;
            Log.d("BY_ME", "onLogin in Login Activity");
            try {
                numUsers = data.getInt("numUsers");
            } catch (JSONException e) {
                return;
            }

            Intent intent = new Intent();
            intent.putExtra("usernameEt", mUsername);
            intent.putExtra("numUsers", numUsers);
            activity.setResult(Activity.RESULT_OK, intent);
            activity.finish();
        }
    };
}
