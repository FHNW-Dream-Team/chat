package com.orbitrondev.Model.Chat;

import com.orbitrondev.Model.ChatModel;

/**
 * A subclass of ChatModel specifically for the a public group chat.
 *
 * @author Manuele Vaccari
 * @version %I%, %G%
 * @since 0.0.1
 */
public class PublicGroupChatModel extends ChatModel {
    public PublicGroupChatModel(String name) {
        super(name);
        this.chatType = ChatType.PublicGroupChat;
    }
}
