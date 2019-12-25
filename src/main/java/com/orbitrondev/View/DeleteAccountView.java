package com.orbitrondev.View;

import com.jfoenix.controls.JFXButton;
import com.orbitrondev.Abstract.View;
import com.orbitrondev.Controller.I18nController;
import com.orbitrondev.Model.DeleteAccountModel;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DeleteAccountView extends View<DeleteAccountModel> {
    private VBox errorMessage;
    private JFXButton btnDelete;
    private JFXButton btnCancel;

    public DeleteAccountView(Stage stage, DeleteAccountModel model) {
        super(stage, model);
        stage.titleProperty().bind(I18nController.createStringBinding("gui.deleteAccount.title"));
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

        // Create body
        HBox btnRow = new HBox();
        btnRow.setSpacing(4); // Otherwise the login and register are right beside each other

        // Create button to register
        btnDelete = Helper.usePrimaryButton("gui.deleteAccount.delete");

        // Create button to change
        btnCancel = Helper.useSecondaryButton("gui.deleteAccount.cancel");

        // Add buttons to btnRow
        btnRow.getChildren().addAll(
            btnDelete,
            Helper.useHorizontalSpacer(1),
            btnCancel
        );

        // Add body content to body
        body.getChildren().addAll(
            errorMessage,
            Helper.useSpacer(10),
            Helper.useText("gui.deleteAccount.message", stage),
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
                if (!btnDelete.isDisable()) {
                    btnDelete.fire();
                }
            }
        });
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        return scene;
    }

    public VBox getErrorMessage() {
        return errorMessage;
    }

    public JFXButton getBtnDelete() {
        return btnDelete;
    }

    public JFXButton getBtnCancel() {
        return btnCancel;
    }
}
