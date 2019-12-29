package com.orbitrondev.Controller;

import com.orbitrondev.Abstract.Controller;
import com.orbitrondev.MainGui;
import com.orbitrondev.Model.SplashModel;
import com.orbitrondev.View.SplashView;
import javafx.concurrent.Worker;

public class SplashController extends Controller<SplashModel, SplashView> {
    public SplashController(final MainGui main, SplashModel model, SplashView view) {
        super(model, view);

        view.progress.progressProperty().bind(model.initializer.progressProperty());
        model.initializer.stateProperty().addListener((o, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) main.startApp();
        });
    }
}
