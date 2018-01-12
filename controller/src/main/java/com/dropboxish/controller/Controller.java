package com.dropboxish.controller;

import com.dropboxish.controller.proto.Leader;
import com.dropboxish.controller.state.FileInfo;
import com.dropboxish.controller.state.FilesMap;
import io.grpc.stub.StreamObserver;
import org.jgroups.*;
import org.jgroups.util.Util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private static final int STATE_SIZE = 100;
    private Path file = null;
    private JChannel channel;
    private final List<FilesMap> state = new ArrayList<>(STATE_SIZE);
    private boolean isLeader = false;
    private StreamObserver<Leader> leaderObserver = null;
    private FilesMap lastMap = new FilesMap();

    public Controller(int port) {
        file = Paths.get(String.format("files-map-%d.store",port));
    }

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
        Address address = channel.getView().getMembers().get(0);
        if (address.equals(channel.address()) && !isLeader){
            isLeader = true;
            logger.info("Leader");
            FilesMap map = restore();
            if (lastMap.compareTo(map) <= 0){
                lastMap = map;
            }
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
        logger.info("Receive a message");
        // Message not by me
        if (msg.getObject() instanceof FilesMap && !msg.getSrc().equals(channel.address())) {
            FilesMap filesMap = msg.getObject();
            lastMap = filesMap;
            try {
                commit();
            } catch (IOException e) {
                logger.warning(e.getMessage());
                e.printStackTrace();
            }
            String line = msg.getSrc() + ": " + filesMap;
            logger.info(line);
        } else {
            String line = msg.getSrc() + ": " + msg.getObject();
            logger.info(line);
        }

    }

    private void commit() throws IOException {
        synchronized (state) {
            state.add(lastMap);
            while (state.size() > 1){
                state.remove(0);
            }
        }
        try(ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(file))){
            out.writeObject(lastMap);
        }
    }

    private FilesMap restore() {
        logger.info("RESTORE");
        if (Files.exists(file)) {
            logger.info("File exists.");
            try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(file))) {
                FilesMap map = (FilesMap) in.readObject();
                logger.info("Map:" + map);
                return map;
            } catch (IOException | ClassNotFoundException e) {
                logger.warning(e.getMessage());
                e.printStackTrace();
            }
        }
        return new FilesMap();
    }

    @Override
    public void viewAccepted(View view) {
        logger.info("! view: " + view);
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
        List<FilesMap> list;
        list = Util.objectFromStream(new DataInputStream(input));
        synchronized (state) {
            if (!list.isEmpty()) {
                state.clear();
                state.addAll(list);
                if (state.size() > 0) {
                    lastMap = state.get(0);
                }
            }
        }
        logger.info("! history: " + list.size());
        list.forEach(System.out::println);
    }

    public void addFile(FileInfo info) throws Exception {
        lastMap.add(info);
        update();
    }

    public FileInfo removeFile(String checksum) throws Exception {
        FileInfo info = lastMap.remove(checksum);
        update();

        return info;
    }

    private void update() throws Exception {
        logger.info("UPDATE");
        channel.send(new Message(null, lastMap));
        commit();
    }

    public void addClient(StreamObserver<Leader> responseObserver) {
        this.leaderObserver = responseObserver;
        if (isLeader){
            responseObserver.onNext(Leader.getDefaultInstance());
        }
    }

    public FileInfo getFileInfo(String filename, String owner) {
        return lastMap.get(filename, owner);
    }

    public boolean fileExists(String filename, String owner, String checksum){
        FileInfo info = lastMap.get(filename, owner);
        if (info != null){
            return info.getChecksum().equals(checksum);
        }

        return false;
    }
}
