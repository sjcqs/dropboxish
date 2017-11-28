package com.dropboxish.client;

/**
 * Created by satyan on 11/17/17.
 * Client application
 */
public class App {

    public static void main(String[] args){
        Client client = Client.getInstance();
        client.run();
    }
}
