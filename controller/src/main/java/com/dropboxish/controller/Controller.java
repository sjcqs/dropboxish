package com.dropboxish.controller;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by satyan on 12/12/17.
 * Controller part
 */
public class Controller extends ReceiverAdapter {
    private static final int MAX_SIZE = 1000;
    private JChannel channel;
    private final List<String> state = new ArrayList<>(1000);

    public void start() throws Exception{
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream("jgroups-config.xml");
        channel = new JChannel(stream).setReceiver(this);
        channel.connect("ChatCluster");
        channel.getState(null, 10000);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may has been reset by its JVM shutdown hook.
            System.err.println("Shutting down jgroups server");
            Controller.this.stop();
            System.err.println("Server shut down.");
        }));
    }

    private void stop() {
        channel.close();
    }

    @Override
    public void receive(Message msg) {
        String line = msg.getSrc() + ": " + msg.getObject();
        System.out.println(line);
        System.out.println();
        synchronized (state) {
            state.add(line);
            if (state.size() > MAX_SIZE){
                state.remove(0);
            }
        }

    }

    @Override
    public void viewAccepted(View view) {
        System.out.println("! view: " + view);
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized (state){
            Util.objectToStream(state, new DataOutputStream(output));
        }
    }

    @Override
    public void setState(InputStream input) throws Exception {
        List<String> list;
        list = Util.objectFromStream(new DataInputStream(input));
        synchronized (state) {
            state.clear();
            state.addAll(list);
        }
        System.out.println("! history: " + list.size());
        list.forEach(System.out::println);
    }

    public void sendMessage(Message msg) throws Exception {
        channel.send(msg);
    }

}
