package com.ankymtan.couplechat.framework;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ankymtan.couplechat.entity.Message;
import com.ankymtan.couplechat.entity.User;

import java.util.ArrayList;

/**
 * Created by ankym on 18/7/2015.
 */
public class MyDatabaseHelper extends SQLiteOpenHelper {

    private final static String LOG_TAG = "by me MyDatabaseHelper ";

    private final static String DATABASE_NAME = "dbName";
    private final static int DATABASE_VERSION = 1;

    private final static String TABLE_FRIEND = "friend";
    private final static String TABLE_MESSAGE = "message";

    private final static String FRIEND_NAME = "username";
    private final static String NEW_MESSAGE_COUNTER = "new_message";

    private final static String MESSAGE_FROM = "from";
    private final static String MESSAGE_TO = "to";
    private final static String MESSAGE_GMT = "gmt";
    private final static String MESSAGE_CONTENT = "content";
    private final static String MESSAGE_TYPE = "type";

    private final static String CREATE_TABLE_FRIEND_SQL
            = "CREATE TABLE `friend` (\n" +
            "\t`id` INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "\t`username` TEXT DEFAULT '0',\n" +
            "\t`new_message` INTEGER DEFAULT '0'\n" +
            ")\n";
    private final static String CREATE_TABLE_MESSAGE_SQL
            = "CREATE TABLE `message` (\n" +
            "\t`id` INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
            "\t`from` TEXT DEFAULT '0',\n" +
            "\t`to` TEXT DEFAULT '0',\n" +
            "\t`gmt` TEXT DEFAULT '0',\n" +
            "\t`content` TEXT DEFAULT '0',\n" +
            "\t`type` INTEGER DEFAULT '0'\n" +
            ")\n";



    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_FRIEND_SQL);
        db.execSQL(CREATE_TABLE_MESSAGE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS friend");
        db.execSQL("DROP TABLE IF EXISTS message");

        onCreate(db);
    }

    public void clear(){
        SQLiteDatabase database = getWritableDatabase();
        database.delete(TABLE_FRIEND, null, null);
        database.delete(TABLE_MESSAGE, null, null);
    }

    public void addFriend(User user){
        SQLiteDatabase database = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(FRIEND_NAME, user.getName());
        values.put(NEW_MESSAGE_COUNTER, 0);

        long newRowId = database.insert(TABLE_FRIEND, null, values);
        Log.d(LOG_TAG, "new row id = " + newRowId);
    }

    //set new message counter += 1
    //if friend is new then add friend then try again
    public void addNewMessageFrom(String username){
        SQLiteDatabase readableDatabase = getReadableDatabase();
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues values = new ContentValues();
        String[] columnToRead = {NEW_MESSAGE_COUNTER};

        Cursor c = readableDatabase.query(TABLE_FRIEND, columnToRead, addBackTick(FRIEND_NAME) + " =? ", new String[]{username}, null, null, null);

        if(c == null || !c.moveToFirst()){
            addFriend(new User(username));
            addNewMessageFrom(username);
            c.close();
            return;
        }

        c.moveToFirst();
        int counter = c.getInt(c.getColumnIndex(NEW_MESSAGE_COUNTER));
        counter++;
        c.close();

        values.put(NEW_MESSAGE_COUNTER, counter);
        int count = writableDatabase.update(TABLE_FRIEND, values, addBackTick(FRIEND_NAME)+" = ? ", new String[]{username});
        Log.d(LOG_TAG, "count = " + count);
    }

    public void resetNewMessageCounterFrom(String username){
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(NEW_MESSAGE_COUNTER, 0);
        int count = writableDatabase.update(TABLE_FRIEND, values, "`"+FRIEND_NAME+"`" +" = ?", new String[]{username});
        Log.d(LOG_TAG, "reset new message counter" + count);
    }

    public ArrayList<User> getFriendList(){
        SQLiteDatabase db = getReadableDatabase();
        String[] columnToRead = {FRIEND_NAME, NEW_MESSAGE_COUNTER};
        ArrayList<User> result = new ArrayList<>();

        Cursor c = db.query(TABLE_FRIEND, columnToRead, null, null, null, null, FRIEND_NAME);
        if(c == null || !c.moveToFirst()){
            c.close();
            return result;
        }

        c.moveToFirst();
        result.add(new User(c.getString(c.getColumnIndex(FRIEND_NAME)), c.getInt(c.getColumnIndex(NEW_MESSAGE_COUNTER))));
        while(c.moveToNext()){
            result.add(new User(c.getString(c.getColumnIndex(FRIEND_NAME)), c.getInt(c.getColumnIndex(NEW_MESSAGE_COUNTER))));
        }

        c.close();
        return result;
    }

    public ArrayList<String> getFriendNameList() {
        SQLiteDatabase db = getReadableDatabase();
        String[] columnToRead = {FRIEND_NAME};
        ArrayList<String> result = new ArrayList<>();

        Cursor c = db.query(TABLE_FRIEND, columnToRead, null, null, null, null, FRIEND_NAME);

        if(c == null || !c.moveToFirst()){
            c.close();
            return result;
        }

        c.moveToFirst();
        result.add(c.getString(c.getColumnIndex(FRIEND_NAME)));
        while(c.moveToNext()){
            result.add(c.getString(c.getColumnIndex(FRIEND_NAME)));
        }

        c.close();
        return result;
    }

    public void addMessage(Message message) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(addBackTick(MESSAGE_FROM), message.getUsernameFrom());
        values.put(addBackTick(MESSAGE_TO), message.getUsernameTo());
        values.put(addBackTick(MESSAGE_GMT), message.getGMT());
        values.put(addBackTick(MESSAGE_CONTENT), message.getMessage());
        values.put(addBackTick(MESSAGE_TYPE), message.getType());
        db.insert(TABLE_MESSAGE, null, values);
        Log.d(LOG_TAG, "add Message to local db " + message.getMessage() + " type " + values);
    }

    public ArrayList<Message> getMessageListWith(String currentFriendName){
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Message> result = new ArrayList<>();
        String[] columnToRead = {"`"+MESSAGE_FROM+"`", "`"+MESSAGE_TO+"`",
                "`"+MESSAGE_GMT+"`", "`"+MESSAGE_CONTENT+"`", "`"+MESSAGE_TYPE+"`"};

        Cursor c = db.query(TABLE_MESSAGE, columnToRead, "`"+MESSAGE_FROM+"`" + "=? OR " + "`"+MESSAGE_TO+"`" + "=? ",
                new String[]{currentFriendName, currentFriendName}, null, null, MESSAGE_GMT);

        if(c == null || !c.moveToFirst()){
            c.close();
            return result;
        }

        c.moveToFirst();
        Message message = new Message.Builder(c.getInt(c.getColumnIndex(MESSAGE_TYPE)))
                .GMT(c.getString(c.getColumnIndex(MESSAGE_GMT)))
                .usernameFrom(c.getString(c.getColumnIndex(MESSAGE_FROM)))
                .usernameTo(c.getString(c.getColumnIndex(MESSAGE_TO)))
                .message(c.getString(c.getColumnIndex(MESSAGE_CONTENT)))
                .build();

        Log.d(LOG_TAG, "get Message to local db" + message.getMessage()+ " type "+message.getType());
        result.add(message);

        while (c.moveToNext()){
            Message message2 = new Message.Builder(c.getInt(c.getColumnIndex(MESSAGE_TYPE)))
                    .GMT(c.getString(c.getColumnIndex(MESSAGE_GMT)))
                    .usernameFrom(c.getString(c.getColumnIndex(MESSAGE_FROM)))
                    .usernameTo(c.getString(c.getColumnIndex(MESSAGE_TO)))
                    .message(c.getString(c.getColumnIndex(MESSAGE_CONTENT)))
                    .build();


            Log.d(LOG_TAG, "get Message to local db" + message2.getMessage()+ " type "+message2.getType());
            result.add(message2);
        }

        c.close();
        return result;
    }

    private String addBackTick(String string){
        return "`"+string+"`";
    }
}
