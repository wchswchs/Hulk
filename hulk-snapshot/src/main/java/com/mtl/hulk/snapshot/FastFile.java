package com.mtl.hulk.snapshot;

import com.mtl.hulk.serializer.HulkSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class FastFile {

    private static final Logger logger = LoggerFactory.getLogger(FastFile.class);

    private FileChannel fileChannel;
    private int bufferlen;
    private RandomAccessFile file;
    private AtomicLong startPosition = new AtomicLong(0);
    private AtomicLong readStartPosition = new AtomicLong(0);

    public FastFile(File file, String mode, int bufferlen) {
        try {
            this.bufferlen = bufferlen;
            this.file = new RandomAccessFile(file, mode);
            this.fileChannel = this.file.getChannel();
            this.startPosition.set(file.length());
        } catch (FileNotFoundException ex) {
            logger.error("File Not Found", ex);
        }
    }

    public <T> List<T> read(HulkSerializer serializer, Class<T> targetClass) throws Exception {
        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferlen);
        int bytes = 0;
        List<T> datas = new ArrayList<>();
        while (true) {
            if (getReadStartPosition() >= fileChannel.size()) {
                return datas;
            }
            bytes = fileChannel.read(byteBuffer, getReadStartPosition());
            if (bytes <= bufferlen) {
                byteBuffer.flip();
                datas.add((T) serializer.deserialize(byteBuffer.array(), targetClass));
            }
            setReadStartPosition(Long.valueOf(bytes) + getReadStartPosition());
        }
    }

    public int write(byte[] bytes) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(bufferlen);
            byteBuffer.clear();
            byteBuffer.put(bytes);
            byteBuffer.clear();
            return fileChannel.write(byteBuffer);
        } catch (IOException ex) {
            logger.error("Write File Error", ex);
        }
        return -1;
    }

    public int write(byte[] bytes, long startPosition) {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(bufferlen);
            byteBuffer.clear();
            byteBuffer.put(bytes);
            byteBuffer.clear();
            return fileChannel.write(byteBuffer, startPosition);
        } catch (IOException ex) {
            logger.error("Write File Error", ex);
        }
        return -1;
    }

    public void setStartPosition(Long startPosition) {
        this.startPosition.set(startPosition);
    }

    public Long getStartPosition() {
        return startPosition.get();
    }

    public void setReadStartPosition(Long readStartPosition) {
        this.readStartPosition.set(readStartPosition);
    }

    public Long getReadStartPosition() {
        return readStartPosition.get();
    }

    public FileChannel getFileChannel() {
        return fileChannel;
    }

    public RandomAccessFile getFile() {
        return file;
    }

}
