package com.orbitrondev.View;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.orbitrondev.Abstract.View;
import com.orbitrondev.Controller.I18nController;
import com.orbitrondev.Model.RegisterModel;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegisterView extends View<RegisterModel> {
    private VBox errorMessage;
    private JFXTextField username;
    private JFXPasswordField password;
    private JFXPasswordField repeatPassword;
    private JFXButton btnRegister;
    private JFXButton btnLogin;

    public RegisterView(Stage stage, RegisterModel model) {
        super(stage, model);
        stage.titleProperty().bind(I18nController.createStringBinding("gui.register.title"));
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
        username = Helper.useTextField("gui.register.username");
        username.getValidators().addAll(
            Helper.useRequiredValidator("gui.register.username.empty")
        );

        // Create password input field
        password = Helper.usePasswordField("gui.register.password");
        password.getValidators().addAll(
            Helper.useRequiredValidator("gui.register.password.empty")
        );

        repeatPassword = Helper.usePasswordField("gui.register.repeatPassword");
        repeatPassword.getValidators().addAll(
            Helper.useRequiredValidator("gui.register.repeatPassword.empty"),
            Helper.useIsSameValidator(password, "gui.register.repeatPassword.notSame")
        );

        // Create body
        HBox btnRow = new HBox();
        btnRow.setSpacing(4); // Otherwise the login and register are right beside each other

        // Create button to register
        btnRegister = Helper.usePrimaryButton("gui.register.register");
        btnRegister.setDisable(true);

        // Create button to login
        btnLogin = Helper.useSecondaryButton("gui.register.login");

        // Add buttons to btnRow
        btnRow.getChildren().addAll(
            btnRegister,
            Helper.useHorizontalSpacer(1),
            btnLogin
        );

        // Add body content to body
        body.getChildren().addAll(
            errorMessage,
            Helper.useSpacer(10),
            username,
            Helper.useSpacer(25),
            password,
            Helper.useSpacer(25),
            repeatPassword,
            Helper.useSpacer(25),
            btnRow
        );

        // Add body to root
        root.getChildren().addAll(
            Helper.useDefaultMenuBar(),
            Helper.useNavBar("gui.register.title"),
            body
        );

        Scene scene = new Scene(root);
        // https://stackoverflow.com/questions/29962395/how-to-write-a-keylistener-for-javafx
        scene.setOnKeyPressed(event -> {
            // Click the connect button by clicking ENTER
            if (event.getCode() == KeyCode.ENTER) {
                if (!btnRegister.isDisable()) {
                    btnRegister.fire();
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

    public JFXPasswordField getRepeatPassword() {
        return repeatPassword;
    }

    public JFXButton getBtnRegister() {
        return btnRegister;
    }

    public JFXButton getBtnLogin() {
        return btnLogin;
    }
}
