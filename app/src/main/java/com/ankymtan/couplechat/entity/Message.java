package com.ankymtan.couplechat.entity;

public class Message {

    public static final int TYPE_LOG = 0;
    public static final int TYPE_ACTION = 1;

    public static final int TYPE_MESSAGE_RIGHT = 2;
    public static final int TYPE_MESSAGE_BOTTOM = 3;
    public static final int TYPE_MESSAGE_BOTTOM_RIGHT = 4;

    private int type;
    private String message;
    private String usernameFrom, usernameTo;
    private String GMT;

    private Message() {}

    public int getType() {
        return type;
    };

    public String getMessage() {
        return message;
    };

    public String getUsernameTo(){
        return  usernameTo;
    }

    public String getUsernameFrom() {
        return usernameFrom;
    };

    public String getGMT(){
        return GMT;
    }


    public static class Builder {
        private final int mType;
        private String mUsernameFrom, mUsernameTo;
        private String mMessage;
        private String mGMT;

        public Builder(int type) {
            mType = type;
        }

        public Builder usernameFrom(String username) {
            mUsernameFrom = username;
            return this;
        }

        public Builder usernameTo(String username) {
            mUsernameTo = username;
            return this;
        }

        public Builder message(String message) {
            this.mMessage = message;
            return this;
        }

        public Builder GMT(String GMT){
            this.mGMT = GMT;
            return this;
        }

        public Message build() {
            Message message = new Message();
            message.type = mType;
            message.usernameFrom = mUsernameFrom;
            message.usernameTo = mUsernameTo;
            message.message = mMessage;
            message.GMT = mGMT;
            return message;
        }
    }
}
