package com.orbitrondev.View;

import com.orbitrondev.Abstract.View;
import com.orbitrondev.Model.DashboardModel;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DashboardView extends View<DashboardModel> {
    public DashboardView(Stage stage, DashboardModel model) {
        super(stage, model);
    }

    @Override
    protected Scene create_GUI() {
        return null;
    }
}
