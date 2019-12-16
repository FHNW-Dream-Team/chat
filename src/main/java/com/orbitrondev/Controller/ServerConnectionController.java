package com.orbitrondev.Controller;

import com.orbitrondev.Abstract.Controller;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;

public class ServerConnectionController extends Controller<ServerConnectionModel, ServerConnectionView> {
    private static final Logger logger = LogManager.getLogger(ServerConnectionController.class);

    public ServerConnectionController(ServerConnectionModel model, ServerConnectionView view) {
        super(model, view);

        // register ourselves to listen for changes in the dropdown
        view.getChooseServer().setOnAction(event -> updateChosenServer());

        // register ourselves to listen for button clicks
        view.getBtnConnect().setOnAction(event -> buttonClick());

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

    public void buttonClick() {
        // Disable everything to prevent something while working on the data
        disableAll();

        MainModel mainModel = new MainModel();
        serviceLocator.setModel(mainModel);

        ServerModel server = new ServerModel(view.getServerIp().getText(), Integer.parseInt(view.getPort().getText()));
        mainModel.setCurrentServer(server);

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
                    Text text = Helper.useText("gui.serverConnection.connectionFailed");
                    text.setFill(Color.RED);
                    view.getErrorMessage().getChildren().addAll(text, Helper.useSpacer(20));
                    view.getStage().setHeight(view.getStage().getHeight() + 30); // Make window larger, so it doesn't become crammed
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
