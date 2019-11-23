package com.orbitrondev.Controller;

import com.orbitrondev.Model.MainModel;

public class ServiceLocator {
    private static ServiceLocator serviceLocator; // singleton
    // Resources

    private DatabaseController db;
    private MainModel model;

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

    public DatabaseController getDb() {
        return db;
    }

    public void setDb(DatabaseController db) {
        this.db = db;
    }

    public MainModel getModel() {
        return model;
    }

    public void setModel(MainModel model) {
        this.model = model;
    }
}
