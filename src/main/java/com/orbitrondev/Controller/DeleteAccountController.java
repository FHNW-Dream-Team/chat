package com.orbitrondev.Controller;

import com.orbitrondev.Abstract.Controller;
import com.orbitrondev.Model.*;
import com.orbitrondev.View.DashboardView;
import com.orbitrondev.View.DeleteAccountView;
import com.orbitrondev.View.Helper;
import com.orbitrondev.View.LoginView;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class DeleteAccountController extends Controller<DeleteAccountModel, DeleteAccountView> {
    protected DeleteAccountController(DeleteAccountModel model, DeleteAccountView view) {
        super(model, view);

        // register ourselves to listen for button clicks
        view.getBtnDelete().setOnAction(event -> clickOnDelete());

        // register ourselves to listen for button clicks
        view.getBtnCancel().setOnAction(event -> clickOnCancel());

        // register ourselves to handle window-closing event
        view.getStage().setOnCloseRequest(event -> Platform.exit());
    }

    public void disableAll() {
        view.getBtnDelete().setDisable(true);
        view.getBtnCancel().setDisable(true);
    }

    public void enableAll() {
        view.getBtnDelete().setDisable(false);
        view.getBtnCancel().setDisable(false);
    }

    public void setErrorMessage(String translatorKey) {
        Platform.runLater(() -> {
            if (view.getErrorMessage().getChildren().size() == 0) {
                // Make window larger, so it doesn't become crammed, only if we haven't done so yet
                view.getStage().setHeight(view.getStage().getHeight() + 30);
            }
            Text text = Helper.useText(translatorKey);
            text.setFill(Color.RED);
            view.getErrorMessage().getChildren().clear();
            view.getErrorMessage().getChildren().addAll(text, Helper.useSpacer(20));
        });
    }

    public void clickOnDelete() {
        // Disable everything to prevent something while working on the data
        disableAll();

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
                enableAll();
                setErrorMessage("gui.deleteAccount.logoutFailed");
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
            } else {
                enableAll();
                setErrorMessage("gui.deleteAccount.logoutFailed");
            }
        };
        Runnable deleteTask = () -> {
            boolean userDeleted = false;
            try {
                // Try to delete the account
                userDeleted = serviceLocator.getBackend().sendDeleteLogin(serviceLocator.getModel().getCurrentLogin().getToken());
            } catch (IOException e) {
                // This exception contains ConnectException, which basically means, it couldn't connect to the server.
                enableAll();
                setErrorMessage("gui.deleteAccount.deleteFailed");
            }

            if (userDeleted) {
                // If deleted, try logging out now.
                new Thread(logoutTask).start();
            } else {
                enableAll();
                setErrorMessage("gui.deleteAccount.deleteFailed");
            }
        };
        new Thread(deleteTask).start();
    }

    public void clickOnCancel() {
        Platform.runLater(() -> {
            // Open dashboard window and close delete account window
            Stage appStage = new Stage();
            DashboardModel model = new DashboardModel();
            DashboardView newView = new DashboardView(appStage, model);
            new DashboardController(model, newView);

            view.stop();
            view = null;
            newView.start();
        });
    }
}
