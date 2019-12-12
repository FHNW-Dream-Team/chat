package com.orbitrondev.View;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.orbitrondev.Abstract.View;
import com.orbitrondev.Controller.I18nController;
import com.orbitrondev.Model.ServerConnectionModel;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicBoolean;

public class ServerConnectionView extends View<ServerConnectionModel> {
    private VBox errorMessage;
    private JFXTextField serverIp;
    private JFXTextField port;
    private JFXButton btnConnect;

    public ServerConnectionView(Stage stage, ServerConnectionModel model) {
        super(stage, model);
        stage.titleProperty().bind(I18nController.createStringBinding("gui.serverConnection.title"));
        stage.setResizable(false);
    }

    @Override
    protected Scene create_GUI() {
        // Create root
        VBox root = new VBox();
        root.getStyleClass().add("background-white");

        // Create body
        VBox body = new VBox();
        body.getStyleClass().add("container");

        // Create error message container
        errorMessage = new VBox();

        // Create server ip input field
        serverIp = Helper.useTextField("gui.serverConnection.ip");
        serverIp.getValidators().addAll(
            Helper.useRequiredValidator("gui.serverConnection.ip.empty"),
            Helper.useIsValidIpValidator("gui.serverConnection.ip.notIp")
        );

        // Create server port input field
        port = Helper.useTextField("gui.serverConnection.port");
        port.getValidators().addAll(
            Helper.useRequiredValidator("gui.serverConnection.port.empty"),
            Helper.useIsIntegerValidator("gui.serverConnection.port.nan"),
            Helper.useIsValidPortValidator("gui.serverConnection.port.outOfRange")
        );

        // Create button to connect
        btnConnect = Helper.usePrimaryButton("gui.serverConnection.connect");
        btnConnect.setDisable(true);

        // Disable/Enable the Connect button depending on if the inputs are valid
        AtomicBoolean serverIpValid = new AtomicBoolean(false);
        AtomicBoolean portValid = new AtomicBoolean(false);
        Runnable updateButtonClickable = () -> {
            if (!serverIpValid.get() || !portValid.get()) {
                btnConnect.setDisable(true);
            } else {
                btnConnect.setDisable(false);
            }
        };
        serverIp.textProperty().addListener((o, oldVal, newVal) -> {
            if (!oldVal.equals(newVal)) {
                serverIpValid.set(serverIp.validate());
                updateButtonClickable.run();
            }
        });
        port.textProperty().addListener((o, oldVal, newVal) -> {
            if (!oldVal.equals(newVal)) {
                portValid.set(port.validate());
                updateButtonClickable.run();
            }
        });

        // Add body content to body
        body.getChildren().addAll(
            errorMessage,
            Helper.useSpacer(10),
            serverIp,
            Helper.useSpacer(25),
            Helper.useText("gui.serverConnection.ip.hint"),
            Helper.useSpacer(15),
            port,
            Helper.useSpacer(25),
            Helper.useText("gui.serverConnection.port.hint"),
            Helper.useSpacer(15),
            btnConnect
        );

        // Add body to root
        root.getChildren().addAll(
            Helper.useNavBar("gui.serverConnection.title"),
            body
        );

        Scene scene = new Scene(root);
        // https://stackoverflow.com/questions/29962395/how-to-write-a-keylistener-for-javafx
        scene.setOnKeyPressed(event -> {
            // Click the connect button by clicking ENTER
            if (event.getCode() == KeyCode.ENTER) {
                if (!btnConnect.isDisable()) {
                    btnConnect.fire();
                }
            }
        });
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        return scene;
    }

    public VBox getErrorMessage() {
        return errorMessage;
    }

    public JFXTextField getServerIp() {
        return serverIp;
    }

    public JFXTextField getPort() {
        return port;
    }

    public JFXButton getBtnConnect() {
        return btnConnect;
    }
}
