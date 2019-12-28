package com.orbitrondev.View;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.IntegerValidator;
import com.jfoenix.validation.RegexValidator;
import com.jfoenix.validation.RequiredFieldValidator;
import com.orbitrondev.Controller.I18nController;
import com.orbitrondev.Validator.IsSameValidator;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import jiconfont.IconCode;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconNode;

import java.util.Locale;

public class Helper {

    // Shortcuts for UI elements

    public static Region useSpacer(int space) {
        Region spacer = new Region();
        spacer.setPrefHeight(space);
        VBox.setVgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    public static Region useHorizontalSpacer(int space) {
        Region spacer = new Region();
        spacer.setPrefWidth(space);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    public static Text useText(String translatorKey) {
        Text textField = new Text();
        textField.textProperty().bind(I18nController.createStringBinding(translatorKey));
        return textField;
    }

    public static Text useText(String translatorKey, Stage paneToBindWidth) {
        // https://stackoverflow.com/questions/51199903/how-to-bind-a-value-to-the-result-of-a-calculation
        Text textField = useText(translatorKey);
        DoubleProperty padding = new SimpleDoubleProperty(40.0); // Check the css at .custom-container (padding left and right = 40)
        NumberBinding wrapping = Bindings.subtract(paneToBindWidth.widthProperty(), padding);
        textField.wrappingWidthProperty().bind(wrapping);
        return textField;
    }

    public static JFXTextField useTextField(String translatorKey) {
        JFXTextField textField = new JFXTextField();
        textField.setLabelFloat(true);
        textField.promptTextProperty().bind(I18nController.createStringBinding(translatorKey));
        return textField;
    }

    public static JFXPasswordField usePasswordField(String translatorKey) {
        JFXPasswordField passwordField = new JFXPasswordField();
        passwordField.setLabelFloat(true);
        passwordField.promptTextProperty().bind(I18nController.createStringBinding(translatorKey));
        return passwordField;
    }

    public static JFXButton usePrimaryButton(String translatorKey) {
        JFXButton primaryButton = new JFXButton();
        primaryButton.textProperty().bind(I18nController.createStringBinding(() -> I18nController.get(translatorKey).toUpperCase()));
        primaryButton.setButtonType(JFXButton.ButtonType.RAISED);
        primaryButton.setPrefWidth(Double.MAX_VALUE);
        primaryButton.setMaxWidth(Double.MAX_VALUE);
        primaryButton.getStyleClass().add("primary");
        return primaryButton;
    }

    public static JFXButton useSecondaryButton(String translatorKey) {
        JFXButton secondaryButton = new JFXButton();
        secondaryButton.textProperty().bind(I18nController.createStringBinding(() -> I18nController.get(translatorKey).toUpperCase()));
        secondaryButton.setButtonType(JFXButton.ButtonType.RAISED);
        secondaryButton.setPrefWidth(Double.MAX_VALUE);
        secondaryButton.setMaxWidth(Double.MAX_VALUE);
        secondaryButton.getStyleClass().add("secondary");
        return secondaryButton;
    }

    public static HBox useNavBar(String translatorKey) {
        HBox navBar = new HBox();
        Text title = new Text();
        title.textProperty().bind(I18nController.createStringBinding(() -> I18nController.get(translatorKey).toUpperCase()));
        title.setFill(Color.WHITE);
        navBar.getStyleClass().add("navbar");
        navBar.getChildren().add(title);
        return navBar;
    }

    public static HBox useStaticNavBar(String text) {
        HBox navBar = new HBox();
        Text title = new Text(text.toUpperCase());
        title.setFill(Color.WHITE);
        navBar.getStyleClass().add("navbar");
        navBar.getChildren().add(title);
        return navBar;
    }

    public static HBox useVariableNavBar(ObservableValue<String> text) {
        HBox navBar = new HBox();
        Text title = new Text();
        title.textProperty().bind(text);
        title.setFill(Color.WHITE);
        navBar.getStyleClass().add("navbar");
        navBar.getChildren().add(title);
        return navBar;
    }

    // Shortcut for validator with UI input elements

    public static RequiredFieldValidator useRequiredValidator(String translatorKey) {
        RequiredFieldValidator requiredValidator = new RequiredFieldValidator();
        requiredValidator.messageProperty().bind(I18nController.createStringBinding(translatorKey));
        return requiredValidator;
    }

    public static IntegerValidator useIsIntegerValidator(String translatorKey) {
        IntegerValidator isIntValidator = new IntegerValidator();
        isIntValidator.messageProperty().bind(I18nController.createStringBinding(translatorKey));
        return isIntValidator;
    }

    public static IsSameValidator useIsSameValidator(TextInputControl validateTo, String translatorKey) {
        IsSameValidator isSameValidator = new IsSameValidator(validateTo);
        isSameValidator.messageProperty().bind(I18nController.createStringBinding(translatorKey));
        return isSameValidator;
    }

    public static RegexValidator useIsValidIpValidator(String translatorKey) {
        RegexValidator isValidIpValidator = new RegexValidator();
        isValidIpValidator.messageProperty().bind(I18nController.createStringBinding(translatorKey));
        isValidIpValidator.setRegexPattern("^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$");
        return isValidIpValidator;
    }

    public static RegexValidator useIsValidPortValidator(String translatorKey) {
        RegexValidator isValidPortValidator = new RegexValidator();
        isValidPortValidator.messageProperty().bind(I18nController.createStringBinding(translatorKey));
        isValidPortValidator.setRegexPattern("^([0-9]{1,4}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]|6553[0-5])$");
        return isValidPortValidator;
    }

    // Shortcut for UI menus

    public static MenuBar useDefaultMenuBar() {
        MenuBar menuBar = new MenuBar();

        // Create menus
        Menu fileMenu = useMenu("gui.menu.file");
        Menu helpMenu = useMenu("gui.menu.help");

        // Add menuItems to the Menus
        fileMenu.getItems().addAll(
            useLanguageMenu(),
            new SeparatorMenuItem(),
            useExitMenuItem()
        );

        // Add Menus to the MenuBar
        menuBar.getMenus().addAll(
            fileMenu,
            helpMenu
        );
        return menuBar;
    }

    public static Menu useMenu(String translatorKey) {
        Menu menu = new Menu();
        menu.textProperty().bind(I18nController.createStringBinding(translatorKey));
        return menu;
    }

    public static Menu useLanguageMenu() {
        Menu changeLanguageMenu = useMenu("gui.menu.file.changeLanguage");
        for (Locale locale : I18nController.getSupportedLocales()) {
            StringProperty langInLocale = new SimpleStringProperty();
            String lang;
            switch (locale.getLanguage()) {
                default:
                case "en":
                    langInLocale.bind(I18nController.createStringBinding("gui.menu.file.changeLanguage.english"));
                    lang = "English";
                    break;
                case "de":
                    langInLocale.bind(I18nController.createStringBinding("gui.menu.file.changeLanguage.german"));
                    lang = "Deutsch";
                    break;
                case "fr":
                    langInLocale.bind(I18nController.createStringBinding("gui.menu.file.changeLanguage.french"));
                    lang = "Français";
                    break;
                case "it":
                    langInLocale.bind(I18nController.createStringBinding("gui.menu.file.changeLanguage.italian"));
                    lang = "Italiano";
                    break;
            }

            MenuItem language = new MenuItem(langInLocale.get() + " (" + lang + ")");
            language.setOnAction(event -> I18nController.setLocale(locale));
            changeLanguageMenu.getItems().add(language);
        }
        return changeLanguageMenu;
    }

    public static MenuItem useMenuItem(String translatorKey) {
        MenuItem item = new MenuItem();
        item.textProperty().bind(I18nController.createStringBinding(translatorKey));
        return item;
    }

    public static MenuItem useExitMenuItem() {
        MenuItem exitItem = useMenuItem("gui.menu.file.exit");
        exitItem.setAccelerator(KeyCombination.keyCombination("Alt+F4"));
        exitItem.setOnAction(event -> System.exit(0));
        return exitItem;
    }

    // Icons
    public static IconNode useIcon(IconCode iconCode, Color color) {
        IconNode icon = new IconNode(iconCode);
        icon.setFill(color);
        return icon;
    }

    public static IconNode useIcon(IconCode iconCode) {
        return useIcon(iconCode, Color.BLACK);
    }

    public static IconNode useIconAdd(Color color) {
        return useIcon(GoogleMaterialDesignIcons.ADD, color);
    }

    public static IconNode useIconContact(Color color) {
        return useIcon(GoogleMaterialDesignIcons.CONTACTS, color);
    }

    public static IconNode useIconChat(Color color) {
        return useIcon(GoogleMaterialDesignIcons.FORUM, color);
    }

    public static IconNode useIconSend(Color color) {
        return useIcon(GoogleMaterialDesignIcons.SEND, color);
    }
}
