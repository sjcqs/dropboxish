package com.dropboxish.client;

import com.dropboxish.client.utils.ConsoleUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by satyan on 11/17/17.
 * User application
 */
public class App {

    public static void main(String[] args){
        if (args.length != 1){
            ConsoleUtils.printError("Dropboxish address expected");
            System.exit(-1);
        }
        String host = args[0];

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            User user = User.getInstance(reader, host, 8080);
            user.run();
        } catch (IOException e) {
            ConsoleUtils.printError("Error while reading the input.");
        }

        System.exit(0);
    }
}
