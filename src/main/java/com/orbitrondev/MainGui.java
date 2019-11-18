package com.orbitrondev;

import com.orbitrondev.Controller.MainController;
import com.orbitrondev.Model.MainModel;
import com.orbitrondev.View.MainView;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainGui extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Create and initialize the MVC components
        MainModel model = new MainModel();
        MainView view = new MainView(primaryStage, model);
        new MainController(model, view);
        view.show();
    }
}
