package com.orbitrondev.Controller;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.orbitrondev.Model.*;
import com.orbitrondev.Model.SupportTables.ChatUserModel;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DatabaseController implements Closeable {
    private ConnectionSource connectionSource;

    public Dao<ChatModel, String> chatDao;
    public Dao<MessageModel, String> messageDao;
    public Dao<UserModel, String> userDao;
    public Dao<ChatUserModel, String> chatUserDao;

    public Dao<LoginModel, String> loginDao;
    public Dao<ServerModel, String> serverDao;

    public DatabaseController(String databaseLocation) {
        if (!databaseFileExists(databaseLocation)) {
            // Create the database file
            try {
                createDatabaseFile(databaseLocation);
            } catch (IOException e) {
                // TODO: Handle what happens if file was not added
                e.printStackTrace();
            }
        }

        try {
            // this uses h2 but you can change it to match your database
            String databaseUrl = "jdbc:sqlite:" + databaseLocation;

            // create our data-source for the database
            connectionSource = new JdbcConnectionSource(databaseUrl);

            // setup our database and DAOs
            setupDatabase(connectionSource);
        } catch (SQLException e) {
            // TODO: Handle exception
            e.printStackTrace();
        }
    }

    /**
     * Setup our database and DAOs
     */
    private void setupDatabase(ConnectionSource connectionSource) throws SQLException {

        /**
         * Create our DAOs. One for each class and associated table.
         */
        chatDao = DaoManager.createDao(connectionSource, ChatModel.class);
        messageDao = DaoManager.createDao(connectionSource, MessageModel.class);
        userDao = DaoManager.createDao(connectionSource, UserModel.class);

        chatUserDao = DaoManager.createDao(connectionSource, ChatUserModel.class);

        loginDao = DaoManager.createDao(connectionSource, LoginModel.class);
        serverDao = DaoManager.createDao(connectionSource, ServerModel.class);

        /**
         * Create the tables for our example. This would not be necessary if the tables already existed.
         */
        // TODO: Check whether tables already exist
        TableUtils.createTable(connectionSource, ChatModel.class);
        TableUtils.createTable(connectionSource, MessageModel.class);
        TableUtils.createTable(connectionSource, UserModel.class);

        TableUtils.createTable(connectionSource, ChatUserModel.class);

        TableUtils.createTable(connectionSource, LoginModel.class);
        TableUtils.createTable(connectionSource, ServerModel.class);
    }

    private boolean databaseFileExists(String databaseLocation) {
        File file = new File(databaseLocation);
        return file.exists();
    }

    private void createDatabaseFile(String databaseLocation) throws IOException {
        File file = new File(databaseLocation);
        file.createNewFile(); // if file already exists will do nothing
    }

    @Override
    public void close() {
        if (connectionSource != null) try {
            connectionSource.close();
        } catch (IOException e) {
            // we don't care
        }
    }


    /** Create Many-To-Many Relations ************************************************/
    /** Source: https://github.com/j256/ormlite-jdbc/blob/master/src/test/java/com/j256/ormlite/examples/manytomany/ManyToManyMain.java */
    private PreparedQuery<ChatModel> chatsForUserQuery = null;
    private PreparedQuery<UserModel> usersForChatQuery = null;

    private List<ChatModel> lookupChatsForUser(UserModel user) throws SQLException {
        if (chatsForUserQuery == null) {
            chatsForUserQuery = makeChatsForUserQuery();
        }
        chatsForUserQuery.setArgumentHolderValue(0, user);
        return chatDao.query(chatsForUserQuery);
    }

    private List<UserModel> lookupUsersForChat(ChatModel chat) throws SQLException {
        if (usersForChatQuery == null) {
            usersForChatQuery = makeUsersForChatQuery();
        }
        usersForChatQuery.setArgumentHolderValue(0, chat);
        return userDao.query(usersForChatQuery);
    }

    /**
     * Build our query for Post objects that match a User.
     */
    private PreparedQuery<ChatModel> makeChatsForUserQuery() throws SQLException {
        // build our inner query for UserPost objects
        QueryBuilder<ChatUserModel, String> userChatQb = chatUserDao.queryBuilder();
        // just select the post-id field
        userChatQb.selectColumns("chat_id");
        SelectArg userSelectArg = new SelectArg();
        // you could also just pass in user1 here
        userChatQb.where().eq("user_id", userSelectArg);

        // build our outer query for Post objects
        QueryBuilder<ChatModel, String> postQb = chatDao.queryBuilder();
        // where the id matches in the post-id from the inner query
        postQb.where().in("id", userChatQb);
        return postQb.prepare();
    }

    /**
     * Build our query for User objects that match a Post
     */
    private PreparedQuery<UserModel> makeUsersForChatQuery() throws SQLException {
        QueryBuilder<ChatUserModel, String> userChatQb = chatUserDao.queryBuilder();
        // this time selecting for the user-id field
        userChatQb.selectColumns("user_id");
        SelectArg postSelectArg = new SelectArg();
        userChatQb.where().eq("chat_id", postSelectArg);

        // build our outer query
        QueryBuilder<UserModel, String> userQb = userDao.queryBuilder();
        // where the user-id matches the inner query's user-id field
        userQb.where().in("id", userChatQb);
        return userQb.prepare();
    }

}
