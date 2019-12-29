package com.orbitrondev.Entity;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;
import com.orbitrondev.Controller.ServiceLocator;
import com.orbitrondev.Entity.SupportTables.ChatUserModel;

import java.sql.SQLException;
import java.util.List;

/**
 * A model for the direct and group chats.
 *
 * @author Manuele Vaccari
 * @version %I%, %G%
 * @since 0.0.1
 */
@DatabaseTable(tableName = "chats")
public class ChatModel {

    /**
     * FIELDS ////////////////////////////////
     */

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private String name;

    @ForeignCollectionField
    private ForeignCollection<MessageModel> messages = null;

    @ForeignCollectionField
    private ForeignCollection<ChatUserModel> members = null;

    @DatabaseField(canBeNull = false)
    protected ChatType chatType;

    @DatabaseField(defaultValue = "false", canBeNull = false)
    private boolean archived = false;

    @DatabaseField(defaultValue = "false", canBeNull = false)
    private boolean admin = false;

    @DatabaseField(defaultValue = "false", canBeNull = false)
    private boolean joined = false;

    /**
     * CONSTRUCTORS //////////////////////////
     */

    ChatModel() {
        // For ORMLite
        // all persisted classes must define a no-arg constructor
        // with at least package visibility
    }

    public ChatModel(String name, ChatType chatType) {
        this.name = name;
        this.chatType = chatType;
    }

    /**
     * METHODS ///////////////////////////////
     */

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ForeignCollection<MessageModel> getMessages() {
        return messages;
    }

    public void addMessage(MessageModel message) {
        this.messages.add(message);
    }

    public List<UserModel> getMembers() {
        ServiceLocator sl = ServiceLocator.getServiceLocator();
        List<UserModel> list = null;
        try {
            QueryBuilder<ChatUserModel, String> userChatQb = sl.getDb().getChatUserDao().queryBuilder();
            // this time selecting for the user-id field
            userChatQb.selectColumns("user_id");
            userChatQb.where().eq("chat_id", id);

            // build our outer query
            QueryBuilder<UserModel, String> userQb = sl.getDb().getUserDao().queryBuilder();
            // where the user-id matches the inner query's user-id field
            list = userQb.where().in("id", userChatQb).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void addMember(UserModel user) {
        ServiceLocator sl = ServiceLocator.getServiceLocator();
        ChatUserModel chatUserConnection = new ChatUserModel(user, this);
        try {
            sl.getDb().getChatUserDao().create(chatUserConnection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeMember(UserModel user) {
        ServiceLocator sl = ServiceLocator.getServiceLocator();
        try {
            ChatUserModel result = sl.getDb().getChatUserDao().queryBuilder().where().eq("user_id", user.getId()).and().eq("chat_id", id).queryForFirst();
            if (result != null) {
                sl.getDb().getChatUserDao().delete(result);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ChatType getChatType() {
        return chatType;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isJoined() {
        return joined;
    }

    public void setJoined(boolean joined) {
        this.joined = joined;
    }

    @Override
    public String toString() {
        return name;
    }
}
