package com.dropboxish.controller.grpc;

import com.dropboxish.controller.Controller;
import com.dropboxish.controller.fec.FECUtil;
import com.dropboxish.controller.proto.*;
import com.dropboxish.controller.state.Block;
import com.dropboxish.controller.state.BlockInfo;
import com.dropboxish.controller.state.ChunkInfo;
import com.dropboxish.controller.state.FileInfo;
import com.dropboxish.model.Host;
import com.dropboxish.model.utils.FileUtil;
import com.dropboxish.pool.grpc.PoolClient;
import com.dropboxish.pool.grpc.RpcServer;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Created by satyan on 12/11/17.p
 * GRPC service
 */
public class ControllerService extends ControllerGrpc.ControllerImplBase {
    private  static final Logger logger = Logger.getLogger("ControllerService");
    private final Controller controller;
    private final List<PoolClient> pools;
    private final Map<Host, PoolClient> hostPoolMap = new HashMap<>();

    private static Path getPath(String name){
        return Paths.get(name + ".tmp");
    }

    public ControllerService(Controller controller, List<PoolClient> pools) {
        this.controller = controller;
        this.pools = pools;
        for (PoolClient pool : pools) {
            hostPoolMap.put(pool.getHost(), pool);
        }
    }

    @Override
    public StreamObserver<File> putFile(StreamObserver<OperationStatus> responseObserver) {
        final Metadata.Builder builder = Metadata.newBuilder();
        return new StreamObserver<File>() {
            @Override
            public void onNext(File file) {
                Metadata metadata;
                switch (file.getFileOneofCase()){
                    case METADATA:
                        metadata = file.getMetadata();
                        builder.setFilename(metadata.getFilename())
                                .setOwner(metadata.getOwner())
                                .setChecksum(metadata.getChecksum())
                                .setLength(metadata.getLength());
                        break;
                    case DATA:
                        metadata = builder.build();
                        Data data = file.getData();
                        try (OutputStream out = Files.newOutputStream(
                                getPath(metadata.getFilename()), StandardOpenOption.APPEND, StandardOpenOption.CREATE)){
                            out.write(data.getData().toByteArray(), 0, data.getLength());
                        } catch (IOException e) {
                            responseObserver.onError(e);
                        }
                        break;
                    case FILEONEOF_NOT_SET:
                        break;
                }
            }

            @Override
            public void onError(Throwable t) {
                try {
                    Files.deleteIfExists(getPath(builder.build().getFilename()));
                } catch (IOException e) {
                    logger.warning(e.getMessage());
                    e.printStackTrace();
                }
                responseObserver.onError(t);
            }

            @Override
            public void onCompleted() {
                Metadata metadata = builder.build();
                String checksum = metadata.getChecksum();
                Path path = getPath(metadata.getFilename());
                if (!FileUtil.check(path, checksum)){
                    logger.info("File NOT uploaded: " + metadata.getFilename() + " " + checksum + " | " + FileUtil.checksum(path));
                    try {
                        Files.deleteIfExists(getPath(builder.build().getFilename()));
                    } catch (IOException e) {
                        logger.warning(e.getMessage());
                        e.printStackTrace();
                    }
                    responseObserver.onError(new IOException("Error receiving file"));
                } else {
                    logger.info("File uploaded: " + metadata.getFilename() + " " + checksum);
                    if (!controller.fileExists(metadata.getFilename(), metadata.getOwner(), metadata.getChecksum())) {
                        FileInfo info =
                                sendToPools(path, metadata.getFilename(), metadata.getChecksum(), metadata.getOwner(), metadata.getLength());
                        if (info != null) {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException e) {
                                logger.warning(e.getMessage());
                            }
                            try {
                                controller.addFile(info);
                                responseObserver.onNext(OperationStatus.newBuilder()
                                        .setStatus(OperationStatus.Status.OK)
                                        .build());
                                logger.info(info.toString());
                                responseObserver.onCompleted();
                            } catch (Exception e) {
                                try {
                                    Files.deleteIfExists(getPath(builder.build().getFilename()));
                                } catch (IOException e1) {
                                    logger.warning(e1.getMessage());
                                    e1.printStackTrace();
                                }
                                logger.warning(e.getMessage());
                                responseObserver.onError(new IOException("File not uploaded"));
                            }

                        } else {
                            try {
                                Files.deleteIfExists(getPath(builder.build().getFilename()));
                            } catch (IOException e) {
                                logger.warning(e.getMessage());
                                e.printStackTrace();
                            }
                            responseObserver.onError(new IOException("Unable to upload blocks."));
                        }
                    } else {
                        try {
                            Files.deleteIfExists(getPath(builder.build().getFilename()));
                        } catch (IOException e) {
                            logger.warning(e.getMessage());
                            e.printStackTrace();
                        }
                        logger.info("File already exist");
                        responseObserver.onNext(OperationStatus.newBuilder()
                                .setStatus(OperationStatus.Status.OK)
                                .build());
                        responseObserver.onCompleted();
                    }
                }
            }
        };
    }

    private FileInfo sendToPools(Path file, String filename, String checksum, String owner, long length) {
        FileInfo info = new FileInfo(owner, filename,checksum, length);
        try {
            Path chunks[] = FileUtil.chunks(file);
            for (int i = 0; i < chunks.length; i++) {
                Path path = chunks[i];
                ChunkInfo chunk = new ChunkInfo(i, FileUtil.checksum(path), Files.size(path));
                Block[] blocks = FECUtil.encode(path, pools.size());
                List<PoolClient> list = new ArrayList<>(pools);
                Random rand = new Random();
                for (Block block : blocks) {
                    if (block != null) {
                        int index = rand.nextInt(list.size());
                        PoolClient pool = list.remove(index);
                        if (pool.putBlock(
                                block.getBuffer(),
                                com.dropboxish.pool.proto.Metadata.newBuilder()
                                        .setChecksum(block.getChecksum())
                                        .setLength(block.getLength())
                                        .build())){
                            logger.info("Block sent to " + pool.getHost());
                            chunk.putBlock(block.getInfo(), pool.getHost());
                        } else {
                            logger.warning("Block could'nt be sent to " + pool.getHost());
                            return null;
                        }
                    }
                }
                info.addChunk(chunk);
            }
        } catch (IOException e) {
            return null;
        }
        return info;
    }

    @Override
    public void getFile(Metadata request, StreamObserver<File> responseObserver) {
        logger.info("Getting a file:" + request.getOwner() + ":" + request.getFilename());
        try {
            Path path = Files.createTempFile(request.getFilename(), null);
            FileInfo info = controller.getFileInfo(request.getFilename(), request.getOwner());
            if (info == null){
                logger.warning("Doesn't exist" + request.getOwner() + ":" + request.getFilename());
                responseObserver.onError(new FileNotFoundException("File doesn't exists"));
            } else {
                for (ChunkInfo chunk : info.getChunks()) {
                    logger.info("Chunk: " + chunk);
                    appendChunk(chunk, path);
                }
                logger.info("OVER");
                if (FileUtil.check(path, request.getChecksum())) {
                    sendFile(path, responseObserver);
                } else {
                    Files.copy(path, Paths.get(request.getFilename() + ".error"));
                    logger.warning("File corrupted.");
                    responseObserver.onError(new FileNotFoundException("File corrupted."));
                }
            }
        } catch (IOException e) {
            responseObserver.onError(e);
        }
    }

    private void appendChunk(ChunkInfo chunk, Path path) throws IOException {
        List<BlockInfo> infos = chunk.getBlocks();
        int count = pools.size();
        Block[] blocks = new Block[count];
        ExecutorService service = Executors.newFixedThreadPool(count);
        for (BlockInfo block : infos) {
            String checksum = block.getChecksum();
            PoolClient client = null;
            for (PoolClient pool : pools) {
                if (pool.getHost().equals(chunk.getHost(checksum))){
                    client = pool;
                }
            }
            if (client != null) {
                ByteBuffer buffer = ByteBuffer.allocate(block.getLength());
                blocks[block.getIndex()] = new Block(block, buffer);
                try {
                    Runnable runnable = client.getBlock(buffer, checksum);
                    service.execute(runnable);
                } catch (Exception e){
                    logger.warning(e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        service.shutdown();
        try {
            service.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.info(e.getMessage());
            Thread.currentThread().interrupt();
        }

        if(FECUtil.recover(blocks)) {
            try (OutputStream in = Files.newOutputStream(path, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
                for (Block block : blocks) {
                    if (block.getType().equals(BlockInfo.Type.DATA)){
                        logger.info("Block lenght:" + block.getRealLength());
                        in.write(block.getBuffer().array(), 0, block.getRealLength());
                    }
                }
            }
        } else {
            logger.warning("Missing blocks");
            throw new IOException("Missing blocks");
        }
    }

    public static void sendFile(Path path, StreamObserver<File> responseObserver) throws IOException {
        byte[] bytes = new byte[RpcServer.GRPC_MAX_SIZE];
        try (InputStream in = Files.newInputStream(path, StandardOpenOption.DELETE_ON_CLOSE)) {
            int read;
            while ((read = in.read(bytes, 0, RpcServer.GRPC_MAX_SIZE)) != -1) {
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                Data data = Data.newBuilder()
                        .setData(ByteString.copyFrom(buffer))
                        .setLength(read)
                        .build();
                File file = File.newBuilder()
                        .setData(data)
                        .build();
                responseObserver.onNext(file);
            }
            responseObserver.onCompleted();
            logger.info("File sent.");
        }
    }

    @Override
    public void deleteFile(Metadata request, StreamObserver<OperationStatus> responseObserver) {
        logger.info("Trying to remove: " + request.getFilename());
        try {
            FileInfo info = controller.removeFile(request.getFilename(), request.getOwner());
            if (info == null){
                logger.warning("File doesn't exist");
                responseObserver.onError(new FileNotFoundException("The file doesn't exists."));
            } else {
                for (ChunkInfo chunk : info.getChunks()) {
                    for (BlockInfo block : chunk.getBlocks()) {
                        String checksum = block.getChecksum();
                        PoolClient client = hostPoolMap.get(chunk.getHost(checksum));
                        if(!client.deleteBlock(checksum)){
                            logger.warning("Block cannot be removed.");
                            responseObserver.onError(new IOException("Block couldn't be removed."));
                            return;
                        }
                    }
                }
                responseObserver.onNext(OperationStatus.newBuilder().setStatus(OperationStatus.Status.OK).build());
                responseObserver.onCompleted();
            }
        } catch (Exception e) {
            logger.warning(e.getMessage());
            responseObserver.onError(e);
        }
    }

    @Override
    public void getLeader(Subscription request, StreamObserver<Leader> responseObserver) {
        controller.addClient(responseObserver);
    }
}
