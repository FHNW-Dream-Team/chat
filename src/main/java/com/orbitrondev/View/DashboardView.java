package com.orbitrondev.View;

import com.jfoenix.controls.*;
import com.orbitrondev.Abstract.View;
import com.orbitrondev.Controller.I18nController;
import com.orbitrondev.Model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconFontFX;
import jiconfont.javafx.IconNode;

public class DashboardView extends View<DashboardModel> {
    private JFXPopup popup;
    private Label itemChangePassword;
    private Label itemDeleteAccount;
    private Label itemLogout;

    private JFXListView<ChatModel> chatList;
    private JFXListView<UserModel> contactList;

    private JFXButton addChatButton;
    private JFXButton addContactButton;

    private StringProperty navBarTitleRight;
    private JFXListView<HBox> messageList;

    public DashboardView(Stage stage, DashboardModel model) {
        super(stage, model);
        stage.titleProperty().bind(I18nController.createStringBinding("gui.dashboard.title"));
        stage.setMinHeight(300);
        stage.setMinWidth(400);
    }

    @Override
    protected Scene create_GUI() {
        // Create root
        AnchorPane rootInAnchor = new AnchorPane();

        VBox root = new VBox();
        root.setPrefWidth(600);
        root.getStyleClass().add("background-white");
        AnchorPane.setTopAnchor(root, 0.0);
        AnchorPane.setLeftAnchor(root, 0.0);
        AnchorPane.setRightAnchor(root, 0.0);
        AnchorPane.setBottomAnchor(root, 0.0);

        rootInAnchor.getChildren().add(root);

        // Create body
        AnchorPane bodyInAnchor = new AnchorPane();
        VBox.setVgrow(bodyInAnchor, Priority.ALWAYS); // Make the split pane full height

        SplitPane body = new SplitPane();
        AnchorPane.setTopAnchor(body, 0.0);
        AnchorPane.setLeftAnchor(body, 0.0);
        AnchorPane.setRightAnchor(body, 0.0);
        AnchorPane.setBottomAnchor(body, 0.0);

        bodyInAnchor.getChildren().add(body);

        // Create left body
        VBox bodyLeft = new VBox();

        // Create nav bar
        HBox navBar = Helper.useStaticNavBar(serviceLocator.getModel().getCurrentLogin().getUsername());

        // Create spacer in nav bar
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Create menu button in nav bar
        JFXHamburger menuButton = new JFXHamburger();
        JFXRippler rippler = new JFXRippler(menuButton, JFXRippler.RipplerMask.CIRCLE, JFXRippler.RipplerPos.BACK);
        JFXListView<Label> list = new JFXListView<>();
        popup = new JFXPopup(list);
        IconFontFX.register(GoogleMaterialDesignIcons.getIconFont());
        itemChangePassword = new Label();
        itemChangePassword.setGraphic(new IconNode(GoogleMaterialDesignIcons.MODE_EDIT));
        itemChangePassword.textProperty().bind(I18nController.createStringBinding("gui.dashboard.menu.changePassword"));
        itemDeleteAccount = new Label();
        itemDeleteAccount.setGraphic(new IconNode(GoogleMaterialDesignIcons.DELETE));
        itemDeleteAccount.textProperty().bind(I18nController.createStringBinding("gui.dashboard.menu.deleteAccount"));
        itemLogout = new Label();
        itemLogout.setGraphic(new IconNode(GoogleMaterialDesignIcons.EXIT_TO_APP));
        itemLogout.textProperty().bind(I18nController.createStringBinding("gui.dashboard.menu.logout"));
        list.getItems().addAll(itemChangePassword, itemDeleteAccount, itemLogout);

        AnchorPane container = new AnchorPane();
        container.getChildren().add(rippler);
        AnchorPane.setLeftAnchor(rippler, 100.0); // This also sets the min width for the left body

        menuButton.setOnMouseClicked(e -> popup.show(rippler, JFXPopup.PopupVPosition.TOP, JFXPopup.PopupHPosition.LEFT));

        navBar.getChildren().addAll(
            spacer,
            container
        );

        // Create menu
        JFXTabPane tabMenu = new JFXTabPane();
        VBox.setVgrow(tabMenu, Priority.ALWAYS);

        // Create menu item for chats
        Tab tabChats = new Tab();
        tabChats.setGraphic(Helper.useIconChat(Color.WHITE));

        // Create anchor for content inside chat list
        AnchorPane tabChatsContentInAnchor = new AnchorPane();

        // Create and add all joined chats
        chatList = new JFXListView<>();
        chatList.setItems(model.getChats());
        AnchorPane.setTopAnchor(chatList, 0.0);
        AnchorPane.setLeftAnchor(chatList, 0.0);
        AnchorPane.setRightAnchor(chatList, 0.0);
        AnchorPane.setBottomAnchor(chatList, 0.0);

        // Create FAB (Floating Action Button)
        addChatButton = new JFXButton();
        addChatButton.setGraphic(Helper.useIconAdd(Color.WHITE));
        addChatButton.setButtonType(JFXButton.ButtonType.RAISED);
        addChatButton.getStyleClass().add("animated-option-button");
        AnchorPane.setBottomAnchor(addChatButton, 10.0);
        AnchorPane.setRightAnchor(addChatButton, 10.0);

        tabChatsContentInAnchor.getChildren().addAll(chatList, addChatButton);

        tabChats.setContent(tabChatsContentInAnchor);

        // Create menu item for contacts
        Tab tabContacts = new Tab();
        tabContacts.setGraphic(Helper.useIconContact(Color.WHITE));

        AnchorPane tabContactsContentInAnchor = new AnchorPane();

        contactList = new JFXListView<>();
        contactList.setItems(model.getContacts());
        AnchorPane.setTopAnchor(contactList, 0.0);
        AnchorPane.setLeftAnchor(contactList, 0.0);
        AnchorPane.setRightAnchor(contactList, 0.0);
        AnchorPane.setBottomAnchor(contactList, 0.0);

        addContactButton = new JFXButton();
        addContactButton.setGraphic(Helper.useIconAdd(Color.WHITE));
        addContactButton.setButtonType(JFXButton.ButtonType.RAISED);
        addContactButton.getStyleClass().add("animated-option-button");
        AnchorPane.setBottomAnchor(addContactButton, 10.0);
        AnchorPane.setRightAnchor(addContactButton, 10.0);

        tabContactsContentInAnchor.getChildren().addAll(contactList, addContactButton);

        tabContacts.setContent(tabContactsContentInAnchor);

        // Add menu items to menu
        tabMenu.widthProperty().addListener(((observable, oldValue, newValue) -> {
            tabMenu.setTabMaxWidth(newValue.doubleValue() / tabMenu.getTabs().size() - 10);
            tabMenu.setTabMinWidth(newValue.doubleValue() / tabMenu.getTabs().size() - 10);
        }));
        tabMenu.getTabs().addAll(
            tabChats,
            tabContacts
        );

        // Add all items to left body
        bodyLeft.getChildren().addAll(
            navBar,
            tabMenu
        );

        // Create right body
        VBox bodyRight = new VBox();

        // Create nav bar for right side
        navBarTitleRight = new SimpleStringProperty();

        messageList = new JFXListView<>();

        HBox messageBox = new HBox();
        AnchorPane messageBoxContentInAnchor = new AnchorPane();
        HBox.setHgrow(messageBoxContentInAnchor, Priority.ALWAYS);

        JFXTextArea message = new JFXTextArea();
        AnchorPane.setTopAnchor(message, 0.0);
        AnchorPane.setLeftAnchor(message, 0.0);
        AnchorPane.setRightAnchor(message, 50.0);
        AnchorPane.setBottomAnchor(message, 0.0);

        JFXButton sendButton = new JFXButton();
        sendButton.setGraphic(Helper.useIconSend(Color.WHITE));
        sendButton.getStyleClass().addAll("primary", "square");
        AnchorPane.setTopAnchor(sendButton, 5.0);
        AnchorPane.setRightAnchor(sendButton, 5.0);
        AnchorPane.setBottomAnchor(sendButton, 5.0);

        messageBoxContentInAnchor.getChildren().addAll(message, sendButton);
        messageBox.getChildren().addAll(messageBoxContentInAnchor);

        // Add content to right body
        bodyRight.getChildren().addAll(
            Helper.useVariableNavBar(navBarTitleRight),
            messageList,
            messageBox
        );

        // Add both bodies to body
        body.getItems().addAll(
            bodyLeft,
            bodyRight
        );

        // Add body to root
        root.getChildren().addAll(
            Helper.useDefaultMenuBar(),
            bodyInAnchor
        );

        Scene scene = new Scene(rootInAnchor);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        return scene;
    }

