package com.dropboxish.client.command;

import com.dropboxish.client.User;
import com.dropboxish.client.utils.ConsoleUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by satyan on 12/6/17.
 */
public class LSCommand extends Command {

    public LSCommand(User user) {
        super("ls", user);
    }

    @Override
    public void run() {
        List<String> command = new ArrayList<>();
        command.add("ls");
        if (args != null) {
            command.addAll(args);
        }

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        Process process;
        BufferedReader reader = null;
        try {
            process = processBuilder.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            boolean endReach = false;
            while (!endReach) {
                String input = reader.readLine();
                if (input != null) {
                    ConsoleUtils.println(input);
                } else {
                    endReach = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void help() {
        ConsoleUtils.printShifted("ls [OPTION]... [FILE]...", "Call GNU ls command.");

    }
}
