package com.orbitrondev.Controller;

import com.orbitrondev.Abstract.Controller;
import com.orbitrondev.Entity.LoginModel;
import com.orbitrondev.Model.*;
import com.orbitrondev.View.DashboardView;
import com.orbitrondev.View.Helper;
import com.orbitrondev.View.LoginView;
import com.orbitrondev.View.RegisterView;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoginController extends Controller<LoginsModel, LoginView> {
    protected LoginController(LoginsModel model, LoginView view) {
        super(model, view);

        // register ourselves to listen for button clicks
        view.getBtnLogin().setOnAction(event -> clickOnLogin());
        view.getBtnRegister().setOnAction(event -> clickOnRegister());

        // Disable/Enable the login button depending on if the inputs are valid
        AtomicBoolean usernameValid = new AtomicBoolean(false);
        AtomicBoolean passwordValid = new AtomicBoolean(false);
        Runnable updateButtonClickable = () -> {
            if (!usernameValid.get() || !passwordValid.get()) {
                view.getBtnLogin().setDisable(true);
            } else {
                view.getBtnLogin().setDisable(false);
            }
        };
        view.getUsername().textProperty().addListener((o, oldVal, newVal) -> {
            if (!oldVal.equals(newVal)) {
                usernameValid.set(view.getUsername().validate());
                updateButtonClickable.run();
            }
        });
        view.getPassword().textProperty().addListener((o, oldVal, newVal) -> {
            if (!oldVal.equals(newVal)) {
                passwordValid.set(view.getPassword().validate());
                updateButtonClickable.run();
            }
        });

        // register ourselves to handle window-closing event
        view.getStage().setOnCloseRequest(event -> Platform.exit());
    }

    public void disableInputs() {
        view.getUsername().setDisable(true);
        view.getPassword().setDisable(true);
    }

    public void disableAll() {
        disableInputs();
        view.getBtnLogin().setDisable(true);
        view.getBtnRegister().setDisable(true);
    }

    public void enableInputs() {
        view.getUsername().setDisable(false);
        view.getPassword().setDisable(false);
    }

    public void enableAll() {
        enableInputs();
        view.getBtnLogin().setDisable(false);
        view.getBtnRegister().setDisable(false);
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

    private void openRegisterWindow() {
        Platform.runLater(() -> {
            Stage appStage = new Stage();
            RegisterModel model = new RegisterModel();
            RegisterView newView = new RegisterView(appStage, model);
            new RegisterController(model, newView);

            view.stop();
            view = null;
            newView.start();
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

    public void clickOnRegister() {
        openRegisterWindow();
    }

    public void clickOnLogin() {
        // Disable everything to prevent something while working on the data
        disableAll();

        MainModel mainModel;
        if (serviceLocator.getModel() == null) {
            mainModel = new MainModel();
            serviceLocator.setModel(mainModel);
        } else {
            mainModel = serviceLocator.getModel();
        }

        LoginModel login = new LoginModel(view.getUsername().getText(), view.getPassword().getText());
        mainModel.setCurrentLogin(login);

        // Connection would freeze window (and the animations) so do it in a different thread.
        Runnable loginTask = () -> {
            try {
                // Try to login (the BackendController automatically saves it to the DB)
                serviceLocator.getBackend().sendLogin(login);
            } catch (IOException e) {
                // This exception contains ConnectException, which basically means, it couldn't connect to the server.
                enableAll();
                setErrorMessage("gui.login.loginFailed");
            }

            if (login.getToken() != null) {
                openDashboardWindow();
            } else {
                enableAll();
                setErrorMessage("gui.login.loginFailed");
            }
        };
        new Thread(loginTask).start();
    }
}
