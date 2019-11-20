/**
 * Copyright (c) 2016 sothawo
 * <p>
 * https://www.sothawo.com/2016/09/how-to-implement-a-javafx-ui-where-the-language-can-be-changed-dynamically/
 */

package com.orbitrondev.Controller;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * I18N utility class..
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 * @since 0.0.1
 */
public final class I18nController {
    /**
     * the current selected Locale.
     */
    private static final ObjectProperty<Locale> locale;

    static {
        locale = new SimpleObjectProperty<>(getDefaultLocale());
        locale.addListener((observable, oldValue, newValue) -> Locale.setDefault(newValue));
    }

    /**
     * get the supported Locales.
     *
     * @return List of Locale objects.
     *
     * @since 0.0.1
     */
    public static List<Locale> getSupportedLocales() {
        return new ArrayList<>(Arrays.asList(
            Locale.GERMAN,
            Locale.ENGLISH,
            Locale.FRENCH,
            Locale.ITALIAN
        ));
    }

    /**
     * get the default locale. This is the systems default if contained in the supported locales, english otherwise.
     *
     * @return A Locale constant, containing the current language.
     *
     * @since 0.0.1
     */
    public static Locale getDefaultLocale() {
        Locale sysDefault = Locale.getDefault();
        return getSupportedLocales().contains(sysDefault) ? sysDefault : Locale.ENGLISH;
    }

    public static Locale getLocale() {
        return locale.get();
    }

    public static void setLocale(Locale locale) {
        localeProperty().set(locale);
        Locale.setDefault(locale);
    }

    public static ObjectProperty<Locale> localeProperty() {
        return locale;
    }

    /**
     * gets the string with the given key from the resource bundle for the current locale and uses it as first argument
     * to MessageFormat.format, passing in the optional args and returning the result.
     *
     * @param key  message key
     * @param args optional arguments for the message
     *
     * @return localized formatted string
     *
     * @since 0.0.1
     */
    public static String get(final String key, final Object... args) {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n/messages", getLocale());
        return MessageFormat.format(bundle.getString(key), args);
    }

    /**
     * creates a String Binding to a localized String that is computed by calling the given func
     *
     * @param func function called on every change
     *
     * @return StringBinding
     *
     * @since 0.0.1
     */
    public static StringBinding createStringBinding(Callable<String> func) {
        return Bindings.createStringBinding(func, locale);
    }

    /**
     * creates a String binding to a localized String for the given message bundle key
     *
     * @param key key
     *
     * @return String binding
     *
     * @since 0.0.1
     */
    public static StringBinding createStringBinding(final String key, Object... args) {
        return createStringBinding(() -> get(key, args));
    }
}
