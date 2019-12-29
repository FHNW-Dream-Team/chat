package com.orbitrondev.Controller;

import com.j256.ormlite.dao.ForeignCollection;
import com.orbitrondev.Abstract.Controller;
import com.orbitrondev.Entity.*;
import com.orbitrondev.EventListener.MessageTextEventListener;
import com.orbitrondev.Model.*;
import com.orbitrondev.View.ChangePasswordView;
import com.orbitrondev.View.DashboardView;
import com.orbitrondev.View.DeleteAccountView;
import com.orbitrondev.View.LoginView;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class DashboardController extends Controller<DashboardModel, DashboardView> implements MessageTextEventListener {
    protected DashboardController(DashboardModel model, DashboardView view) {
        super(model, view);

        // register ourselves to listen for button clicks
        view.getItemChangePassword().setOnMouseClicked(event -> clickOnMenuChangePassword());
        view.getItemDeleteAccount().setOnMouseClicked(event -> clickOnMenuDeleteAccount());
        view.getItemLogout().setOnMouseClicked(event -> clickOnMenuLogout());
        view.getChatList().setOnMouseClicked(event -> clickOnChatList());
        view.getContactList().setOnMouseClicked(event -> clickOnContactList());
        view.getAddChatButton().setOnAction(event -> clickOnAddChat());
        view.getAddContactButton().setOnAction(event -> clickOnAddContact());
        view.getSendButton().setOnAction(event -> clickOnSendMessage());

        // Don't allow the user to enter more than 1024 characters
        view.getMessage().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > 1024) {
                view.getMessage().setText(newValue.substring(0, 1024));
            }
        });

        // register ourselves to listen for incoming chat messages
        serviceLocator.getBackend().addMessageTextListener(this);

        // register ourselves to handle window-closing event
        view.getStage().setOnCloseRequest(event -> Platform.exit());
    }

    private void showErrorDialogue(String translatorKeyTitle, String translatorKeyContent) {
        Platform.runLater(() -> {
            Alert dialog = new Alert(Alert.AlertType.ERROR);
            dialog.titleProperty().bind(I18nController.createStringBinding(translatorKeyTitle));
            dialog.setHeaderText(null);
            dialog.contentTextProperty().bind(I18nController.createStringBinding(translatorKeyContent));
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(getClass().getResource("/images/icon.png").toString())); // Add icon to window
            dialog.showAndWait();
        });
    }

    private void openChangePasswordWindow() {
        Platform.runLater(() -> {
            Stage appStage = new Stage();
            ChangePasswordModel model = new ChangePasswordModel();
            ChangePasswordView newView = new ChangePasswordView(appStage, model);
            new ChangePasswordController(model, newView);

            view.stop();
            view = null;
            newView.start();
        });
    }

    private void openDeleteAccountWindow() {
        Platform.runLater(() -> {
            Stage appStage = new Stage();
            DeleteAccountModel model = new DeleteAccountModel();
            DeleteAccountView newView = new DeleteAccountView(appStage, model);
            new DeleteAccountController(model, newView);

            view.stop();
            view = null;
            newView.start();
        });
    }

    private void openLoginWindow() {
        Platform.runLater(() -> {
            // Open login window and close delete account window
            Stage appStage = new Stage();
            LoginsModel model = new LoginsModel();
            LoginView newView = new LoginView(appStage, model);
            new LoginController(model, newView);

            view.stop();
            view = null;
            newView.start();
        });
    }

    private void clickOnMenuChangePassword() {
        openChangePasswordWindow();
    }

    private void clickOnMenuDeleteAccount() {
        openDeleteAccountWindow();
    }

    private void clickOnMenuLogout() {
        MainModel mainModel;
        if (serviceLocator.getModel() == null) {
            mainModel = new MainModel();
            serviceLocator.setModel(mainModel);
        } else {
            mainModel = serviceLocator.getModel();
        }

        // Connection would freeze window (and the animations) so do it in a different thread.
        Runnable logoutTask = () -> {
            boolean userLoggedOut = false;
            try {
                // Try to logout
                userLoggedOut = serviceLocator.getBackend().sendLogout();
            } catch (IOException e) {
                // This exception contains ConnectException, which basically means, it couldn't connect to the server.
            }

            if (userLoggedOut) {
                mainModel.setCurrentLogin(null);
                openLoginWindow();
            }
        };
        new Thread(logoutTask).start();
    }

    private void clickOnAddChat() {
        final BackendController backend = serviceLocator.getBackend();
        final LoginModel login = serviceLocator.getModel().getCurrentLogin();
        final String token = login.getToken();
        final DatabaseController db = serviceLocator.getDb();

        String chatWith = "";
        boolean validTarget = false;
        boolean targetIsUser = false;
        boolean targetIsGroup = false;
        boolean targetIsNewGroup = false;
        // "null" means user canceled, length "0" means no user name given, so repeat until right
        while (chatWith != null && chatWith.length() == 0 && !validTarget) {
            chatWith = showAddChatDialogue();

            // Check if it's a group chat
            ArrayList<String> groupChats = new ArrayList<>();
            try {
                // Update local list of known public group chats
                groupChats = backend.sendListChatrooms(token);
            } catch (IOException e) {
                // Ignore
            }
            if (groupChats.contains(chatWith)) {
                validTarget = true;
                targetIsGroup = true;
            } else {
                // Check if it's a user
                try {
                    if (backend.sendUserOnline(token, chatWith)) {
                        validTarget = true;
                        targetIsUser = true;
                    } else {
                        targetIsGroup = true;
                        targetIsNewGroup = true;
                    }
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
        // Stop operation completely if no username was given in the dialogue
        if (chatWith == null) return;

        try {
            ChatModel chat;
            if (targetIsUser) chat = db.getGroupChatOrCreate(login.getUsername() + "_" + chatWith, ChatType.DirectChat);
            else if (targetIsGroup) {
                if (targetIsNewGroup) {
                    // TODO: Let the user choose between public and private
                    if (!backend.sendCreateChatroom(token, chatWith, true)) {
                        showErrorDialogue("gui.dashboard.error.program.title", "gui.dashboard.error.program.content");
                        return;
                    }
                }
                if (backend.sendJoinChatroom(token, chatWith, login.getUsername())) {
                    chat = db.getGroupChatOrCreate(chatWith, ChatType.PublicGroupChat);
                } else {
                    showErrorDialogue("gui.dashboard.error.program.title", "gui.dashboard.error.program.content");
                    return;
                }
            } else {
                showErrorDialogue("gui.dashboard.error.program.title", "gui.dashboard.error.program.content");
                return;
            }

            // Set as joined
            chat.setJoined(true);
            db.getChatDao().update(chat);
            model.getChats().add(chat);
        } catch (SQLException | IOException e) {
            // Ignore
        }

    }

    private String showAddChatDialogue() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.titleProperty().bind(I18nController.createStringBinding("gui.dashboard.addChat.title"));
        AtomicReference<String> chatList = new AtomicReference<>("");
        try {
            serviceLocator.getBackend().sendListChatrooms(serviceLocator.getModel().getCurrentLogin().getToken()).forEach(chat -> {
                if (chatList.get().length() != 0) {
                    chatList.set(chatList.get() + ", ");
                }
                chatList.set(chatList.get() + chat);
            });
        } catch (IOException e) {
            // Ignore
        }
        // Publicly available group chats:
        VBox content = new VBox();
        content.setPadding(new Insets(5));
        Text headerText = new Text("Publicly available group chats: " + chatList);
        headerText.setWrappingWidth(500);
        content.getChildren().add(headerText);
        dialog.getDialogPane().setHeader(content);
        dialog.contentTextProperty().bind(I18nController.createStringBinding("gui.dashboard.addChat.content"));
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResource("/images/icon.png").toString())); // Add icon to window

        Optional<String> result = dialog.showAndWait();

        return result.orElse(null);
    }

    private void clickOnAddContact() {
        String username = showAddContactDialogue();
        // "null" means user canceled, length "0" means no user name given, so repeat until right
        while (username != null && username.length() == 0) {
            username = showAddContactDialogue();
        }
        // Stop operation completely if no username was given in the dialogue
        if (username == null) return;

        // Otherwise find the neede model
        UserModel user = null;
        for (UserModel u : serviceLocator.getDb().getUserDao()) {
            if (!u.getUsername().equals(username)) continue;
            user = u;
        }

        // Cancel if we have already added that user
        for (UserModel u : model.getContacts()) {
            if (u.getUsername().equals(username)) {
                showErrorDialogue("gui.dashboard.addContact.error.alreadyAdded.title", "gui.dashboard.addContact.error.alreadyAdded.content");
                return;
            }
        }

        try {
            if (serviceLocator.getBackend().sendUserOnline(serviceLocator.getModel().getCurrentLogin().getToken(), username)) {
                if (user == null) {
                    // Create a new model if we didn't have it yet
                    user = serviceLocator.getDb().getUserOrCreate(username);
                }
                // Set him as a new friend
                user.setFriend(true);
                serviceLocator.getDb().getUserDao().update(user);
                model.getContacts().add(user);
            } else {
                showErrorDialogue("gui.dashboard.addContact.error.offline.title", "gui.dashboard.addContact.error.offline.content");
            }
        } catch (SQLException | IOException e) {
            showErrorDialogue("gui.dashboard.error.program.title", "gui.dashboard.error.program.content");
        }
    }

    private String showAddContactDialogue() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.titleProperty().bind(I18nController.createStringBinding("gui.dashboard.addContact.title"));
        dialog.setHeaderText(null);
        dialog.contentTextProperty().bind(I18nController.createStringBinding("gui.dashboard.addContact.content"));
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResource("/images/icon.png").toString())); // Add icon to window

        Optional<String> result = dialog.showAndWait();

        return result.orElse(null);
    }

    private void clickOnChatList() {
        DatabaseController db = serviceLocator.getDb();

        ChatModel selectedItem = view.getChatList().getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            // Clicked on random place, so do nothing
            return;
        }
        ChatModel chat = null;
        try {
            chat = db.getGroupChatOrCreate(selectedItem.getName(), ChatType.PublicGroupChat);
        } catch (SQLException e) {
            showErrorDialogue("gui.dashboard.error.program.title", "gui.dashboard.error.program.content");
        }
        if (chat == null) {
            showErrorDialogue("gui.dashboard.error.program.title", "gui.dashboard.error.program.content");
        }

        view.removeAllMessages();
        view.navBarTitleRightProperty().set(chat.getName());
        ForeignCollection<MessageModel> messages = chat.getMessages();
        if (messages != null) {
            messages.forEach(message -> view.addMessage(message));
        }
    }

    private void clickOnContactList() {
        DatabaseController db = serviceLocator.getDb();
        LoginModel login = serviceLocator.getModel().getCurrentLogin();

        UserModel selectedItem = view.getContactList().getSelectionModel().getSelectedItem();
        ChatModel chat = null;
        if (selectedItem == null) {
            // Clicked on random place, so do nothing
            return;
        }
        try {
            chat = db.getGroupChatOrCreate(login.getUsername() + "_" + selectedItem.getUsername(), ChatType.DirectChat);
        } catch (SQLException e) {
            showErrorDialogue("gui.dashboard.error.program.title", "gui.dashboard.error.program.content");
        }
        if (chat == null) {
            showErrorDialogue("gui.dashboard.error.program.title", "gui.dashboard.error.program.content");
        }

        view.removeAllMessages();
        view.navBarTitleRightProperty().set(selectedItem.getUsername());
        ForeignCollection<MessageModel> messages = chat.getMessages();
        if (messages != null) {
            messages.forEach(message -> view.addMessage(message));
        }
    }

    private void clickOnSendMessage() {
        if (view.getMessage().getLength() == 0) {
            return;
        }
        // Check if it is not more than 1024 characters
        if (view.getMessage().getLength() > 1024) {
            showErrorDialogue("gui.dashboard.error.program.title", "gui.dashboard.error.program.content");
            return;
        }

        BackendController backend = serviceLocator.getBackend();
        LoginModel login = serviceLocator.getModel().getCurrentLogin();

        Runnable sendTask = () -> {
            try {
                boolean isUser = false;
                for (UserModel user : serviceLocator.getDb().getUserDao()) {
                    if (view.getNavBarTitleRight().equals(user.getUsername())) {
                        isUser = true;
                        break;
                    }
                }
                if (isUser && !backend.sendUserOnline(login.getToken(), view.getNavBarTitleRight())) {
                    showErrorDialogue("gui.dashboard.sendMessage.error.offline.title", "gui.dashboard.sendMessage.error.offline.content");
                    return;
                }

                MessageModel message = backend.sendSendMessage(login.getToken(), view.getNavBarTitleRight(), view.getMessage().getText());
                if (message != null) {
                    view.getMessage().setText("");
                    view.addMessage(message);
                } else {
                    showErrorDialogue("gui.dashboard.error.program.title", "gui.dashboard.error.program.content");
                }
            } catch (IOException e) {
                showErrorDialogue("gui.dashboard.error.program.title", "gui.dashboard.error.program.content");
            }
        };
        new Thread(sendTask).start();
    }

    @Override
    public void onMessageTextEvent(UserModel user, ChatModel chat, MessageModel message) {
        // The server also sends a message when we send one. So only show messages without our username.
        if (!user.getUsername().equals(serviceLocator.getModel().getCurrentLogin().getUsername())) {
            try {
                serviceLocator.getDb().getMessageDao().create(message);
            } catch (SQLException e) {
                showErrorDialogue("gui.dashboard.error.program.title", "gui.dashboard.error.program.content");
            }

            if (chat.getName().equals(view.getNavBarTitleRight())) {
                view.addMessage(message);
            }
        }
    }
}
