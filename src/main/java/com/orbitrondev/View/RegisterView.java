package com.orbitrondev.View;

import com.orbitrondev.Abstract.View;
import com.orbitrondev.Model.RegisterModel;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class RegisterView extends View<RegisterModel> {
    public RegisterView(Stage stage, RegisterModel model) {
        super(stage, model);
    }

    @Override
    protected Scene create_GUI() {
        return null;
    }
}
