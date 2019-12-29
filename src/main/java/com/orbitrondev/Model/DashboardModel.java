package com.orbitrondev.Model;

import com.orbitrondev.Abstract.Model;
import com.orbitrondev.Entity.ChatModel;
import com.orbitrondev.Entity.MessageModel;
import com.orbitrondev.Entity.UserModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DashboardModel extends Model {
    private ObservableList<ChatModel> chats;
    private ObservableList<UserModel> contacts;
    private ObservableList<MessageModel> messages;

    public DashboardModel() {
        super();

        chats = FXCollections.observableArrayList();
        serviceLocator.getDb().getChatDao().forEach(chat -> {
            if (chat.isJoined() && !chat.isArchived()) {
                chats.add(chat);
            }
        });

        contacts = FXCollections.observableArrayList();
        serviceLocator.getDb().getUserDao().forEach(contact -> {
            if (contact.isFriend()) {
                contacts.add(contact);
            }
        });

        messages = FXCollections.observableArrayList();
    }

    public ObservableList<ChatModel> getChats() {
        return chats;
    }

    public ObservableList<UserModel> getContacts() {
        return contacts;
    }

    public ObservableList<MessageModel> getMessages() {
        return messages;
    }
}
