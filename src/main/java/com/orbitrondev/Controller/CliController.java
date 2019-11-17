package com.orbitrondev.Controller;

import com.orbitrondev.Model.MainModel;
import com.orbitrondev.View.MainView;

public class CliController {
    private MainModel model;
    private MainView view;

    public CliController(MainModel model) {
        this.model = model;
    }
}
