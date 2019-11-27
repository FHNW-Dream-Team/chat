package com.orbitrondev.EventListener;

import com.orbitrondev.Model.ChatModel;
import com.orbitrondev.Model.MessageModel;
import com.orbitrondev.Model.UserModel;

public interface MessageTextEventListener {
    void onMessageTextEvent(UserModel user, ChatModel chat, MessageModel message);
}
