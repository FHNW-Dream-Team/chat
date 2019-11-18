package com.orbitrondev.Model.Chat;

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
