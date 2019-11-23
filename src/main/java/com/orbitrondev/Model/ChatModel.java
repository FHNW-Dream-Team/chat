package com.orbitrondev.Model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.orbitrondev.Controller.ServiceLocator;

import java.util.ArrayList;

/**
 * A model for the direct and group chats.
 *
 * @author Manuele Vaccari
 * @version %I%, %G%
 * @since 0.0.1
 */
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

    @DatabaseField(defaultValue = "false", canBeNull = false)
    private boolean admin = false;

    /**
     * CONSTRUCTORS //////////////////////////
     */

    ChatModel() {
        // For ORMLite
        // all persisted classes must define a no-arg constructor
        // with at least package visibility
    }

    public ChatModel(String name, ChatType chatType) {
        this.name = name;
        this.chatType = chatType;
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

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
}
