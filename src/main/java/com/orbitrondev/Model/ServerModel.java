package com.orbitrondev.Model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "server")
public class ServerModel {

    /**
     * FIELDS ////////////////////////////////
     */

    @DatabaseField(id = true, generatedId = true)
    private int id;

    @DatabaseField
    private String ip;

    @DatabaseField
    private int port;

    @DatabaseField(defaultValue = "false")
    private boolean secure = false;

    @DatabaseField(defaultValue = "false")
    private boolean defaultServer = false;

    /**
     * CONSTRUCTORS //////////////////////////
     */

    ServerModel() {
        // For ORMLite
        // all persisted classes must define a no-arg constructor
        // with at least package visibility
    }

    public ServerModel(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public ServerModel(String ip, int port, boolean secure) {
        this.ip = ip;
        this.port = port;
        this.secure = secure;
    }

    public ServerModel(String ip, int port, boolean secure, boolean defaultServer) {
        this.ip = ip;
        this.port = port;
        this.secure = secure;
        this.defaultServer = defaultServer;
    }

    /**
     * METHODS ///////////////////////////////
     */

    public int getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public boolean isSecure() {
        return secure;
    }

    public boolean isDefaultServer() {
        return defaultServer;
    }

    public void setDefaultServer(boolean defaultServer) {
        this.defaultServer = defaultServer;
    }
}
