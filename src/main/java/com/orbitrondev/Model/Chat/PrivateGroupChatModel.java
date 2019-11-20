package com.orbitrondev.Model.Chat;

import com.orbitrondev.Model.ChatModel;

/**
 * A subclass of ChatModel specifically for the a private group chat chat.
 *
 * @author Manuele Vaccari
 * @version %I%, %G%
 * @since 0.0.1
 */
public class PrivateGroupChatModel extends ChatModel {
    public PrivateGroupChatModel(String name) {
        super(name);
        this.chatType = ChatType.PrivateGroupChat;
    }
}
