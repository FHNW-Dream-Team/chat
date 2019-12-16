package com.orbitrondev.View;

import com.orbitrondev.Abstract.View;
import com.orbitrondev.Model.ChangePasswordModel;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ChangePasswordView extends View<ChangePasswordModel> {
    public ChangePasswordView(Stage stage, ChangePasswordModel model) {
        super(stage, model);
    }

    @Override
    protected Scene create_GUI() {
        return null;
    }
}