    public JFXPopup getPopup() {
        return popup;
    }

    public Label getItemChangePassword() {
        return itemChangePassword;
    }

    public Label getItemDeleteAccount() {
        return itemDeleteAccount;
    }

    public Label getItemLogout() {
        return itemLogout;
    }

    public JFXListView<ChatModel> getChatList() {
        return chatList;
    }

    public JFXListView<UserModel> getContactList() {
        return contactList;
    }

    public JFXButton getAddChatButton() {
        return addChatButton;
    }

    public JFXButton getAddContactButton() {
        return addContactButton;
    }

    public String getNavBarTitleRight() {
        return navBarTitleRight.get();
    }

    public StringProperty navBarTitleRightProperty() {
        return navBarTitleRight;
    }

    public JFXListView<HBox> getMessageList() {
        return messageList;
    }

    public void removeAllMessages() {
        model.getMessages().clear();
        messageList.getItems().clear();
    }

    // https://github.com/sarafinmahtab/MakeChat-App/blob/master/MakeChatClient/src/application/chatboard/ChatBoard.java
    public void addMessage(MessageModel message) {
        LoginModel login = serviceLocator.getModel().getCurrentLogin();
        if (message.getUser().getUsername().equals(login.getUsername())) {
            // If we sent a message
            Label messageLabel = new Label();
            messageLabel.setText(message.getMessage());
            messageLabel.setTextFill(Color.WHITE);

            Label userLabel = new Label();
            userLabel.setText(message.getUser().getUsername());
            userLabel.setStyle("-fx-font-weight: bold;");
            userLabel.setTextFill(Color.WHITE);

            VBox vBox = new VBox(2);
            CornerRadii cornerRadi = new CornerRadii(5f);
            BackgroundFill backgroundFill = new BackgroundFill(Color.rgb(64, 128, 128), cornerRadi, null);
            vBox.setBackground(new Background(backgroundFill));
            vBox.setPadding(new Insets(5f));
            vBox.getChildren().addAll(userLabel, messageLabel);

            Label dateLabel = new Label();
            dateLabel.setText(message.getTimeSentFormatted());
            dateLabel.setStyle("-fx-font-size: 10;");
            dateLabel.setTextFill(Color.GRAY);
            dateLabel.setAlignment(Pos.CENTER);
            dateLabel.setMaxHeight(Double.MAX_VALUE);

            HBox x = new HBox(2);
            x.setMaxWidth(messageList.getWidth() - 20);
            x.setAlignment(Pos.TOP_RIGHT);
            x.getChildren().addAll(dateLabel, vBox);

            messageList.getItems().add(x);
        } else {
            // If we received a message
            Label messageLabel = new Label();
            messageLabel.setText(message.getMessage());
            messageLabel.setTextFill(Color.BLACK);

            Label userLabel = new Label();
            userLabel.setText(message.getUser().getUsername());
            userLabel.setStyle("-fx-font-weight: bold;");
            userLabel.setTextFill(Color.BLACK);

            VBox vBox = new VBox(2);
            CornerRadii cornerRadi = new CornerRadii(5f);
            BackgroundFill backgroundFill = new BackgroundFill(Color.rgb(218, 218, 218), cornerRadi, null);
            vBox.setBackground(new Background(backgroundFill));
            vBox.setPadding(new Insets(5f));
            vBox.getChildren().addAll(userLabel, messageLabel);

            Label dateLabel = new Label();
            dateLabel.setText(message.getTimeSentFormatted());
            dateLabel.setStyle("-fx-font-size: 10;");
            dateLabel.setTextFill(Color.GRAY);
            dateLabel.setAlignment(Pos.CENTER);
            dateLabel.setMaxHeight(Double.MAX_VALUE);

            HBox x = new HBox(2);
            x.setMaxWidth(messageList.getWidth() - 20);
            x.setAlignment(Pos.TOP_LEFT);
            x.getChildren().addAll(vBox, dateLabel);

            messageList.getItems().add(x);
        }
    }
}
