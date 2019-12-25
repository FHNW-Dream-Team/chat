package com.orbitrondev.View;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.orbitrondev.Abstract.View;
import com.orbitrondev.Controller.I18nController;
import com.orbitrondev.Model.LoginsModel;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicBoolean;

public class LoginView extends View<LoginsModel> {
    private VBox errorMessage;
    private JFXTextField username;
    private JFXPasswordField password;
    private JFXButton btnLogin;
    private JFXButton btnRegister;

    public LoginView(Stage stage, LoginsModel model) {
        super(stage, model);
        stage.titleProperty().bind(I18nController.createStringBinding("gui.login.title"));
        stage.setWidth(300);
        stage.setResizable(false);
    }

    @Override
    protected Scene create_GUI() {
        // Create root
        VBox root = new VBox();
        root.getStyleClass().add("background-white");

        // Create body
        VBox body = new VBox();
        body.getStyleClass().add("custom-container");

        // Create error message container
        errorMessage = new VBox();

        // Create username input field
        username = Helper.useTextField("gui.login.username");
        username.getValidators().addAll(
            Helper.useRequiredValidator("gui.login.username.empty")
        );

        // Create password input field
        password = Helper.usePasswordField("gui.login.password");
        password.getValidators().addAll(
            Helper.useRequiredValidator("gui.login.password.empty")
        );

        // Create body
        HBox btnRow = new HBox();
        btnRow.setSpacing(4); // Otherwise the login and register are right beside each other

        // Create button to login
        btnLogin = Helper.usePrimaryButton("gui.login.login");
        btnLogin.setDisable(true);

        // Disable/Enable the login button depending on if the inputs are valid
        AtomicBoolean usernameValid = new AtomicBoolean(false);
        AtomicBoolean passwordValid = new AtomicBoolean(false);
        Runnable updateButtonClickable = () -> {
            if (!usernameValid.get() || !passwordValid.get()) {
                btnLogin.setDisable(true);
            } else {
                btnLogin.setDisable(false);
            }
        };
        username.textProperty().addListener((o, oldVal, newVal) -> {
            if (!oldVal.equals(newVal)) {
                usernameValid.set(username.validate());
                updateButtonClickable.run();
            }
        });
        password.textProperty().addListener((o, oldVal, newVal) -> {
            if (!oldVal.equals(newVal)) {
                passwordValid.set(password.validate());
                updateButtonClickable.run();
            }
        });

        // Create button to register
        btnRegister = Helper.useSecondaryButton("gui.login.register");

        // Add buttons to btnRow
        btnRow.getChildren().addAll(
            btnLogin,
            Helper.useHorizontalSpacer(1),
            btnRegister
        );

        // Add body content to body
        body.getChildren().addAll(
            errorMessage,
            Helper.useSpacer(10),
            username,
            Helper.useSpacer(25),
            password,
            Helper.useSpacer(25),
            btnRow
        );

        // Add body to root
        root.getChildren().addAll(
            Helper.useDefaultMenuBar(),
            Helper.useNavBar("gui.login.title"),
            body
        );

        Scene scene = new Scene(root);
        // https://stackoverflow.com/questions/29962395/how-to-write-a-keylistener-for-javafx
        scene.setOnKeyPressed(event -> {
            // Click the connect button by clicking ENTER
            if (event.getCode() == KeyCode.ENTER) {
                if (!btnLogin.isDisable()) {
                    btnLogin.fire();
                }
            }
        });
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        return scene;
    }

    public VBox getErrorMessage() {
        return errorMessage;
    }

    public JFXTextField getUsername() {
        return username;
    }

    public JFXPasswordField getPassword() {
        return password;
    }

    public JFXButton getBtnLogin() {
        return btnLogin;
    }

    public JFXButton getBtnRegister() {
        return btnRegister;
    }
}
