package com.orbitrondev;

import com.orbitrondev.Controller.CliController;
import com.orbitrondev.Controller.MainController;
import com.orbitrondev.Model.MainModel;

public class Main {
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
            MainGui.main(args);
        } else {
            MainModel model = new MainModel();
            new CliController(model);
        }
    }
}
