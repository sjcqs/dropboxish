package com.dropboxish.client.command;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by satyan on 11/21/17.
 * The command parser
 * Allows to add and execute commands
 */
public class CommandParser {
    private static final Pattern SPLIT_PATTERN = Pattern.compile("((?:[^\\\\ ]+\\\\\\s+)+[^\\\\ ]+|\\S+)");
    /**
     * A {@link Map} to store every possible {@link Command}
     */
    private final Map<String, Command> commands;

    /**
     * A {@link BufferedReader} to read commands from an input
     */
    private final BufferedReader reader;

    private CommandParser(BufferedReader in){
        reader = in;
        commands = new HashMap<>();
    }

    public CommandParser(BufferedReader in, Command[] commands, HelpCommand helpCommand){
        this(in);
        for (Command command : commands) {
            this.commands.put(command.getName(),command);
        }
        this.commands.put("help", helpCommand);
    }

    public Command readCommand() throws IOException {
        String input = reader.readLine();
        Command cmd = null;

        if (input != null){
            input = input.trim();
            Matcher matcher = SPLIT_PATTERN.matcher(input);
            List<String> args = new ArrayList<>();

            while (matcher.find()){
                args.add(matcher.group());
            }
            cmd = parse(args);
        }

        return cmd;
    }

    private Command parse(List<String> args){
        Command command = commands.get(args.get(0));
        if (command == null){
            command = new CommandNotFound();
        }
        command.clearArgs();
        if (args.size() > 1){
            command.setArgs(args.subList(1,args.size()));
        }
        return command;
    }
}
