package com.orbitrondev.Entity;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.orbitrondev.Entity.SupportTables.ChatUserModel;

/**
 * A model with all known (and cached) users.
 *
 * @author Manuele Vaccari
 * @version %I%, %G%
 * @since 0.0.1
 */
@DatabaseTable(tableName = "users")
public class UserModel {

    /**
     * FIELDS ////////////////////////////////
     */

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private String username;

    @DatabaseField(defaultValue = "false", canBeNull = false)
    private boolean online = false;

    @DatabaseField(defaultValue = "false", canBeNull = false)
    private boolean friend = false;

    @DatabaseField(defaultValue = "false", canBeNull = false)
    private boolean blocked = false;

    @ForeignCollectionField
    private ForeignCollection<ChatUserModel> chats;

    /**
     * CONSTRUCTORS //////////////////////////
     */

    UserModel() {
        // For ORMLite
        // all persisted classes must define a no-arg constructor
        // with at least package visibility
    }
    public UserModel(String username) {
        this.username = username;
    }

    public UserModel(String username, boolean online) {
        this.username = username;
        this.online = online;
    }

    /**
     * METHODS ///////////////////////////////
     */

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline() {
        this.online = true;
    }

    public void setOffline() {
        this.online = false;
    }

    public void toggleOnline() {
        this.online = !this.online;
    }

    public boolean isFriend() {
        return friend;
    }

    public void setFriend(boolean friend) {
        this.friend = friend;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    @Override
    public String toString() {
        return username;
    }
}
