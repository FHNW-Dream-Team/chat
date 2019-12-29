package com.orbitrondev.EventListener;

import com.orbitrondev.Entity.ChatModel;
import com.orbitrondev.Entity.MessageModel;
import com.orbitrondev.Entity.UserModel;

public interface MessageTextEventListener {
    void onMessageTextEvent(UserModel user, ChatModel chat, MessageModel message);
}
