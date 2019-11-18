package com.orbitrondev.Model.Chat;

import com.orbitrondev.Model.ChatModel;

public class PublicGroupChatModel extends ChatModel {
    public PublicGroupChatModel(String name) {
        super(name);
        this.chatType = ChatType.PublicGroupChat;
    }
}
