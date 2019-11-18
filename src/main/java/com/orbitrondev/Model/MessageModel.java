package com.orbitrondev.Model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    private LocalDateTime timeSent;

    private UserModel user;

    /**
     * CONSTRUCTORS //////////////////////////
     */

    MessageModel() {
        // For ORMLite
        // all persisted classes must define a no-arg constructor
        // with at least package visibility
    }

    public MessageModel(String message, LocalDateTime timeSent, UserModel user) {
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

    public LocalDateTime getTimeSent() {
        return timeSent;
    }

    public String getTimeSentFormatted() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return this.timeSent.format(dateTimeFormatter);
    }

    public UserModel getUser() {
        return user;
    }
}
