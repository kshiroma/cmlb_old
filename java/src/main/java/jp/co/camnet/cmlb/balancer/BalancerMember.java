package jp.co.camnet.cmlb.balancer;

import jp.co.camnet.cmlb.http.HttpRequestFirstLine;
import jp.co.camnet.cmlb.proxy.ProxyAgent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;

public class BalancerMember implements ProxyAgent {

    private final String host;
    private final String scheme;
    private final String path;
    private final int port;
    private final String destination;

    private final Socket socket;

    public BalancerMember(String destination) {
        this.destination = destination;
        URI uri = URI.create(destination);
        this.scheme = uri.getScheme();
        this.host = uri.getHost();
        this.path = uri.getPath();
        this.port = uri.getPort();


        this.socket = newSocket(host, port);
    }


    private static Socket newSocket(String destination, int port) {
        try {
            if (port == -1) {
                port = 80;
            }
            Socket sock = new Socket(destination, port);
            //sock.setTcpNoDelay(true);
            //sock.setSoTimeout(3000);
            //sock.setKeepAlive(false);
            return sock;
        } catch (IOException e) {
            return null;
        }
    }

    public String getDestination() {
        return destination;
    }


    /**
     * 改行まで
     *
     * @return
     */
    @Override
    public byte[] read() {
        try {
            return read0();
        } catch (IOException e) {
            new RuntimeException(e);
        }
        return new byte[0];
    }

    public void writeFirstLine(HttpRequestFirstLine firstLine) {
        String method = firstLine.getMethod();
        String path = firstLine.getUri();
        String protocolVersion = firstLine.getProtocolVersion();
        StringBuffer sb = new StringBuffer();
        sb.append(method)
                .append(" ")
                .append(path)
                .append(" ")
                .append(protocolVersion)
                .append("\r\n");
        write(sb.toString().getBytes());
    }


    public void write(byte[] data) {
        try {
            write0(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void write0(byte[] data) throws IOException {
        OutputStream os = socket.getOutputStream();
        os.write(data);
    }


    private byte[] read0() throws IOException {
        InputStream is = socket.getInputStream();
        ByteArrayOutputStream bao = null;
        byte[] data = new byte[1];
        int length = 0;
        while (length != -1) {
            length = is.read();
            if (length != -1) {
                byte b = (byte) length;
                if (bao == null) {
                    bao = new ByteArrayOutputStream();
                }
                bao.write(b);
            }
        }
        if (bao == null) {
            return null;
        }
        return bao.toByteArray();
    }


    @Override
    public void close() {
    }

    @Override
    public Socket getSocketToServer() {
        return socket;
    }


}
