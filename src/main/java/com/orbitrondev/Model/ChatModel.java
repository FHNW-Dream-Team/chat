package com.orbitrondev.Model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.orbitrondev.Model.Chat.ChatType;

import java.util.ArrayList;

@DatabaseTable(tableName = "chats")
public class ChatModel {

    /**
     * FIELDS ////////////////////////////////
     */

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private String name;

    private ArrayList<MessageModel> messages;
    private ArrayList<UserModel> members;

    @DatabaseField(canBeNull = false)
    protected ChatType chatType;

    @DatabaseField(defaultValue = "false", canBeNull = false)
    private boolean archived = false;

    /**
     * CONSTRUCTORS //////////////////////////
     */

    ChatModel() {
        // For ORMLite
        // all persisted classes must define a no-arg constructor
        // with at least package visibility
    }

    public ChatModel(String name) {
        this.name = name;
    }

    /**
     * METHODS ///////////////////////////////
     */

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<MessageModel> getMessages() {
        return messages;
    }

    public void addMessage(MessageModel message) {
        this.messages.add(message);
    }

    public ArrayList<UserModel> getMembers() {
        return members;
    }

    public void addMembers(UserModel user) {
        this.members.add(user);
    }

    public ChatType getChatType() {
        return chatType;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}
