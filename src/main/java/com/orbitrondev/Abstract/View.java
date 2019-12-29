package com.orbitrondev.Abstract;

import com.orbitrondev.Controller.ServiceLocator;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Copyright 2015, FHNW, Prof. Dr. Brad Richards. All rights reserved. This code
 * is licensed under the terms of the BSD 3-clause license (see the file
 * license.txt).
 * 
 * @author Brad Richards
 */
public abstract class View<M extends Model> {
    protected ServiceLocator serviceLocator;
    protected Stage stage;
    protected Scene scene;
    protected M model;
    
    /**
     * Set any options for the stage in the subclass constructor
     * 
     * @param stage An object containing a Stage
     * @param model An object which extends the Model class
     */
    protected View(Stage stage, M model) {
        this.stage = stage;
        this.model = model;

        serviceLocator = ServiceLocator.getServiceLocator();
        scene = create_GUI(); // Create all controls within "root"
        stage.getIcons().add(new Image(getClass().getResource("/images/icon.png").toString())); // Add icon to window
        stage.setScene(scene);
    }

    protected abstract Scene create_GUI();

    /**
     * Display the view
     */
    public void start() {
        stage.show();
    }
    
    /**
     * Hide the view
     */
    public void stop() {
        stage.hide();
    }
    
    /**
     * Getter for the stage, so that the controller can access window events
     */
    public Stage getStage() {
        return stage;
    }
}
