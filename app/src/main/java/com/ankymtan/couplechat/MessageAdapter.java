package com.ankymtan.couplechat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.nkzawa.socketio.androidchat.R;

import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private final static String LOG_TAG = "by me messageAdapter ";

    private List<Message> mMessages;
    private Themer themer;
    private UserLocal userLocal;
    private String loggedInUserName;
    private Drawable left, right;
    private TextView tvTest;


    public MessageAdapter(Context context, List<Message> messages, TextView tvTest) {
        mMessages = messages;
        themer = new Themer(context);
        userLocal = new UserLocal(context);

        loggedInUserName = userLocal.getLoggedInUser().getName();
        left = themer.getDrawable("left");
        right = themer.getDrawable("right");

        this.tvTest = tvTest;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = -1;
        switch (viewType) {
            case Message.TYPE_MESSAGE_RIGHT:
                layout = R.layout.item_message_time_right;
                break;
            case Message.TYPE_MESSAGE_BOTTOM:
                layout = R.layout.item_message_time_bottom;
                break;
            case Message.TYPE_MESSAGE_BOTTOM_RIGHT:
                layout = R.layout.item_message_time_bottom_right;
                break;
            case Message.TYPE_LOG:
                layout = R.layout.item_log;
                break;
            case Message.TYPE_ACTION:
                layout = R.layout.item_action;
                break;
            default:
                layout = R.layout.item_log;
                break;
        }
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Message message = mMessages.get(position);
        viewHolder.setMessage(message.getMessage());
        viewHolder.setUsername(message.getUsernameFrom());
        viewHolder.setTime(message.getGMT());

        //set right/left side
        if (message.getType() > message.TYPE_ACTION && message.getUsernameFrom().equals(loggedInUserName)) {
            viewHolder.setRight();
        }else if(message.getType() > message.TYPE_ACTION && message.getUsernameFrom().equals("system advice")) {
            viewHolder.setAdvice();
        }else{
            viewHolder.setLeft();
        }

        if (message.getType() == Message.TYPE_ACTION) {
            viewHolder.setMessage(message.getMessage());
            viewHolder.setUsername(message.getUsernameFrom());
        }
    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mMessages.get(position).getType();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mUsernameView;
        private TextView mMessageView;
        private TextView mTimeView;
        RelativeLayout messageLayout;
        RelativeLayout messageBackground;



        public ViewHolder(View itemView) {
            super(itemView);

            mUsernameView = (TextView) itemView.findViewById(R.id.username);
            mMessageView = (TextView) itemView.findViewById(R.id.message);
            mTimeView = (TextView) itemView.findViewById(R.id.time);
            messageLayout = (RelativeLayout) itemView.findViewById(R.id.message_row);
            messageBackground = (RelativeLayout) itemView.findViewById(R.id.message_background);
        }

        public void setUsername(String username) {
            if (null == mUsernameView) return;
            mUsernameView.setText(username);
        }

        public void setMessage(String message) {
            if (null == mMessageView) return;
            mMessageView.setText(message);
            mMessageView.setGravity(Gravity.LEFT);
        }

        public void setTime(String GMT){
            if(null == mTimeView) return;
            mTimeView.setText(GMT.substring(10,16));
        }

        public void setLeft() {
            if (null == messageLayout) return;
            messageLayout.setGravity(Gravity.LEFT);
            Drawable cloneLeft = left.getConstantState().newDrawable();
            messageBackground.setBackground(cloneLeft);
        }

        public void setRight() {
            if (null == messageLayout) return;
            messageLayout.setGravity(Gravity.RIGHT);
            Drawable cloneRight = right.getConstantState().newDrawable();
            messageBackground.setBackground(cloneRight);
        }

        public void setAdvice() {
            if(null == messageLayout) return;
            messageLayout.setGravity(Gravity.RIGHT);
            messageBackground.setBackgroundResource(R.drawable.orange);
            mMessageView.setTextColor(Color.WHITE);
        }
    }
}
