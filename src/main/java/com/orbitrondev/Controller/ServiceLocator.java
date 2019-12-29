package com.orbitrondev.Controller;

import com.orbitrondev.Entity.LoginModel;
import com.orbitrondev.Entity.ServerModel;

public class ServiceLocator {
    private static ServiceLocator serviceLocator; // singleton

    // Resources
    private DatabaseController db = null;
    private BackendController backend = null;
    private ServerModel currentServer = null;
    private LoginModel currentLogin = null;

    /**
     * Factory method for returning the singleton
     */
    public static ServiceLocator getServiceLocator() {
        if (serviceLocator == null) {
            serviceLocator = new ServiceLocator();
        }
        return serviceLocator;
    }

    /**
     * Private constructor, because this class is a singleton
     */
    private ServiceLocator() {
    }

    // Database
    public DatabaseController getDb() {
        return db;
    }

    public void setDb(DatabaseController db) {
        this.db = db;
    }

    // Backend
    public BackendController getBackend() {
        return backend;
    }

    public void setBackend(BackendController backend) {
        this.backend = backend;
    }

    // Server
    public ServerModel getCurrentServer() {
        return currentServer;
    }

    public void setCurrentServer(ServerModel currentServer) {
        this.currentServer = currentServer;
    }

    // Login
    public LoginModel getCurrentLogin() {
        return currentLogin;
    }

    public void setCurrentLogin(LoginModel currentLogin) {
        this.currentLogin = currentLogin;
    }
}
