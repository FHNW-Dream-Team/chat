package com.orbitrondev;

import com.orbitrondev.Controller.*;
import com.orbitrondev.Model.ServerConnectionModel;
import com.orbitrondev.Model.SplashModel;
import com.orbitrondev.View.ServerConnectionView;
import com.orbitrondev.View.SplashView;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainGui extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private SplashView splashView;

    private ServiceLocator serviceLocator;
    private BackendController backend;

    @Override
    public void start(Stage primaryStage) {
        SplashModel splashModel = new SplashModel();
        splashView = new SplashView(primaryStage, splashModel);
        new SplashController(this, splashModel, splashView);
        splashView.start();
        splashModel.initialize();
    }

    public void startApp() {
        serviceLocator = ServiceLocator.getServiceLocator();

        Stage appStage = new Stage();

        if (backend == null) {
            ServerConnectionModel model = new ServerConnectionModel();
            ServerConnectionView view = new ServerConnectionView(appStage, model);
            new ServerConnectionController(model, view);

            splashView.stop();
            splashView = null;
            view.start();
        }
    }
}
