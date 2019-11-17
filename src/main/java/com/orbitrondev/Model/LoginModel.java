package com.orbitrondev.Model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "login")
public class LoginModel {

    /** FIELDS ////////////////////////////////*/

    @DatabaseField(id = true, generatedId = true)
    private int id;

    @DatabaseField
    private String username;

    @DatabaseField
    private String password;

    @DatabaseField
    private String token;

    /** CONSTRUCTORS //////////////////////////*/

    LoginModel() {
        // For ORMLite
        // all persisted classes must define a no-arg constructor
        // with at least package visibility
    }

    public LoginModel(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public LoginModel(String username, String password, String token) {
        this.username = username;
        this.password = password;
        this.token = token;
    }

    /** METHODS ///////////////////////////////*/

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
