package com.orbitrondev.Controller;

import com.orbitrondev.Abstract.Controller;
import com.orbitrondev.Model.*;
import com.orbitrondev.View.ChangePasswordView;
import com.orbitrondev.View.DashboardView;
import com.orbitrondev.View.Helper;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChangePasswordController extends Controller<ChangePasswordModel, ChangePasswordView> {
    protected ChangePasswordController(ChangePasswordModel model, ChangePasswordView view) {
        super(model, view);

        // register ourselves to listen for button clicks
        view.getBtnChange().setOnAction(event -> clickOnChange());
        view.getBtnCancel().setOnAction(event -> clickOnCancel());

        // Disable/Enable the login button depending on if the inputs are valid
        AtomicBoolean oldPasswordValid = new AtomicBoolean(false);
        AtomicBoolean newPasswordValid = new AtomicBoolean(false);
        AtomicBoolean repeatNewPasswordValid = new AtomicBoolean(false);
        Runnable updateButtonClickable = () -> {
            if (!oldPasswordValid.get() || !newPasswordValid.get() || !repeatNewPasswordValid.get()) {
                view.getBtnChange().setDisable(true);
            } else {
                view.getBtnChange().setDisable(false);
            }
        };
        view.getOldPassword().textProperty().addListener((o, oldVal, newVal) -> {
            if (!oldVal.equals(newVal)) {
                oldPasswordValid.set(view.getOldPassword().validate());
                updateButtonClickable.run();
            }
        });
        view.getNewPassword().textProperty().addListener((o, oldVal, newVal) -> {
            if (!oldVal.equals(newVal)) {
                newPasswordValid.set(view.getNewPassword().validate());
                updateButtonClickable.run();
            }
        });
        view.getRepeatNewPassword().textProperty().addListener((o, oldVal, newVal) -> {
            if (!oldVal.equals(newVal)) {
                repeatNewPasswordValid.set(view.getRepeatNewPassword().validate());
                updateButtonClickable.run();
            }
        });

        // register ourselves to handle window-closing event
        view.getStage().setOnCloseRequest(event -> Platform.exit());
    }

    public void disableInputs() {
        view.getOldPassword().setDisable(true);
        view.getNewPassword().setDisable(true);
        view.getRepeatNewPassword().setDisable(true);
    }

    public void disableAll() {
        disableInputs();
        view.getBtnChange().setDisable(true);
        view.getBtnCancel().setDisable(true);
    }

    public void enableInputs() {
        view.getOldPassword().setDisable(false);
        view.getNewPassword().setDisable(false);
        view.getRepeatNewPassword().setDisable(false);
    }

    public void enableAll() {
        enableInputs();
        view.getBtnChange().setDisable(false);
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

    private void openDashboardWindow() {
        Platform.runLater(() -> {
            Stage appStage = new Stage();
            DashboardModel model = new DashboardModel();
            DashboardView newView = new DashboardView(appStage, model);
            new DashboardController(model, newView);

            view.stop();
            view = null;
            newView.start();
        });
    }

    public void clickOnChange() {
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
        Runnable changePasswordTask = () -> {
            LoginModel login = mainModel.getCurrentLogin();
            LoginModel newLogin = new LoginModel(login.getUsername(), view.getNewPassword().getText(), login.getToken());
            boolean passwordChanged = false;
            try {
                // Try to login (the BackendController automatically saves it to the DB)
                passwordChanged = serviceLocator.getBackend().sendChangePassword(login.getToken(), newLogin.getPassword());
            } catch (IOException e) {
                // This exception contains ConnectException, which basically means, it couldn't connect to the server.
                enableAll();
                setErrorMessage("gui.changePassword.changeFailed");
            }

            if (passwordChanged) {
                mainModel.setCurrentLogin(newLogin);
                openDashboardWindow();
            } else {
                enableAll();
                setErrorMessage("gui.changePassword.changeFailed");
            }
        };
        new Thread(changePasswordTask).start();
    }

    public void clickOnCancel() {
        openDashboardWindow();
    }
}
