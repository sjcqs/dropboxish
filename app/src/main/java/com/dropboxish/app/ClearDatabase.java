package com.dropboxish.app;

import com.dropboxish.db.DropboxishDatabase;

import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Created by satyan on 12/5/17.
 */
public class ClearDatabase {
    private final static Logger logger = Logger.getLogger("Clear Database");

    public static void main(String[] args) {
        try {
            DropboxishDatabase.init();
            DropboxishDatabase db = DropboxishDatabase.getInstance();
            int affected = db.clearUsers();
            logger.info(String.format("Removed %d users.", affected));
        } catch (SQLException e) {
            logger.severe("Database clearing failed.\n " + e.getMessage());
        }

    }
}
