package com.orbitrondev.Model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@DatabaseTable(tableName = "messages")
public class MessageModel {

    /**
     * FIELDS ////////////////////////////////
     */

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private String message;

    @DatabaseField(canBeNull = false)
    private Date timeSent;

    private UserModel user;

    /**
     * CONSTRUCTORS //////////////////////////
     */

    MessageModel() {
        // For ORMLite
        // all persisted classes must define a no-arg constructor
        // with at least package visibility
    }

    public MessageModel(String message, Date timeSent, UserModel user) {
        this.message = message;
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
