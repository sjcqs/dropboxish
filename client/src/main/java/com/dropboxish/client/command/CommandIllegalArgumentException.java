package com.dropboxish.client.command;

/**
 * Created by satyan on 11/30/17.
 *
 */
public class CommandIllegalArgumentException extends IllegalArgumentException {
    private String[] lines;

    public CommandIllegalArgumentException(){}

    public CommandIllegalArgumentException(String...lines) {
        super(lines[0]);
        this.lines = lines;
    }

    public String[] getLines() {
        return lines;
    }
}
