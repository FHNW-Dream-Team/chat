package com.orbitrondev.Model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A model with all the messages available in all chats (that were caught while being online).
 *
 * @author Manuele Vaccari
 * @version %I%, %G%
 * @since 0.0.1
 */
@DatabaseTable(tableName = "messages")
public class MessageModel {

    /**
     * FIELDS ////////////////////////////////
     */

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private String message;

    @DatabaseField(foreign = true)
    private ChatModel chat;

    @DatabaseField(canBeNull = false)
    private Date timeSent;

    @DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
    private UserModel user;

    /**
     * CONSTRUCTORS //////////////////////////
     */

    MessageModel() {
        // For ORMLite
        // all persisted classes must define a no-arg constructor
        // with at least package visibility
    }

    public MessageModel(String message, ChatModel chat, Date timeSent, UserModel user) {
        this.message = message;
        this.chat = chat;
        this.timeSent = timeSent;
        this.user = user;
    }

    /**
     * METHODS ///////////////////////////////
     */

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public ChatModel getChat() {
        return chat;
    }

    public Date getTimeSent() {
        return timeSent;
    }

    public String getTimeSentFormatted() {
        SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return dateTimeFormatter.format(timeSent);
    }

    public UserModel getUser() {
        return user;
    }
}
