package com.orbitrondev.Model.Chat;

import com.orbitrondev.Model.ChatModel;

public class DirectChatModel extends ChatModel {
    public DirectChatModel(String name) {
        super(name);
        this.chatType = ChatType.DirectChat;
    }
}
