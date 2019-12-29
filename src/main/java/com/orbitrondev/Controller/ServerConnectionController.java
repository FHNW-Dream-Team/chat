package com.orbitrondev.Controller;

import com.orbitrondev.Abstract.Controller;
import com.orbitrondev.Entity.ServerModel;
import com.orbitrondev.Exception.InvalidIpException;
import com.orbitrondev.Exception.InvalidPortException;
import com.orbitrondev.Model.*;
import com.orbitrondev.View.Helper;
import com.orbitrondev.View.LoginView;
import com.orbitrondev.View.ServerConnectionView;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerConnectionController extends Controller<ServerConnectionModel, ServerConnectionView> {
    private static final Logger logger = LogManager.getLogger(ServerConnectionController.class);

    public ServerConnectionController(ServerConnectionModel model, ServerConnectionView view) {
        super(model, view);

        // register ourselves to listen for changes in the dropdown
        view.getChooseServer().setOnAction(event -> updateChosenServer());

        // register ourselves to listen for button clicks
        view.getBtnConnect().setOnAction(event -> clickOnConnect());

        // Add options to server list drop down
        view.getChooseServer().setConverter(new StringConverter<ServerModel>() {
            @Override
            public String toString(ServerModel server) {
                return
                    server == null || server.getIp() == null
                        ? I18nController.get("gui.serverConnection.create")
                        : (
                        server.isSecure()
                            ? I18nController.get("gui.serverConnection.entry.ssl", server.getIp(), Integer.toString(server.getPort()))
                            : I18nController.get("gui.serverConnection.entry", server.getIp(), Integer.toString(server.getPort()))
                    );
            }

            @Override
            public ServerModel fromString(String string) {
                return null;
            }
        });
        view.getChooseServer().getItems().add(new ServerModel(null, 0));
        view.getChooseServer().getSelectionModel().selectFirst();
        for (ServerModel server : serviceLocator.getDb().getServerDao()) {
            view.getChooseServer().getItems().add(server);
        }

        // Disable/Enable the Connect button depending on if the inputs are valid
        AtomicBoolean serverIpValid = new AtomicBoolean(false);
        AtomicBoolean portValid = new AtomicBoolean(false);
        Runnable updateButtonClickable = () -> {
            if (!serverIpValid.get() || !portValid.get()) {
                view.getBtnConnect().setDisable(true);
            } else {
                view.getBtnConnect().setDisable(false);
            }
        };
        view.getServerIp().textProperty().addListener((o, oldVal, newVal) -> {
            if (!oldVal.equals(newVal)) {
                serverIpValid.set(view.getServerIp().validate());
                updateButtonClickable.run();
            }
        });
        view.getPort().textProperty().addListener((o, oldVal, newVal) -> {
            if (!oldVal.equals(newVal)) {
                portValid.set(view.getPort().validate());
                updateButtonClickable.run();
            }
        });

        // register ourselves to handle window-closing event
        view.getStage().setOnCloseRequest(event -> Platform.exit());
    }

    public void updateChosenServer() {
        ServerModel server = view.getChooseServer().getSelectionModel().getSelectedItem();

        if (server == null || server.getIp() == null) {
            enableInputs();
            view.getServerIp().setText("");
            view.getPort().setText("");
        } else {
            disableInputs();
            view.getServerIp().setText(server.getIp());
            view.getPort().setText(Integer.toString(server.getPort()));
        }
    }

    public void disableInputs() {
        view.getServerIp().setDisable(true);
        view.getPort().setDisable(true);
    }

    public void disableAll() {
        disableInputs();
        view.getBtnConnect().setDisable(true);
    }

    public void enableInputs() {
        view.getServerIp().setDisable(false);
        view.getPort().setDisable(false);
    }

    public void enableAll() {
        enableInputs();
        view.getBtnConnect().setDisable(false);
    }

    public void clickOnConnect() {
        // Disable everything to prevent something while working on the data
        disableAll();

        ServerModel server = new ServerModel(view.getServerIp().getText(), Integer.parseInt(view.getPort().getText()));
        serviceLocator.setCurrentServer(server);

        // Connection would freeze window (and the animations) so do it in a different thread.
        Runnable connect = () -> {
            BackendController backend = null;
            try {
                // Try to connect to the server
                backend = new BackendController(server.getIp(), server.getPort());
                serviceLocator.setBackend(backend);
            } catch (InvalidIpException | InvalidPortException e) {
                // Ignore exceptions, because we made sure about the variables beforehand.
            } catch (IOException e) {
                // This exception contains ConnectException, which basically means, it couldn't connect to the server.
                enableAll();
                Platform.runLater(() -> {

                    if (view.getErrorMessage().getChildren().size() == 0) {
                        // Make window larger, so it doesn't become crammed, only if we haven't done so yet
                        view.getStage().setHeight(view.getStage().getHeight() + 30);
                    }
                    Text text = Helper.useText("gui.serverConnection.connectionFailed");
                    text.setFill(Color.RED);
                    view.getErrorMessage().getChildren().clear();
                    view.getErrorMessage().getChildren().addAll(text, Helper.useSpacer(20));
                });
            }

            if (backend != null) {
                // If the user selected "Create new connection" add it to the DB
                ServerModel selectedItem = view.getChooseServer().getSelectionModel().getSelectedItem();
                if (selectedItem != null && selectedItem.getIp() == null) {
                    try {
                        serviceLocator.getDb().getServerDao().create(server);
                    } catch (SQLException e) {
                        logger.error("Server connection not saved to database");
                    }
                }

                Platform.runLater(() -> {
                    // Open login window and close server connection window
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
        new Thread(connect).start();
    }
}
