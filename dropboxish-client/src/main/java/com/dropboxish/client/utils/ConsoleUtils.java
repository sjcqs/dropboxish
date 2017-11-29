package com.dropboxish.client.utils;

/**
 * Created by satyan on 11/21/17.
 * Utils for the console
 */
public class ConsoleUtils {
    private static final String ANSI_PLAIN_TEXT = "\033[0;0m";
    private static final String ANSI_BOLD_TEXT = "\033[0;1m";

    public static void printAppInfo() {
        String str = ANSI_BOLD_TEXT + "Dropboxish" + ANSI_PLAIN_TEXT + "\n";
        str += "FCUP - Distributed Systems 2017/2018\n";
        str += "This storage service is inspired by Dropbox.\n" +
                "The idea is to allow the user to put, get and delete files from the cloud.";
        System.out.println(str);
    }

    public static void printShifted(String title, String...lines){
        printTitle("\t" + title);
        printLines(2, lines);
    }


    public static void printTitle(String title) {
        System.out.println(ANSI_BOLD_TEXT + title + ANSI_PLAIN_TEXT );
    }

    public static void printError(String...lines){
        printTitle("ERROR");
        printLines(1, lines);
    }

    public static void printDebug(String...lines) {
        printTitle("DEBUG");
        printLines(1, lines);
    }

    private static void printLines(int shift, String... lines){
        String separator = "";
        for (int i = 0; i < shift; i++) {
            separator += "\t";
        }
        String body = String.join("\n" + separator,lines);
        System.out.println(separator + body);
    }

    public static void printPrompt(String prompt) {
        System.out.print(prompt + " ");
    }

    public static void print(String title, String...lines) {
        printTitle(title);
        printLines(1,lines);
    }
}
