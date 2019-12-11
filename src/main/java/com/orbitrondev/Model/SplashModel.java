package com.orbitrondev.Model;

import com.orbitrondev.Abstract.Model;
import com.orbitrondev.Controller.DatabaseController;
import com.orbitrondev.Controller.ServiceLocator;
import com.orbitrondev.Main;
import javafx.concurrent.Task;

import java.util.ArrayList;

public class SplashModel extends Model {

    ServiceLocator serviceLocator;

    public final Task<Void> initializer = new Task<Void>() {
        @Override
        protected Void call() {
            final int MAX_LOOP_COUNT = 100000000;

            // Create the service locator to hold our resources
            serviceLocator = ServiceLocator.getServiceLocator();

            // List of all tasks
            ArrayList<Runnable> tasks = new ArrayList<>();
            tasks.add(() -> serviceLocator.setDb(new DatabaseController(Main.dbLocation)));

            // First, take some time, update progress
            int i = 0;
            for (; i < MAX_LOOP_COUNT; i++) {
                if (i == MAX_LOOP_COUNT / 10) {
                    // Initialize the resources in the service locator
                    new Thread(tasks.get(0)).start();
                }
                if ((i % 1000000) == 0) {
                    this.updateProgress(i, MAX_LOOP_COUNT);
                }
            }
            return null;
        }
    };

    public void initialize() {
        new Thread(initializer).start();
    }
}
