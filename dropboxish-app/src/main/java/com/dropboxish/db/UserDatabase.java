package com.dropboxish.db;

import java.sql.*;
import java.util.logging.Logger;

/**
 * Created by satyan on 11/22/17.
 * Database where user's credentials are stored
 */
public class UserDatabase {
    private final static Logger logger = Logger.getLogger("App");
    /**
     * File where the SQLite db is stored
     */
    private final static String DB_FILE = "users.db";

    /**
     * A singleton for the {@link UserDatabase}
     */
    private static UserDatabase instance = null;

    public static UserDatabase getInstance() throws SQLException {
        if (instance == null){
            instance = new UserDatabase();
        }
        return instance;
    }

    private UserDatabase() {}

    public static void init() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            logger.severe("SQLite Driver is missing.");
            System.exit(-1);
        }

        Connection c = connect();
        Statement stmt;
        stmt = c.createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS USERS " +
                "(USERNAME TEXT PRIMARY KEY     NOT NULL," +
                " PASSWORD           TEXT    NOT NULL, " +
                " TOKEN            TEXT     NOT NULL);";
        stmt.executeUpdate(sql);
        stmt.close();

        c.commit();
        c.close();
    }

    private static Connection connect() throws SQLException {
        Connection con = DriverManager.getConnection("jdbc:sqlite:"+ DB_FILE);
        con.setAutoCommit(false);
        return con;
    }

    public boolean isUsernameAvailable(String username) throws SQLException {
        Connection c = null;
        PreparedStatement st = null;
        boolean available;
        try {
            c = connect();
            st = c.prepareStatement(
                    "SELECT USERNAME FROM USERS WHERE USERNAME = ?");
            st.setString(1, username);
            available = !st.executeQuery().next();
        } finally {
            if (st != null) {
                st.close();
            }
            if (c != null) {
                c.close();
            }
        }

        return available;
    }

    public void createUser(String username, String password, String token) throws SQLException {
        Connection c = null;
        PreparedStatement st = null;
        try {
            c = connect();
            st = c.prepareStatement(
                    "INSERT INTO USERS (USERNAME, PASSWORD, TOKEN) " +
                            "VALUES (?, ?, ?)");
            st.setString(1, username);
            st.setString(2, password);
            st.setString(3, token);
            st.executeUpdate();
            c.commit();
        } finally {
            if (st != null){
                st.close();
            }
            if (c != null){
                c.close();
            }
        }
    }

    /**
     * Check if the given credentials are valid.
     * @param username the username
     * @param password the password
     * @return false if the credentials are wrong, true otherwise.
     */
    public boolean checkCredentials(String username, String password) throws SQLException {
        boolean valid = false;
        Connection c = null;
        PreparedStatement st = null;
        try {
            c = connect();
            st = c.prepareStatement(
                    "SELECT USERNAME FROM USERS WHERE USERNAME = ? AND PASSWORD = ?");
            st.setString(1, username);
            st.setString(2, password);

            ResultSet set = st.executeQuery();
            valid = set.next();
        } finally {
            if (st != null) {
                st.close();
            }
            if (c != null) {
                c.close();
            }
        }
        return valid;
    }

    public String getToken(String username) throws SQLException {
        String token = null;
        Connection c = null;
        PreparedStatement st = null;
        try {
            c = connect();
            st = c.prepareStatement(
                    "SELECT TOKEN FROM USERS WHERE USERNAME = ?");
            st.setString(1, username);

            ResultSet set = st.executeQuery();
            if (set.next()) {
                token = set.getString("TOKEN");
            }
        } finally {
            if (st != null) {
                st.close();
            }
            if (c != null) {
                c.close();
            }
        }
        return token;
    }

    public String getUsername(String token) throws SQLException {
        Connection c = null;
        PreparedStatement st = null;
        String username = null;
        try {
            c = connect();
            st = c.prepareStatement(
                    "SELECT USERNAME FROM USERS WHERE TOKEN = ?");
            st.setString(1, token);
            ResultSet set = st.executeQuery();
            if (set.next()){
                username = set.getString("USERNAME");
            }
        } finally {
            if (st != null) {
                st.close();
            }
            if (c != null) {
                c.close();
            }
        }
        return username;
    }

    public boolean updateToken(String username, String token) throws SQLException {
        Connection c = null;
        PreparedStatement st = null;
        try {
            c = connect();
            st = c.prepareStatement(
                    "UPDATE USERS " +
                            "SET TOKEN = ?" +
                            "WHERE USERNAME = ?");
            st.setString(1, token);
            st.setString(2, username);
            int count = st.executeUpdate();
            c.commit();

            return (count == 1);
        } finally {
            if (st != null){
                st.close();
            }
            if (c != null){
                c.close();
            }
        }
    }
}
