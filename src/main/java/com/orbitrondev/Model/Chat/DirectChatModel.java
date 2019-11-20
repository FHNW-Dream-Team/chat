package com.orbitrondev.Model.Chat;

import com.orbitrondev.Model.ChatModel;

/**
 * A subclass of ChatModel specifically for the a person-to-person chat.
 *
 * @author Manuele Vaccari
 * @version %I%, %G%
 * @since 0.0.1
 */
public class DirectChatModel extends ChatModel {
    public DirectChatModel(String name) {
        super(name);
        this.chatType = ChatType.DirectChat;
    }
}
