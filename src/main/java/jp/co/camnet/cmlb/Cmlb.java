package jp.co.camnet.cmlb;

import jp.co.camnet.cmlb.config.CmlbConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Cmlb implements Runnable {
    public static Logger logger = LoggerFactory.getLogger(Cmlb.class);


    private final ServerSocket listenSock;

    private final CmlbConfig config;

    public Cmlb(CmlbConfig config) throws IOException {
        this.config = config;
        try {
            this.listenSock = new ServerSocket(config.serverPort);
        } catch (IOException ex) {
            System.err.println("Error opening listening port");
            throw ex;
        }
    }


    @Override
    public void run() {
        System.out.println("Listening for connections");
        while (!Thread.interrupted()) {
            try {
                Socket newSock = listenSock.accept();
                logger.info(String.format("access Listen port = [%s] ", newSock.getPort()));
                CmlbConnection newCon = new CmlbConnection(config, newSock);
                Thread newThread = new Thread(newCon);
                newThread.start();
            } catch (Exception e) {
                logger.error("Server listen error", e);
                break;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        //String fileName = "work/cmlb.xml";
        CmlbConfig config = new CmlbConfig();//JAXB.unmarshal(new File(fileName), CmlbConfig.class);
        Cmlb cmlb = new Cmlb(config);
        Thread listenerThread = new Thread(cmlb);
        listenerThread.run();
    }

}
