package com.orbitrondev.Controller;

import com.orbitrondev.Abstract.Controller;
import com.orbitrondev.Model.*;
import com.orbitrondev.View.ChangePasswordView;
import com.orbitrondev.View.DashboardView;
import com.orbitrondev.View.DeleteAccountView;
import com.orbitrondev.View.LoginView;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DashboardController extends Controller<DashboardModel, DashboardView> {
    protected DashboardController(DashboardModel model, DashboardView view) {
        super(model, view);

        // register ourselves to listen for button clicks
        view.getItemChangePassword().setOnMouseClicked(event -> clickOnMenuChangePassword());
        view.getItemDeleteAccount().setOnMouseClicked(event -> clickOnMenuDeleteAccount());
        view.getItemLogout().setOnMouseClicked(event -> clickOnMenuLogout());
        view.getAddChatButton().setOnAction(event -> clickOnAddChat());
        view.getAddContactButton().setOnAction(event -> clickOnAddContact());

        // register ourselves to handle window-closing event
        view.getStage().setOnCloseRequest(event -> Platform.exit());
    }

    private void clickOnMenuChangePassword() {
        Platform.runLater(() -> {
            // Open login window and close server connection window
            Stage appStage = new Stage();
            ChangePasswordModel model = new ChangePasswordModel();
            ChangePasswordView newView = new ChangePasswordView(appStage, model);
            new ChangePasswordController(model, newView);

            view.stop();
            view = null;
            newView.start();
        });
    }

    private void clickOnMenuDeleteAccount() {
        Platform.runLater(() -> {
            // Open login window and close server connection window
            Stage appStage = new Stage();
            DeleteAccountModel model = new DeleteAccountModel();
            DeleteAccountView newView = new DeleteAccountView(appStage, model);
            new DeleteAccountController(model, newView);

            view.stop();
            view = null;
            newView.start();
        });
    }

    private void clickOnMenuLogout() {
        MainModel mainModel;
        if (serviceLocator.getModel() == null) {
            mainModel = new MainModel();
            serviceLocator.setModel(mainModel);
        } else {
            mainModel = serviceLocator.getModel();
        }

        // Connection would freeze window (and the animations) so do it in a different thread.
        Runnable logoutTask = () -> {
            boolean userLoggedOut = false;
            try {
                // Try to logout
                userLoggedOut = serviceLocator.getBackend().sendLogout();
            } catch (IOException e) {
                // This exception contains ConnectException, which basically means, it couldn't connect to the server.
            }

            if (userLoggedOut) {
                mainModel.setCurrentLogin(null);
                Platform.runLater(() -> {
                    // Open login window and close delete account window
                    Stage appStage = new Stage();
                    LoginsModel model = new LoginsModel();
                    LoginView newView = new LoginView(appStage, model);
                    new LoginController(model, newView);

                    view.stop();
                    view = null;
                    newView.start();
                });
            }
        };
        new Thread(logoutTask).start();
    }



    private void clickOnAddChat() {

    }

    private void clickOnAddContact() {
        String username = showAddContactDialogue();
        // "null" means user canceled, length "0" means no user name given, so repeat until right
        while (username != null && username.length() == 0) {
            username = showAddContactDialogue();
        }
        // Stop operation completely if no username was given in the dialogue
        if (username == null) return;

        // Otherwise find the neede model
        UserModel user = null;
        for (UserModel u : serviceLocator.getDb().getUserDao()) {
            if (!u.getUsername().equals(username)) continue;
            user = u;
        }

        // Cancel if we have already added that user
        for (UserModel u : model.getContacts()) {
            if (u.getUsername().equals(username)) {
                showErrorDialogue("gui.dashboard.addContact.error.alreadyAdded.title", "gui.dashboard.addContact.error.alreadyAdded.content");
                return;
            }
        }

        try {
            if (serviceLocator.getBackend().sendUserOnline(serviceLocator.getModel().getCurrentLogin().getToken(), username)) {
                if (user == null) {
                    // Create a new model if we didn't have it yet
                    user = serviceLocator.getDb().getUserOrCreate(username);
                }
                // Set him as a new friend
                user.setFriend(true);
                serviceLocator.getDb().getUserDao().update(user);
                model.getContacts().add(user);
            } else {
                showErrorDialogue("gui.dashboard.addContact.error.offline.title", "gui.dashboard.addContact.error.offline.content");
            }
        } catch (SQLException | IOException e) {
            showErrorDialogue("gui.dashboard.addContact.error.program.title", "gui.dashboard.addContact.error.program.content");
        }
    }

    private String showAddContactDialogue() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.titleProperty().bind(I18nController.createStringBinding("gui.dashboard.addContact.title"));
        dialog.setHeaderText(null);
        dialog.contentTextProperty().bind(I18nController.createStringBinding("gui.dashboard.addContact.content"));
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResource("/images/icon.png").toString())); // Add icon to window

        Optional<String> result = dialog.showAndWait();

        return result.orElse(null);
    }

    private void showErrorDialogue(String translatorKeyTitle, String translatorKeyContent) {
        Alert dialog = new Alert(Alert.AlertType.ERROR);
        dialog.titleProperty().bind(I18nController.createStringBinding(translatorKeyTitle));
        dialog.setHeaderText(null);
        dialog.contentTextProperty().bind(I18nController.createStringBinding(translatorKeyContent));
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResource("/images/icon.png").toString())); // Add icon to window
        dialog.showAndWait();
    }
}
