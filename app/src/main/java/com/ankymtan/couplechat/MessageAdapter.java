package com.ankymtan.couplechat;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.nkzawa.socketio.androidchat.R;

import java.util.List;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private List<Message> mMessages;
    private Themer themer;
    private UserLocal userLocal;

    public MessageAdapter(Context context, List<Message> messages) {
        mMessages = messages;
        themer = new Themer(context);
        userLocal = new UserLocal(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = -1;
        switch (viewType) {
            case Message.TYPE_MESSAGE:
                layout = R.layout.item_message;
                break;
            case Message.TYPE_LOG:
                layout = R.layout.item_log;
                break;
            case Message.TYPE_ACTION:
                layout = R.layout.item_action;
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
        if (message.getType() == message.TYPE_MESSAGE && message.getUsernameFrom().equals(userLocal.getLoggedInUser().getName())) {
            viewHolder.setRight();
        }else if(message.getType() == message.TYPE_MESSAGE && message.getUsernameFrom().equals("system advice")) {
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
        LinearLayout messageLayout;
        LinearLayout messageBackground;

        private Drawable left, right;

        public ViewHolder(View itemView) {
            super(itemView);

            left = themer.getDrawable("left");
            right = themer.getDrawable("right");

            mUsernameView = (TextView) itemView.findViewById(R.id.username);
            mMessageView = (TextView) itemView.findViewById(R.id.message);
            mTimeView = (TextView) itemView.findViewById(R.id.time);
            messageLayout = (LinearLayout) itemView.findViewById(R.id.message_row);
            messageBackground = (LinearLayout) itemView.findViewById(R.id.message_background);
        }

        public void setUsername(String username) {
            if (null == mUsernameView) return;
            mUsernameView.setText(username);
        }

        public void setMessage(String message) {
            if (null == mMessageView) return;
            mMessageView.setText(message);
            mMessageView.setGravity(Gravity.RIGHT);
        }

        public void setTime(String GMT){
            if(null == mTimeView) return;
            mTimeView.setText(GMT.substring(10,16));
        }

        public void setLeft() {
            if (null == messageLayout) return;
            messageLayout.setGravity(Gravity.LEFT);
            messageBackground.setBackground(left);
        }

        public void setRight() {
            if (null == messageLayout) return;
            messageLayout.setGravity(Gravity.RIGHT);
            messageBackground.setBackground(right);
        }

        public void setAdvice() {
            if(null == messageLayout) return;
            messageLayout.setGravity(Gravity.RIGHT);
            messageBackground.setBackgroundResource(R.drawable.orange);
            mMessageView.setTextColor(Color.WHITE);
        }
    }
}
