package com.orbitrondev;

import com.orbitrondev.Controller.CliController;
import com.orbitrondev.Controller.MainController;
import com.orbitrondev.Model.MainModel;
import com.orbitrondev.View.MainView;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    public static boolean connectToDb = true;

    public static void main(String[] args) {
        boolean openGui = true;
        for (String arg : args) {
            if (arg.equals("--no-gui")) {
                openGui = false;
            }
            if (arg.equals("--no-db")) {
                connectToDb = false;
            }
        }
        if (openGui) {
            launch(args);
        }
        MainModel model = new MainModel();
        new CliController(model);
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
