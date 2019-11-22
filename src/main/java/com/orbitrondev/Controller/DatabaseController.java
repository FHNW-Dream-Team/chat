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
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * A class to create a database connection.
 *
 * @author Manuele Vaccari
 * @version %I%, %G%
 * @since 0.0.1
 */
public class DatabaseController implements Closeable {
    private ConnectionSource connectionSource;

    private Dao<ChatModel, String> chatDao;
    private Dao<MessageModel, String> messageDao;
    private Dao<UserModel, String> userDao;
    private Dao<ChatUserModel, String> chatUserDao;

    private Dao<LoginModel, String> loginDao;
    private Dao<ServerModel, String> serverDao;

    /**
     * Create a database connection
     *
     * @param databaseLocation A string containing the location of the file to be accessed (and if necessary created)
     *
     * @since 0.0.1
     */
    public DatabaseController(String databaseLocation) {
        try {
            // this uses h2 but you can change it to match your database
            String databaseUrl = "jdbc:sqlite:" + databaseLocation;

            // create our data-source for the database
            connectionSource = new JdbcConnectionSource(databaseUrl);

            // setup our database and DAOs
            setupDatabase();
        } catch (SQLException e) {
            // TODO: Handle exception
            e.printStackTrace();
        }
    }

    /**
     * Setup our database and DAOs, for the created connection.
     *
     * @throws SQLException If an SQL error occurs.
     * @since 0.0.1
     */
    private void setupDatabase() throws SQLException {

        /*
         * Create our DAOs. One for each class and associated table.
         */
        chatDao = DaoManager.createDao(connectionSource, ChatModel.class);
        messageDao = DaoManager.createDao(connectionSource, MessageModel.class);
        userDao = DaoManager.createDao(connectionSource, UserModel.class);

        chatUserDao = DaoManager.createDao(connectionSource, ChatUserModel.class);

        loginDao = DaoManager.createDao(connectionSource, LoginModel.class);
        serverDao = DaoManager.createDao(connectionSource, ServerModel.class);

        /*
         * Create the tables, if they don't exist yet.
         */
        TableUtils.createTableIfNotExists(connectionSource, ChatModel.class);
        TableUtils.createTableIfNotExists(connectionSource, MessageModel.class);
        TableUtils.createTableIfNotExists(connectionSource, UserModel.class);

        TableUtils.createTableIfNotExists(connectionSource, ChatUserModel.class);

        TableUtils.createTableIfNotExists(connectionSource, LoginModel.class);
        TableUtils.createTableIfNotExists(connectionSource, ServerModel.class);
    }

    /**
     * Close the database connection.
     *
     * @since 0.0.1
     */
    @Override
    public void close() {
        if (connectionSource != null) try {
            connectionSource.close();
        } catch (IOException e) {
            // we don't care
        }
    }


    /**
     * @return DAO object for the chats
     *
     * @since 0.0.2
     */
    public Dao<ChatModel, String> getChatDao() {
        return chatDao;
    }

    /**
     * @return DAO object for the messages inside chats
     *
     * @since 0.0.2
     */
    public Dao<MessageModel, String> getMessageDao() {
        return messageDao;
    }

    /**
     * @return DAO object for the users
     *
     * @since 0.0.2
     */
    public Dao<UserModel, String> getUserDao() {
        return userDao;
    }

    /**
     * @return DAO object for the many-to-many connection between chats and users
     *
     * @since 0.0.2
     */
    public Dao<ChatUserModel, String> getChatUserDao() {
        return chatUserDao;
    }

    /**
     * @return DAO object for the saved logins
     *
     * @since 0.0.2
     */
    public Dao<LoginModel, String> getLoginDao() {
        return loginDao;
    }

    /**
     * @return DAO object for the saved servers
     *
     * @since 0.0.2
     */
    public Dao<ServerModel, String> getServerDao() {
        return serverDao;
    }

    /* Create Many-To-Many Relations ************************************************/
    /**
     * Source: https://github.com/j256/ormlite-jdbc/blob/master/src/test/java/com/j256/ormlite/examples/manytomany/ManyToManyMain.java
     */
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
