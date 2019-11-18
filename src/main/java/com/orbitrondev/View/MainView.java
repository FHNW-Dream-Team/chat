package com.orbitrondev.View;

import com.orbitrondev.Model.MainModel;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainView {
    private MainModel model;
    private Stage stage;

    public MainView(Stage stage, MainModel model) {
        this.model = model;
        this.stage = stage;

        BorderPane root = new BorderPane();

        Scene scene = new Scene(root);
        //scene.getStylesheets().add(getClass().getResource("/css/poker.css").toExternalForm());

        //this.stage.getIcons().add(new Image(this.getClass().getResource("/images/icon.png").toString())); // Add icon to window

        this.stage.setTitle("Five Card Stud");
        this.stage.setScene(scene);
    }

    public void show() {
        stage.show();
    }
}
