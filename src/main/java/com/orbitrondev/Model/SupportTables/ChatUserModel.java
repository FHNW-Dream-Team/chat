package com.orbitrondev.Model.SupportTables;

import com.j256.ormlite.field.DatabaseField;
import com.orbitrondev.Model.ChatModel;
import com.orbitrondev.Model.UserModel;

public class ChatUserModel {

    /**
     * FIELDS ////////////////////////////////
     */

    @DatabaseField(id = true, generatedId = true)
    int id;

    // This is a foreign object which just stores the id from the User object in this table.
    @DatabaseField(foreign = true, canBeNull = false)
    private UserModel user;

    // This is a foreign object which just stores the id from the Post object in this table.
    @DatabaseField(foreign = true, canBeNull = false)
    private ChatModel chat;

    /**
     * CONSTRUCTORS //////////////////////////
     */

    ChatUserModel() {
        // For ORMLite
        // all persisted classes must define a no-arg constructor
        // with at least package visibility
    }

    public ChatUserModel(UserModel user, ChatModel chat) {
        this.user = user;
        this.chat = chat;
    }

    /**
     * METHODS ///////////////////////////////
     */

    public UserModel getUser() {
        return user;
    }

    public ChatModel getChat() {
        return chat;
    }
}
