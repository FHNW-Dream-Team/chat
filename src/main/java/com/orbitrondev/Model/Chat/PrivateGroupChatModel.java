package com.orbitrondev.Model.Chat;

import com.orbitrondev.Model.ChatModel;

public class PrivateGroupChatModel extends ChatModel {
    public PrivateGroupChatModel(String name) {
        super(name);
        this.chatType = ChatType.PrivateGroupChat;
    }
}
