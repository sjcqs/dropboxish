package com.dropboxish.db;

import com.dropboxish.model.FileInfo;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by satyan on 11/22/17.
 * Database where user's credentials are stored
 */
public class DropboxishDatabase {
    private final static Logger logger = Logger.getLogger("App");
    /**
     * File where the SQLite db is stored
     */
    private final static String DB_FILE = "users.db";

    /**
     * A singleton for the {@link DropboxishDatabase}
     */
    private static DropboxishDatabase instance = null;

    public static DropboxishDatabase getInstance() throws SQLException {
        if (instance == null){
            instance = new DropboxishDatabase();
        }
        return instance;
    }

    private DropboxishDatabase() {}

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
        String up0 = "CREATE TABLE IF NOT EXISTS User( " +
                "username TEXT PRIMARY KEY     NOT NULL," +
                "password           TEXT    NOT NULL, " +
                "token            TEXT     NOT NULL);";
        String up1 = "CREATE TABLE  IF NOT EXISTS File(" +
                "filename TEXT NOT NULL," +
                "checksum TEXT NOT NULL," +
                "size BIGINT NOT NULL CHECK (size > 0)," +
                "owner TEXT NOT NULL," +
                "PRIMARY KEY (filename, owner),"+
                "FOREIGN KEY(owner) REFERENCES User(username));";

        stmt.executeUpdate(up0);
        stmt.executeUpdate(up1);
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
                    "SELECT username FROM User WHERE username = ?");
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
                    "INSERT INTO User (username, password, token) " +
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
        boolean valid;
        Connection c = null;
        PreparedStatement st = null;
        try {
            c = connect();
            st = c.prepareStatement(
                    "SELECT username FROM User WHERE username = ? AND password = ?");
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
                    "SELECT token FROM User WHERE username = ?");
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
                    "SELECT username FROM User WHERE token = ?");
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
                    "UPDATE User " +
                            "SET token = ?" +
                            "WHERE username = ?");
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

    public int clearUsers() throws SQLException {
        Connection c = null;
        Statement st = null;
        try {
            c = connect();
            st = c.createStatement();

            int affected = st.executeUpdate("DELETE FROM User;");
            c.commit();
            return affected;
        } finally {
            if (st != null){
                st.close();
            }
            if (c != null){
                c.close();
            }
        }
    }

    public void putFile(String filename, String checksum, long size, String owner) throws SQLException {
        Connection c = connect();
        PreparedStatement update = c.prepareStatement(
                "UPDATE File SET checksum = ?, size = ?, owner = ? WHERE filename = ?");
        PreparedStatement insert = c.prepareStatement(
                "INSERT INTO File (filename, checksum, size, owner) VALUES (?, ?, ?, ?)");

        update.setString(4, filename);
        insert.setString(1, filename);
        update.setString(1, checksum);
        insert.setString(2, checksum);
        update.setLong(2, size);
        insert.setLong(3, size);
        update.setString(3, owner);
        insert.setString(4, owner);

        int affected = update.executeUpdate();
        if (affected == 0){
            affected = insert.executeUpdate();
            if (affected == 0){
                throw new SQLException("File wasn't added to database");
            }
            c.commit();
            insert.close();
        } else {
            c.commit();
        }

        update.close();
        c.close();
    }

    public List<FileInfo> listFiles(String owner) throws SQLException {
        Connection c = null;
        PreparedStatement statement = null;
        try {
            List<FileInfo> results;
            c = connect();
            statement = c.prepareStatement(
                    "SELECT * FROM File WHERE owner = ?"
            );
            statement.setString(1, owner);

            ResultSet set = statement.executeQuery();
            results = new LinkedList<>();
            while (set.next()) {
                FileInfo file = new FileInfo(
                        set.getString("filename"),
                        set.getString("checksum"),
                        set.getLong("size"),
                        set.getString("owner")
                );
                results.add(file);
            }
            return results;
        }finally {
            if (statement != null){
                statement.close();
            }
            if (c != null){
                c.close();
            }
        }
    }

    public Map<String, Boolean> removeFiles(String owner, List<String> names) throws SQLException{
        Connection con = null;
        List<PreparedStatement> statements = new ArrayList<>(names.size());
        Map<String, Boolean> results = new HashMap<>();
        try {
            con = connect();
            for (String name : names) {
                PreparedStatement st = con.prepareStatement(
                    "DELETE FROM File WHERE filename = ? AND owner = ?"
                );
                st.setString(1, name);
                st.setString(2, owner);

                int affected = st.executeUpdate();
                if (affected == 1){
                    results.put(name, true);
                } else {
                    results.put(name, false);
                }
                statements.add(st);
            }
            con.commit();
        } finally {
            for (PreparedStatement statement : statements) {
                statement.close();
            }
            if (con != null){
                con.close();
            }
        }

        return results;
    }

    public FileInfo getFile(String filename, String owner) throws SQLException{
        Connection c = null;
        PreparedStatement st = null;
        try {
            FileInfo result = null;
            c = connect();
            st = c.prepareStatement(
                    "SELECT * FROM File WHERE owner = ? AND filename = ?"
            );
            st.setString(1, owner);
            st.setString(2, filename);

            ResultSet set = st.executeQuery();
            if (set.next()) {
                result = new FileInfo(
                        set.getString("filename"),
                        set.getString("checksum"),
                        set.getLong("size"),
                        set.getString("owner")
                );
            }
            return result;
        }finally {
            if (st != null){
                st.close();
            }
            if (c != null){
                c.close();
            }
        }
    }
}
