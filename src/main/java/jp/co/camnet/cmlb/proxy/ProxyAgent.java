package jp.co.camnet.cmlb.proxy;

import jp.co.camnet.cmlb.http.HttpRequestFirstLine;

import java.net.Socket;

public interface ProxyAgent {
    public byte[] read();

    public void close();


    public Socket getSocketToServer();

    public void writeFirstLine(HttpRequestFirstLine firstLine);


}
