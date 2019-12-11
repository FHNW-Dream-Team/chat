package com.orbitrondev.Controller;

import com.orbitrondev.Abstract.Controller;
import com.orbitrondev.Exception.InvalidIpException;
import com.orbitrondev.Exception.InvalidPortException;
import com.orbitrondev.Model.MainModel;
import com.orbitrondev.Model.ServerConnectionModel;
import com.orbitrondev.Model.ServerModel;
import com.orbitrondev.View.Helper;
import com.orbitrondev.View.ServerConnectionView;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.io.IOException;

public class ServerConnectionController extends Controller<ServerConnectionModel, ServerConnectionView> {
    ServiceLocator serviceLocator;

    public ServerConnectionController(ServerConnectionModel model, ServerConnectionView view) {
        super(model, view);

        serviceLocator = ServiceLocator.getServiceLocator();

        // register ourselves to listen for button clicks
        view.getBtnConnect().setOnAction(event -> buttonClick());

        // register ourselves to handle window-closing event
        view.getStage().setOnCloseRequest(event -> Platform.exit());
    }

    public void disableInputs() {
        view.getServerIp().setDisable(true);
        view.getPort().setDisable(true);
        view.getBtnConnect().setDisable(true);
    }

    public void enableInputs() {
        view.getServerIp().setDisable(false);
        view.getPort().setDisable(false);
        view.getBtnConnect().setDisable(false);
    }

    public void buttonClick() {
        // Disable everything to prevent something while working on the data
        disableInputs();

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
                enableInputs();
                Platform.runLater(() -> {
                    Text text = Helper.useText("gui.serverConnection.connectionFailed");
                    text.setFill(Color.RED);
                    view.getErrorMessage().getChildren().addAll(text, Helper.useSpacer(20));
                    view.getStage().setHeight(view.getStage().getHeight() + 30); // Make window larger, so it doesn't become crammed
                });
            }

            if (backend != null) {
                Platform.runLater(() -> {
                    // Open login window and close server connection window
                    // TODO: Open Login window
                    //LoginModel model = new LoginModel();
                    //LoginView newView = new LoginView(model);
                    //new LoginController(model, view);

                    view.stop();
                    view = null;
                    //newView.start();
                });
            }
        };
        new Thread(connect).start();
    }
}
