package com.ankymtan.couplechat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.androidchat.R;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


/**
 * A chat fragment containing messages view and input form.
 */
public class FragmentMain extends Fragment {

    private static final int TYPING_TIMER_LENGTH = 600;
    private static final int RETHINKING_TIMER_LENGTH = 5000;
    private static final String BY_ME = "by me";
    public int messageCounter = 0, numberOfHeart = 0, x, y;
    private RecyclerView mMessagesView;
    public EditText mInputMessageView;
    private List<Message> mMessages = new ArrayList<Message>();
    private RecyclerView.Adapter mAdapter;
    private boolean mTyping = false;
    private Handler mTypingHandler = new Handler();
    private Handler mNegativeMessageHandler = new Handler();
    private String mUsername;
    private Socket mSocket;
    private PluginManager pluginManager;
    private UserLocal userLocal;
    private android.support.v7.app.ActionBar actionBar;
    private JSONObject jsonNewMessage;
    private SimpleDateFormat dateFormatGMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private TextView tvFriendStatus;

    {
        try {
            mSocket = IO.socket(FragmentLogin.ADDRESS);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //prevent keyboard from being shown first time
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        pluginManager = new PluginManager(activity);
        mAdapter = new MessageAdapter(activity, mMessages);
        pluginManager.onStart();
        Log.d(BY_ME, "onAttach" + mUsername);
        //get screen size
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        x = size.x;
        y = size.y;
        //
        userLocal = new UserLocal(activity);
        mUsername = userLocal.getLoggedInUser().getName();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("new message", onNewMessage);
        mSocket.on("online status", onUpdateOnlineStatus);
        //mSocket.on("user joined", onUserJoined);
        //mSocket.on("user left", onUserLeft);
        mSocket.on("typing", onTyping);
        mSocket.on("stop typing", onStopTyping);
        mSocket.connect();

        Log.d(BY_ME, "onCreate" + mUsername);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(BY_ME, "onCreateView" + mUsername);
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("new message", onNewMessage);
        mSocket.off("online status", onUpdateOnlineStatus);
        //mSocket.off("user joined", onUserJoined);
        //mSocket.off("user left", onUserLeft);
        mSocket.off("typing", onTyping);
        mSocket.off("stop typing", onStopTyping);

        pluginManager.onStop();
        Log.d(BY_ME, "onDestroy" + mUsername);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMessagesView = (RecyclerView) view.findViewById(R.id.messages);
        mMessagesView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMessagesView.setAdapter(mAdapter);

        mInputMessageView = (EditText) view.findViewById(R.id.message_input);
        mInputMessageView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == R.id.send || id == EditorInfo.IME_NULL) {
                    attemptSend();
                    return true;
                }
                return false;
            }
        });
        mInputMessageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (null == mUsername) return;
                if (!mSocket.connected()) return;

                if (!mTyping) {
                    mTyping = true;
                    mSocket.emit("typing");
                }

                mTypingHandler.removeCallbacks(onTypingTimeout);
                mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        mInputMessageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (((ActivityMain) getActivity()).getBackground().onAnimation()) {
                    Log.d(BY_ME, "block cuz drawing");
                    mInputMessageView.setInputType(EditorInfo.TYPE_NULL);
                } else {
                    mInputMessageView.setInputType(EditorInfo.TYPE_CLASS_TEXT);
                }
                return false;
            }
        });

        Button sendButton = (Button) view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(BY_ME, "button clicked " + mUsername);
                attemptSend();
            }
        });

        //set time zone

        dateFormatGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.item_current_friend);
        TextView textView = (TextView) actionBar.getCustomView().findViewById(R.id.current_friend_name);
        textView.setText(userLocal.getCurrentFriend());
        tvFriendStatus = (TextView) actionBar.getCustomView().findViewById(R.id.current_friend_status);
        getOnlineStatus();
        updateMessageList();
    }

    private void addLog(String message) {
        Log.d(BY_ME, "b4addLog" + mUsername);
        mMessages.add(new Message.Builder(Message.TYPE_LOG)
                .message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
        Log.d(BY_ME, "addLog" + mUsername);
    }


    private void addMessage(String usernameFrom, String usernameTo, String message, String GMT) {

        mMessages.add(new Message.Builder(Message.TYPE_MESSAGE)
                .usernameFrom(usernameFrom).usernameTo(usernameTo).message(message).GMT(GMT).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
        messageCounter++;
        Log.d(BY_ME, "addMess" + mUsername);
    }

    //delete most recent message send by LoggedIn user or advice by system
    private void delPreviousMessage() {
        for (int i = 0; i < 15; i++) {
            Message checkMessage = mMessages.get(mMessages.size() - 1 - i);
            if (checkMessage.getUsernameFrom().equals("system advice")
                    || checkMessage.getUsernameFrom().equals(userLocal.getLoggedInUser().getName())) {
                mMessages.remove(mMessages.size() - 1 - i);
                mAdapter.notifyDataSetChanged();
                return;
            }
        }
    }

    private void addTyping(String username) {
        if (!username.equals("van an") && !username.equals("keong")) return;
        mMessages.add(new Message.Builder(Message.TYPE_ACTION)
                .usernameFrom(username).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
        Log.d(BY_ME, "addTyping" + mUsername);
    }

    private void removeTyping(String username) {
        if (!username.equals("van an") && !username.equals("keong")) return;
        for (int i = mMessages.size() - 1; i >= 0; i--) {
            Message message = mMessages.get(i);
            if (message.getType() == Message.TYPE_ACTION && message.getUsernameFrom().equals(username)) {
                mMessages.remove(i);
                mAdapter.notifyItemRemoved(i);
            }
        }
        Log.d(BY_ME, "removeTyping" + mUsername);
    }

    private void attemptSend() {
        if (mUsername == null) {
            Log.d(BY_ME, "attem to send 1" + mUsername);
            return;
        }
        if (!mSocket.connected()) {
            Log.d(BY_ME, "mSocket never connected");
            return;
        }

        mTyping = false;

        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }

        mInputMessageView.setText("");
        addMessage(userLocal.getLoggedInUser().getName(), userLocal.getCurrentFriend(), message, getGMT());
        String adviceMessage = pluginManager.check(message);

        if (adviceMessage != null) {
            addMessage("system advice", userLocal.getCurrentFriend(), adviceMessage, getGMT());
        }

        jsonNewMessage = new JSONObject();
        try {
            jsonNewMessage.put("message", message);
            jsonNewMessage.put("username", userLocal.getLoggedInUser().getName());
            jsonNewMessage.put("currentFriend", userLocal.getCurrentFriend());
            jsonNewMessage.put("GMT", getGMT().toString());
            Log.d(BY_ME, userLocal.getLoggedInUser().getName());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(BY_ME, "is Message positive: " + pluginManager.isPositive());
        //when recognized a negative mess
        if (!pluginManager.isPositive()) {
            mNegativeMessageHandler.postDelayed(onRethinkingTimeout, RETHINKING_TIMER_LENGTH);
            //TODO display new actionbar
            actionBar.setCustomView(R.layout.item_confirm_send_actionbar);
            TextView textView = (TextView) actionBar.getCustomView().findViewById(R.id.waiting_message);
            textView.setText(message);

            Button send = (Button) actionBar.getCustomView().findViewById(R.id.bt_still_send);
            send.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC);
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //if user still want to send = timeout for rethinking
                    mNegativeMessageHandler.removeCallbacks(onRethinkingTimeout);
                    mNegativeMessageHandler.post(onRethinkingTimeout);
                }
            });

            Button cancel = (Button) actionBar.getCustomView().findViewById(R.id.bt_cancel_message);
            cancel.getBackground().setColorFilter(Color.rgb(221, 75, 57), PorterDuff.Mode.SRC);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //cancel = remove callback
                    //and delete displayed message
                    //and change back actionbar
                    mNegativeMessageHandler.removeCallbacks(onRethinkingTimeout);
                    //del system advice + user mess
                    delPreviousMessage();
                    delPreviousMessage();

                    actionBar.setCustomView(R.layout.item_current_friend);
                    TextView textView = (TextView) actionBar.getCustomView().findViewById(R.id.current_friend_name);
                    textView.setText(userLocal.getCurrentFriend());

                    Toast.makeText(getActivity(), "Message cancelled", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        // perform the sending message attempt.
        mSocket.emit("new message", jsonNewMessage.toString());
        //save message to local storage
        userLocal.addMessage(new Message.Builder(Message.TYPE_MESSAGE).usernameFrom(userLocal.getLoggedInUser().getName())
                .usernameTo(userLocal.getCurrentFriend()).GMT(getGMT()).message(message).build());
    }


    private void scrollToBottom() {
        mMessagesView.scrollToPosition(mAdapter.getItemCount() - 1);
    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity().getApplicationContext(),
                            R.string.error_connect, Toast.LENGTH_LONG).show();
                }
            });
            Log.d(BY_ME, "onConnect ERR" + mUsername);
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d(BY_ME, "b4 on new Mess " + mUsername);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    String time;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                        time = data.getString("GMT");
                    } catch (JSONException e) {
                        return;
                    }

                    removeTyping(username);

                    //save to local storage
                    userLocal.addMessage(new Message.Builder(Message.TYPE_MESSAGE).usernameFrom(username)
                            .usernameTo(userLocal.getLoggedInUser().getName()).GMT(time).message(message).build());

                    //if sender is current friend then show message
                    if (username.equals(userLocal.getCurrentFriend())) {
                        addMessage(username, mUsername, message, time);
                    } else {
                        //if username is new in friendlsit then add to friend list
                        if(!userLocal.getFriendNameList().contains(username)){
                            userLocal.addFriend(new User(username));
                        }
                        userLocal.addNewMessageFrom(username);
                        Toast.makeText(getActivity(), "new message from " + username, Toast.LENGTH_LONG).show();
                    }
                }
            });
            Log.d(BY_ME, "on new Mess " + mUsername);
        }
    };

    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        return;
                    }
                    addLog(getResources().getString(R.string.message_user_joined, username));
                    Log.d(BY_ME, "onuserjoin" + mUsername);
                }
            });
        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        return;
                    }

                    addLog(getResources().getString(R.string.message_user_left, username));
                    removeTyping(username);
                }
            });
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        return;
                    }
                    addTyping(username);
                }
            });
        }
    };

    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        return;
                    }
                    removeTyping(username);
                }
            });
        }
    };

    private Emitter.Listener onUpdateOnlineStatus = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final JSONObject json = (JSONObject) args[0];
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (json.getBoolean("onlineStatus")) {
                            tvFriendStatus.setText("Online");
                        } else {
                            tvFriendStatus.setText("Offline");
                        }
                        Log.d(BY_ME, "online status " + json.getBoolean("onlineStatus"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!mTyping) return;

            mTyping = false;
            mSocket.emit("stop typing");
        }
    };

    private Runnable onRethinkingTimeout = new Runnable() {
        @Override
        public void run() {
            mSocket.emit("new message", jsonNewMessage.toString());
            Toast.makeText(getActivity(), "Message sent", Toast.LENGTH_LONG).show();
            //TODO change back actionbar
            actionBar.setCustomView(R.layout.item_current_friend);
            TextView textView = (TextView) actionBar.getCustomView().findViewById(R.id.current_friend_name);
            textView.setText(userLocal.getCurrentFriend());
            //add message to local storage
            String messageContent = "";
            try {
                messageContent = jsonNewMessage.getString("message");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Message message = new Message.Builder(Message.TYPE_MESSAGE).usernameFrom(userLocal.getLoggedInUser().getName())
                    .usernameTo(userLocal.getCurrentFriend()).GMT(getGMT()).message(messageContent).build();
            userLocal.addMessage(message);
        }
    };

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        JSONObject json = new JSONObject();
        try {
            json.put("username", userLocal.getLoggedInUser().getName());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("username", json.toString());
        Log.d(BY_ME, "emit username");
    }

    private String getGMT() {
        Log.d(BY_ME, dateFormatGMT.format(new Date()));
        return dateFormatGMT.format(new Date());
    }

    private void getOnlineStatus() {
        if (userLocal.getCurrentFriend() == null) return;
        JSONObject json = new JSONObject();
        try {
            json.put("username", userLocal.getCurrentFriend());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("online status", json.toString());
    }

    private void updateMessageList() {
        ArrayList<Message> savedMessageList = userLocal.getMessageListWith(userLocal.getCurrentFriend());
        mMessages.clear();
        for(Message savedMessage: savedMessageList){
            mMessages.add(savedMessage);
        }
        mAdapter.notifyDataSetChanged();

        Log.d(BY_ME, "update message list");
    }
}

