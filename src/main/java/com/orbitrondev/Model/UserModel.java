package com.orbitrondev.Model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "cached_users")
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

    /**
     * CONSTRUCTORS //////////////////////////
     */

    UserModel() {
        // For ORMLite
        // all persisted classes must define a no-arg constructor
        // with at least package visibility
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
}
