package com.dropboxish.client.utils;

/**
 * Created by satyan on 11/21/17.
 * Utils for the console
 */
public class ConsoleUtils {
    public static final String ANSI_PLAIN_TEXT = "\033[0;0m";
    public static final String ANSI_BOLD_TEXT = "\033[0;1m";

    public static void printAppInfo() {
        String str = ANSI_BOLD_TEXT + "Dropboxish" + ANSI_PLAIN_TEXT + "\n";
        str += "FCUP - Distributed Systems 2017/2018\n";
        str += "This storage service is inspired by Dropbox.\n" +
                "The idea is to allow the user to put, get and delete files from the cloud.";
        System.out.println(str);
    }

    public static void printLines(String title, String...lines){
        String body = String.join("\n\t\t",lines);
        System.out.println(String.format("\t%s%n\t\t%s",title,body));
    }


    public static void printTitle(String title) {
        System.out.println(ANSI_BOLD_TEXT + title + ANSI_PLAIN_TEXT );
    }

    public static void printError(String...lines){
        String body = String.join("\n\t",lines);
        printTitle("ERROR");
        System.out.println("\t" + body);
    }
}
