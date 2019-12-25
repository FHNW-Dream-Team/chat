package com.orbitrondev.View;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.orbitrondev.Abstract.View;
import com.orbitrondev.Controller.I18nController;
import com.orbitrondev.Model.ChangePasswordModel;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicBoolean;

public class ChangePasswordView extends View<ChangePasswordModel> {
    private VBox errorMessage;
    private JFXPasswordField oldPassword;
    private JFXPasswordField newPassword;
    private JFXPasswordField repeatNewPassword;
    private JFXButton btnChange;
    private JFXButton btnCancel;

    public ChangePasswordView(Stage stage, ChangePasswordModel model) {
        super(stage, model);
        stage.titleProperty().bind(I18nController.createStringBinding("gui.changePassword.title"));
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

        // Create old password input field
        oldPassword = Helper.usePasswordField("gui.changePassword.oldPassword");
        oldPassword.getValidators().addAll(
            Helper.useRequiredValidator("gui.changePassword.oldPassword.empty")
        );

        // Create password input field
        newPassword = Helper.usePasswordField("gui.changePassword.newPassword");
        newPassword.getValidators().addAll(
            Helper.useRequiredValidator("gui.changePassword.newPassword.empty")
        );

        repeatNewPassword = Helper.usePasswordField("gui.changePassword.repeatNewPassword");
        repeatNewPassword.getValidators().addAll(
            Helper.useRequiredValidator("gui.changePassword.repeatNewPassword.empty"),
            Helper.useIsSameValidator(newPassword, "gui.changePassword.repeatNewPassword.notSame")
        );

        // Create body
        HBox btnRow = new HBox();
        btnRow.setSpacing(4); // Otherwise the login and register are right beside each other

        // Create button to register
        btnChange = Helper.usePrimaryButton("gui.changePassword.change");
        btnChange.setDisable(true);

        // Disable/Enable the login button depending on if the inputs are valid
        AtomicBoolean oldPasswordValid = new AtomicBoolean(false);
        AtomicBoolean newPasswordValid = new AtomicBoolean(false);
        AtomicBoolean repeatNewPasswordValid = new AtomicBoolean(false);
        Runnable updateButtonClickable = () -> {
            if (!oldPasswordValid.get() || !newPasswordValid.get() || !repeatNewPasswordValid.get()) {
                btnChange.setDisable(true);
            } else {
                btnChange.setDisable(false);
            }
        };
        oldPassword.textProperty().addListener((o, oldVal, newVal) -> {
            if (!oldVal.equals(newVal)) {
                oldPasswordValid.set(oldPassword.validate());
                updateButtonClickable.run();
            }
        });
        newPassword.textProperty().addListener((o, oldVal, newVal) -> {
            if (!oldVal.equals(newVal)) {
                newPasswordValid.set(newPassword.validate());
                updateButtonClickable.run();
            }
        });
        repeatNewPassword.textProperty().addListener((o, oldVal, newVal) -> {
            if (!oldVal.equals(newVal)) {
                repeatNewPasswordValid.set(repeatNewPassword.validate());
                updateButtonClickable.run();
            }
        });

        // Create button to change
        btnCancel = Helper.useSecondaryButton("gui.changePassword.cancel");

        // Add buttons to btnRow
        btnRow.getChildren().addAll(
            btnChange,
            Helper.useHorizontalSpacer(1),
            btnCancel
        );

        // Add body content to body
        body.getChildren().addAll(
            errorMessage,
            Helper.useSpacer(10),
            oldPassword,
            Helper.useSpacer(25),
            newPassword,
            Helper.useSpacer(25),
            repeatNewPassword,
            Helper.useSpacer(25),
            btnRow
        );

        // Add body to root
        root.getChildren().addAll(
            Helper.useDefaultMenuBar(),
            Helper.useNavBar("gui.changePassword.title"),
            body
        );

        Scene scene = new Scene(root);
        // https://stackoverflow.com/questions/29962395/how-to-write-a-keylistener-for-javafx
        scene.setOnKeyPressed(event -> {
            // Click the connect button by clicking ENTER
            if (event.getCode() == KeyCode.ENTER) {
                if (!btnChange.isDisable()) {
                    btnChange.fire();
                }
            }
        });
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        return scene;
    }

    public VBox getErrorMessage() {
        return errorMessage;
    }

    public JFXPasswordField getOldPassword() {
        return oldPassword;
    }

    public JFXPasswordField getNewPassword() {
        return newPassword;
    }

    public JFXPasswordField getRepeatNewPassword() {
        return repeatNewPassword;
    }

    public JFXButton getBtnChange() {
        return btnChange;
    }

    public JFXButton getBtnCancel() {
        return btnCancel;
    }
}
