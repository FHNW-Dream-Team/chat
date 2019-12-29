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
            // Create the service locator to hold our resources
            serviceLocator = ServiceLocator.getServiceLocator();

            // List of all tasks
            ArrayList<Runnable> tasks = new ArrayList<>();
            tasks.add(() -> serviceLocator.setDb(new DatabaseController(Main.dbLocation))); // Initialize the db connection in the service locator

            // First, take some time, update progress
            this.updateProgress(1, tasks.size() + 1); // Start the progress bar with 1 instead of 0
            for (int i = 0; i < tasks.size(); i++) {
                tasks.get(i).run();
                this.updateProgress(i + 2, tasks.size() + 1);
            }
            // For better UX, let the user see the full progress bar
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // Ignore
            }
            return null;
        }
    };

    public void initialize() {
        new Thread(initializer).start();
    }
}
