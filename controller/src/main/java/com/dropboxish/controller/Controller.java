package com.dropboxish.controller;

import com.dropboxish.controller.state.Update;
import com.dropboxish.controller.proto.*;
import io.grpc.stub.StreamObserver;
import org.jgroups.*;
import org.jgroups.Address;
import org.jgroups.util.Util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by satyan on 12/12/17.
 * Controller part
 */
public class Controller extends ReceiverAdapter {
    private final static Logger logger = Logger.getLogger("Controller");
    private static final long SLEEP_TIME = 100;
    private JChannel channel;
    private final List<Update> state = new ArrayList<>();
    private boolean isLeader = false;
    private StreamObserver<Leader> leaderObserver = null;

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

        checkLeader();
        while(!isLeader){
            Thread.sleep(SLEEP_TIME);
            checkLeader();
        }
    }

    private void checkLeader() {
        Address addr = channel.getView().getMembers().get(0);
        if (addr.equals(channel.address()) && !isLeader){
            isLeader = true;
            logger.info("Leader");
            if (leaderObserver != null){
                leaderObserver.onNext(Leader.getDefaultInstance());
            }
        } else {
            isLeader = false;
        }
    }

    private void stop() {
        isLeader = false;
        channel.close();
    }

    @Override
    public void receive(Message msg) {
        // Message not by me
        if (msg.getObject() instanceof Update && !msg.getSrc().equals(channel.address())) {
            Update update = msg.getObject();
            // TODO update local map
            String line = msg.getSrc() + ": " + update;
            logger.info(line);
            synchronized (state) {
                state.add(update);
            }
        } else {
            String line = msg.getSrc() + ": " + msg.getObject();
            logger.info(line);
        }

    }

    @Override
    public void viewAccepted(View view) {
        System.out.println("! view: " + view);
        for (Address address : view.getMembers()) {
            System.out.println(address + " " + address.compareTo(channel.address()));
        }
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized (state){
            Util.objectToStream(state, new DataOutputStream(output));
        }
    }

    @Override
    public void setState(InputStream input) throws Exception {
        List<Update> list;
        list = Util.objectFromStream(new DataInputStream(input));
        synchronized (state) {
            state.clear();
            state.addAll(list);
        }
        System.out.println("! history: " + list.size());
        list.forEach(System.out::println);
    }

    public void update(Update update) throws Exception {
        channel.send(new Message(null, update));
    }

    public void addClient(StreamObserver<Leader> responseObserver) {
        this.leaderObserver = responseObserver;
        if (isLeader){
            responseObserver.onNext(Leader.getDefaultInstance());
        }
    }
}
