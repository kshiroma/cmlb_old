package jp.co.camnet.cmlb.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ResponseConnection extends AbstractConnection implements Runnable {
    public ResponseConnection(InputStream inputStream, OutputStream outputStream) {
        super(inputStream, outputStream);
    }

    public void run() {
        try {
            run0();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Server connection to client closed");
        }
    }

    private void run0() throws IOException {
        while (!Thread.interrupted()) {
            int data = send();
            if (data == -1) {
                return;
            }
            flush();
        }
    }
}
