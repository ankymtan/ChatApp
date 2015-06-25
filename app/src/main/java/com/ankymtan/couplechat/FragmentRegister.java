package com.ankymtan.couplechat;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.nkzawa.socketio.androidchat.R;

/**
 * Created by An on 20/6/2015.
 */
public class FragmentRegister extends Fragment implements View.OnClickListener{

    ServerRequest serverRequest;
    String username, password, passwordConfirm, email;
    EditText usernameEt, passwordEt, passwordConfirmEt, emailEt;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        serverRequest = new ServerRequest(getActivity());
        //setup Buttons color, theme
        Button loginFacebook = (Button) view.findViewById(R.id.login_facebook);
        Button loginGoogle = (Button) view.findViewById(R.id.login_google);
        Button signInButton = (Button) view.findViewById(R.id.register_button);

        usernameEt = (EditText) view.findViewById(R.id.username_register);
        emailEt = (EditText) view.findViewById(R.id.email_register);
        passwordEt = (EditText) view.findViewById(R.id.password_register);
        passwordConfirmEt = (EditText) view.findViewById(R.id.password_confirm);

        loginFacebook.getBackground().setColorFilter(Color.rgb(59, 89, 152), PorterDuff.Mode.SRC);
        loginGoogle.getBackground().setColorFilter(Color.rgb(221,75,57), PorterDuff.Mode.SRC);
        signInButton.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC);
        signInButton.setTextColor(Color.WHITE);

        loginFacebook.setTextColor(Color.WHITE);
        loginGoogle.setTextColor(Color.WHITE);

        signInButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.register_button:
                if(!chechInputValidity()) return;
                User user= new User(username, password, email);
                registerNewUser(user);
                break;
        }
    }

    private void registerNewUser(User user){
        ServerRequest serverRequest = new ServerRequest(getActivity());
        serverRequest.storeUserInBackground(user, new serverCallback() {
            @Override
            public void done(User returnedUser) {
                Toast.makeText(getActivity(), "Registered successfully, you can sign in now.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean chechInputValidity(){
        Log.d("by me ", "cheking input validity");
        username = usernameEt.getText().toString().trim();
        email = emailEt.getText().toString();
        password = passwordEt.getText().toString();
        passwordConfirm = passwordConfirmEt.getText().toString();

        if(TextUtils.isEmpty(username)){
            usernameEt.setError(getString(R.string.error_field_required));
            usernameEt.requestFocus();
            return false;
        }
        if(email.isEmpty()){
            emailEt.setError(getString(R.string.error_field_required));
            emailEt.requestFocus();
            return false;
        }
        if(!isEmailValid(email)){
            emailEt.setError("Not an email. Please check again :)");
            emailEt.requestFocus();
            return false;
        }
        if(password.isEmpty()){
            passwordEt.setError(getString(R.string.error_field_required));
            passwordEt.requestFocus();
            return false;
        }
        if(!passwordConfirm.equals(password)){
            passwordConfirmEt.setError("This confirm password is not match");
            passwordConfirmEt.setText(null);
            passwordConfirmEt.requestFocus();
            return false;
        }
        return true;
    }
    //check validity email
    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
