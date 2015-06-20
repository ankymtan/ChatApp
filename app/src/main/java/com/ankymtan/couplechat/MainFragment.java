package com.ankymtan.couplechat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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
import android.view.MenuItem;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * A chat fragment containing messages view and input form.
 */
public class MainFragment extends Fragment {

    private static final int TYPING_TIMER_LENGTH = 600;
    private static final String BY_ME = "by me";
    private int messageCounter = 0, numberOfHeart = 0, numUsers = 0, x, y;
    private Timer timer = new Timer();
    private TimerTask timerTask;
    private RecyclerView mMessagesView;
    private EditText mInputMessageView;
    private List<Message> mMessages = new ArrayList<Message>();
    private RecyclerView.Adapter mAdapter;
    private boolean mTyping = false;
    private Handler mTypingHandler = new Handler();
    private String mUsername;
    private Socket mSocket;
    private PluginManager pluginManager;
    private Backgrounder backgrounder;

    {
        try {
            mSocket = IO.socket("http://chat.socket.io");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public MainFragment() {
        super();
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
        onFragmentAttachedListenner mActivity = (onFragmentAttachedListenner) activity;
        numUsers = mActivity.getNumberUser();
        mUsername = mActivity.getUsername();
        //get screen size
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        x = size.x;
        y = size.y;
        //

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("new message", onNewMessage);
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
                if (getBackgrounder().alpha < 255) {
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

        backgrounder = (Backgrounder) view.findViewById(R.id.background);

        //work for background
        final Runnable backgroundUpdate = new Runnable() {
            public void run() {
                if (messageCounter > 0) {
                    numberOfHeart += messageCounter;
                } else {
                    numberOfHeart -= 2;
                    if (numberOfHeart < 0) {
                        numberOfHeart = 0;
                    }
                    ;
                }
                ;
                //TODO set up when to re-draw: if...then below
                if (mInputMessageView.getBottom() > y / 4 * 3) {
                    backgrounder.update(numberOfHeart);
                }else{
                    Log.d(BY_ME, "no update cuz typing");
                }
                messageCounter = 0;
            }
        };

        timerTask = new TimerTask() {
            public void run() {
                getActivity().runOnUiThread(backgroundUpdate);
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 5000);
        Log.d(BY_ME, "onViewCreated" + mUsername);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_main, menu);
        Log.d(BY_ME, "onCreateOption" + mUsername);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_leave) {
            leave();
            return true;
        }

        Log.d(BY_ME, "onOpselected" + mUsername);
        return super.onOptionsItemSelected(item);
    }

    private void addLog(String message) {
        Log.d(BY_ME, "b4addLog" + mUsername);
        mMessages.add(new Message.Builder(Message.TYPE_LOG)
                .message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
        Log.d(BY_ME, "addLog" + mUsername);
    }

    private void addParticipantsLog(int numUsers) {
        addLog(getResources().getQuantityString(R.plurals.message_participants, numUsers, numUsers));
        Log.d(BY_ME, "addPart..." + mUsername);
    }

    private void addMessage(String username, String message) {

        if (!username.equals("van an") && !username.equals("keong")) return;
        mMessages.add(new Message.Builder(Message.TYPE_MESSAGE)
                .username(username).message(message).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
        messageCounter++;
        Log.d(BY_ME, "addMess" + mUsername);
    }

    private void addTyping(String username) {
        if (!username.equals("van an") && !username.equals("keong")) return;
        mMessages.add(new Message.Builder(Message.TYPE_ACTION)
                .username(username).build());
        mAdapter.notifyItemInserted(mMessages.size() - 1);
        scrollToBottom();
        Log.d(BY_ME, "addTyping" + mUsername);
    }

    private void removeTyping(String username) {
        if (!username.equals("van an") && !username.equals("keong")) return;
        for (int i = mMessages.size() - 1; i >= 0; i--) {
            Message message = mMessages.get(i);
            if (message.getType() == Message.TYPE_ACTION && message.getUsername().equals(username)) {
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
        message = pluginManager.check(message);
        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }

        mInputMessageView.setText("");
        addMessage(mUsername, message);

        // perform the sending message attempt.
        mSocket.emit("new message", message);
    }

    private void leave() {
        Log.d(BY_ME, "leaving");
        mUsername = null;
        mSocket.disconnect();
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
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                    } catch (JSONException e) {
                        return;
                    }

                    removeTyping(username);
                    addMessage(username, message);
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
                    int numUsers;
                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        return;
                    }

                    addLog(getResources().getString(R.string.message_user_joined, username));
                    addParticipantsLog(numUsers);
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
                    int numUsers;
                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        return;
                    }

                    addLog(getResources().getString(R.string.message_user_left, username));
                    addParticipantsLog(numUsers);
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

    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!mTyping) return;

            mTyping = false;
            mSocket.emit("stop typing");
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        backgrounder.onPauseMySurfaceView();
    }

    @Override
    public void onResume() {
        super.onResume();
        backgrounder.onResumeMySurfaceView();
    }

    public Backgrounder getBackgrounder() {
        return backgrounder;
    }
}

