package com.orbitrondev.View;

import com.orbitrondev.Abstract.View;
import com.orbitrondev.Model.LoginsModel;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoginView extends View<LoginsModel> {
    public LoginView(Stage stage, LoginsModel model) {
        super(stage, model);
    }

    @Override
    protected Scene create_GUI() {
        return null;
    }
}
