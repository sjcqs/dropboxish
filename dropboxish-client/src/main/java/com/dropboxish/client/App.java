package com.dropboxish.client;

import com.dropboxish.client.utils.ConsoleUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by satyan on 11/17/17.
 * Client application
 */
public class App {

    public static void main(String[] args){
        if (args.length != 1){
            ConsoleUtils.printError("Dropboxish address expected");
            System.exit(-1);
        }
        String host = args[0];

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            Client client = Client.getInstance(reader, host, 8080);
            client.run();
        } catch (IOException e) {
            ConsoleUtils.printError("Error while reading the input.");
        }

        System.exit(0);
    }
}
