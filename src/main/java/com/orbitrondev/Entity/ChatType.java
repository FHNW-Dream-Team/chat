package com.orbitrondev.Entity;

/**
 * An enum defining the different chat types.
 *
 * @author Manuele Vaccari
 * @version %I%, %G%
 * @since 0.0.1
 */
public enum ChatType {
    DirectChat, PublicGroupChat, PrivateGroupChat;

    @Override
    public String toString() {
        String type = "";
        switch (this) {
            case DirectChat:
                type = "DirectChat";
                break;
            case PublicGroupChat:
                type = "PublicGroupChat";
                break;
            case PrivateGroupChat:
                type = "PrivateGroupChat";
                break;
        }
        return type;
    }
}
