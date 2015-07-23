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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ankymtan.couplechat.framework.ProfileManager;
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
    private static final int WAITING_NEW_MESSAGE_LENGTH = 500;
    private static final int RETHINKING_TIMER_LENGTH = 5000;
    private static final String LOG_TAG = "by me ";

    public int messageCounter = 0, numberOfHeart = 0, messageType, x, y;
    private RecyclerView mMessagesView;
    public EditText mInputMessageView;
    private List<Message> mMessages = new ArrayList<Message>();
    private RecyclerView.Adapter mAdapter;
    private boolean mTyping = false;
    private Handler mTypingHandler = new Handler();
    private Handler mNegativeMessageHandler = new Handler();
    private Handler mNewMessageNotiHandler = new Handler();
    private String loggedInUsername, currentFriendName;
    private Socket mSocket;
    private PluginManager pluginManager;
    private UserLocal userLocal;
    private android.support.v7.app.ActionBar actionBar;
    private JSONObject jsonNewMessage;
    private SimpleDateFormat dateFormatGMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private TextView tvFriendStatus, tvTest, tvTestAfterAdding;
    private ImageView currentFriendProfile;
    private ProfileManager profileManager;

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
        profileManager = new ProfileManager(activity);
        //prevent keyboard from being shown first time
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        pluginManager = new PluginManager(activity);
        pluginManager.onStart();
        Log.d(LOG_TAG, "onAttach" + loggedInUsername);
        //get screen size
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        x = size.x;
        y = size.y;
        //
        userLocal = new UserLocal(activity);
        loggedInUsername = userLocal.getLoggedInUser().getName();
        currentFriendName = userLocal.getCurrentFriend();
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

        Log.d(LOG_TAG, "onCreate" + loggedInUsername);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreateView" + loggedInUsername);
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
        Log.d(LOG_TAG, "onDestroy" + loggedInUsername);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTest = (TextView) view.findViewById(R.id.tv_test);
        tvTestAfterAdding = (TextView) view.findViewById(R.id.tv_test_after_adding);

        mAdapter = new MessageAdapter(getActivity(), mMessages, tvTest);
        mMessagesView = (RecyclerView) view.findViewById(R.id.messages);
        mMessagesView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mMessagesView.setAdapter(mAdapter);

        mInputMessageView = (EditText) view.findViewById(R.id.message_input);
        mInputMessageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (null == loggedInUsername) return;
                if (!mSocket.connected()) return;

                if (!mTyping) {
                    mTyping = true;
                    mSocket.emit("typing");
                }

                tvTest.setText(mInputMessageView.getText());
                tvTestAfterAdding.setText(mInputMessageView.getText() + "        i");

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
                    return true;
                }
                return false;
            }
        });

        Button sendButton = (Button) view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "button clicked " + loggedInUsername);
                attemptSend();
            }
        });

        //set time zone

        dateFormatGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        currentFriendName = userLocal.getCurrentFriend();
        //if have choose current friend yet then do nothing
        if(currentFriendName == null) return;

        actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.item_current_friend);
        TextView textView = (TextView) actionBar.getCustomView().findViewById(R.id.tv_added_friend_name);
        currentFriendProfile = (ImageView) actionBar.getCustomView().findViewById(R.id.iv_added_friend_profile);

        textView.setText(userLocal.getCurrentFriend());
        tvFriendStatus = (TextView) actionBar.getCustomView().findViewById(R.id.current_friend_status);
        getOnlineStatus();
        updateMessageList();

        profileManager.lazyLoad(currentFriendProfile, userLocal.getCurrentFriend(), false);
    }

    private void addLog(String message) {
        Log.d(LOG_TAG, "b4addLog" + loggedInUsername);
        mMessages.add(new Message.Builder(Message.TYPE_LOG)
                .message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
        Log.d(LOG_TAG, "addLog" + loggedInUsername);
    }


    private void addMessage(Message message) {

        mMessages.add(message);
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
        messageCounter++;
        Log.d(LOG_TAG, "addMess" + loggedInUsername);
    }

    //delete most recent message send by LoggedIn user or advice by system
    private void delPreviousMessage() {
        for (int i = 0; i < 15; i++) {
            Message checkMessage = mMessages.get(mMessages.size() - 1 - i);
            if (checkMessage.getUsernameFrom().equals("system advice")
                    || checkMessage.getUsernameFrom().equals(loggedInUsername)) {
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
        Log.d(LOG_TAG, "addTyping" + loggedInUsername);
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
        Log.d(LOG_TAG, "removeTyping" + loggedInUsername);
    }

    private void attemptSend() {
        if (currentFriendName == null) {
            Log.d(LOG_TAG, currentFriendName+" = null");
            return;
        }
        if (!mSocket.connected()) {
            Log.d(LOG_TAG, "mSocket never connected");
            return;
        }

        mTyping = false;

        String message = mInputMessageView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }

        messageType = getMessageType();

        addMessage(new Message.Builder(messageType).message(message)
                .usernameFrom(loggedInUsername).usernameTo(currentFriendName).GMT(getGMT()).build());
        String adviceMessage = pluginManager.check(message);

        if (adviceMessage != null) {
            addMessage(new Message.Builder(Message.TYPE_MESSAGE_BOTTOM).message(adviceMessage)
                    .usernameFrom("system advice").usernameTo(currentFriendName).GMT(getGMT()).build());
        }

        jsonNewMessage = new JSONObject();
        try {
            jsonNewMessage.put("message", message);
            jsonNewMessage.put("username", loggedInUsername);
            jsonNewMessage.put("currentFriend", currentFriendName);
            jsonNewMessage.put("GMT", getGMT().toString());
            jsonNewMessage.put("type", messageType);
            Log.d(LOG_TAG, loggedInUsername);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(LOG_TAG, "is Message positive: " + pluginManager.isPositive());
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
                    TextView textView = (TextView) actionBar.getCustomView().findViewById(R.id.tv_added_friend_name);
                    textView.setText(currentFriendName);

                    Toast.makeText(getActivity(), "Message cancelled", Toast.LENGTH_SHORT).show();
                }
            });
            return;
        }

        // perform the sending message attempt.
        mSocket.emit("new message", jsonNewMessage.toString());
        //save message to local storage
        userLocal.addMessage(new Message.Builder(messageType).usernameFrom(loggedInUsername)
                .usernameTo(currentFriendName).GMT(getGMT()).message(message).build());
        mInputMessageView.setText("");
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
            Log.d(LOG_TAG, "onConnect ERR" + loggedInUsername);
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            Log.d(LOG_TAG, "b4 on new Mess " + loggedInUsername);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    String time;
                    int type;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                        time = data.getString("GMT");
                        type = data.getInt("type");
                    } catch (JSONException e) {
                        return;
                    }

                    removeTyping(username);

                    //save to local storage
                    userLocal.addMessage(new Message.Builder(type).usernameFrom(username)
                            .usernameTo(loggedInUsername).GMT(time).message(message).build());

                    //if sender is current friend then show message
                    if (username.equals(currentFriendName)) {
                        addMessage(new Message.Builder(type).message(message)
                                .usernameFrom(username).usernameTo(loggedInUsername).GMT(time).build());
                    } else {
                        userLocal.addNewMessageFrom(username);
                        mNewMessageNotiHandler.removeCallbacks(onWaitingNewMessageTimeout);
                        mNewMessageNotiHandler.postDelayed(onWaitingNewMessageTimeout, WAITING_NEW_MESSAGE_LENGTH);
                    }
                }
            });
            Log.d(LOG_TAG, "on new Mess " + loggedInUsername);
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
                    Log.d(LOG_TAG, "onuserjoin" + loggedInUsername);
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
                        Log.d(LOG_TAG, "online status " + json.getBoolean("onlineStatus"));
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
            TextView textView = (TextView) actionBar.getCustomView().findViewById(R.id.tv_added_friend_name);
            textView.setText(currentFriendName);
            //add message to local storage
            String messageContent = "";
            try {
                messageContent = jsonNewMessage.getString("message");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Message message = new Message.Builder(Message.TYPE_MESSAGE_BOTTOM).usernameFrom(loggedInUsername)
                    .usernameTo(currentFriendName).GMT(getGMT()).message(messageContent).build();
            userLocal.addMessage(message);
        }
    };

    public Runnable onWaitingNewMessageTimeout = new Runnable() {
        @Override
        public void run() {
            Toast.makeText(getActivity(), "new message", Toast.LENGTH_SHORT).show();
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
        Log.d(LOG_TAG, "emit username");
    }

    private String getGMT() {
        Log.d(LOG_TAG, dateFormatGMT.format(new Date()));
        return dateFormatGMT.format(new Date());
    }

    private void getOnlineStatus() {
        JSONObject json = new JSONObject();
        try {
            json.put("username", currentFriendName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("online status", json.toString());
    }

    private void updateMessageList() {
        ArrayList<Message> savedMessageList = userLocal.getMessageListWith(currentFriendName);
        mMessages.clear();
        for(Message savedMessage: savedMessageList){
            mMessages.add(savedMessage);
        }
        mAdapter.notifyDataSetChanged();

        Log.d(LOG_TAG, "update message list");
    }

    public int getMessageType(){
        Log.d(LOG_TAG, "tvTest = "+tvTest.getText()+" has "+tvTest.getLineCount());
        Log.d(LOG_TAG, "tvTestAfterAdding = "+tvTestAfterAdding.getText()+" has "+tvTestAfterAdding.getLineCount());

        if(tvTest.getLineCount() == 0 || tvTestAfterAdding.getLineCount() == 0 ){
            return Message.TYPE_MESSAGE_BOTTOM;
        }

        if(tvTest.getLineCount() == 1){
            return Message.TYPE_MESSAGE_RIGHT;
        }

        if(tvTest.getLineCount() == tvTestAfterAdding.getLineCount()){
            return Message.TYPE_MESSAGE_BOTTOM_RIGHT;
        }

        return Message.TYPE_MESSAGE_BOTTOM;
    }
}

