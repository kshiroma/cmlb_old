package jp.co.camnet.cmlb.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AbstractConnection {
    protected static final int BUFFER_SIZE = 4096;
    protected final InputStream inputStream;
    protected final OutputStream outputStream;
    protected final byte[] inputStreamReadingBuffer = new byte[BUFFER_SIZE];
    //ByteArrayOutputStream os = new ByteArrayOutputStream();

    protected AbstractConnection(
            final InputStream inputStream,
            final OutputStream outputStream
    ) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public int send() throws IOException {
        int data = inputStream.read(inputStreamReadingBuffer);
        if (data == -1) {
            return -1;
        }
        writeOs(inputStreamReadingBuffer, data);
        return data;
    }

    public void flush() throws IOException {
        outputStream.flush();
    }

    protected void writeOs(byte[] byteArray) throws IOException {
        outputStream.write(byteArray);
        //os.write(byteArray);
    }

    protected void writeOs(byte[] byteArray, int length) throws IOException {
        outputStream.write(byteArray, 0, length);
        //os.write(byteArray, 0, length);
    }


}
