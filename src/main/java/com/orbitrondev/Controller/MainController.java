package com.orbitrondev.Controller;

import com.orbitrondev.Model.MainModel;
import com.orbitrondev.View.MainView;

public class MainController {
    private MainModel model;
    private MainView view;

    public MainController(MainModel model, MainView view) {
        this.model = model;
        this.view = view;
    }
}
