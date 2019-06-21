package jp.co.camnet.cmlb.connector;

import jp.co.camnet.cmlb.Cmlb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RequestConnection extends AbstractConnection implements Runnable {

    public RequestConnection(InputStream inputStream, OutputStream outputStream) {
        super(inputStream, outputStream);
    }

    protected int contentLength = -1;

    public void run() {
        try {
            headerPhase();
            bodyphase();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            Cmlb.logger.trace(outputStream.toString());
        }
    }

    protected void headerPhase() throws IOException {
        // 改行が続くと終わり
        // \r\nまで読み込む
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int previous1 = -1;
        int previous2 = -1;
        {//KeepAliveを無効にする
            String header = "Connection: Close\r\n";
            outputStream.write(header.getBytes());
        }
        {//X-Forwarded-*を設定する
            //TODO
        }
        while (true) {
            int data = inputStream.read();
            if (data == -1) {
                return;
            }
            os.write(data);


            if (data == '\n') {
                if (previous1 == '\n' || previous2 == '\n') {
                    //      os.write(data);
                    writeOs(os.toByteArray());
                    break;
                }
                byte[] byteArray = os.toByteArray();
                os.reset();
                String str = new String(byteArray);
                String[] parts = str.split(":", 2);
                String name = parts[0];
                String value = parts[1].trim();

                if (name.equalsIgnoreCase("Connection")) {
                    //無効にする
                } else if (name.equalsIgnoreCase("Host")) {
                    //X-Forward-Forに変換
                    //Hostの書き換え
                    //String header = "Host: " + " Close\r\n";
                    //outputStream.write(header.getBytes());
                    //TODO 送信先に変更
                    writeOs(byteArray);
                } else if (name.equalsIgnoreCase("Content-Length")) {
                    //Content-Lengthの保存
                    contentLength = Integer.parseInt(value, 10);
                    writeOs(byteArray);
                } else {
                    writeOs(byteArray);
                }
            }
            previous2 = previous1;
            previous1 = data;
        }
    }

    protected void bodyphase() throws IOException {
        if (contentLength <= 0) {
            return;
        }
        int data;
        int readLength = 0;
        do {
            data = send();
            flush();
            if (data > -1) {
                readLength += data;
            }
            if (readLength >= contentLength) {
                break;
            }
        } while (data != -1);
    }


}