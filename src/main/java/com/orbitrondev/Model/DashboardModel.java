package com.orbitrondev.Model;

import com.orbitrondev.Abstract.Model;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DashboardModel extends Model {
    private ObservableList<ChatModel> chats;
    private ObservableList<UserModel> contacts;

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
    }

    public ObservableList<ChatModel> getChats() {
        return chats;
    }

    public ObservableList<UserModel> getContacts() {
        return contacts;
    }
}
