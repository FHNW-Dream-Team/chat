package com.orbitrondev.View;

import com.orbitrondev.Abstract.View;
import com.orbitrondev.Model.DeleteAccountModel;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DeleteAccountView extends View<DeleteAccountModel> {
    public DeleteAccountView(Stage stage, DeleteAccountModel model) {
        super(stage, model);
    }

    @Override
    protected Scene create_GUI() {
        return null;
    }
}
