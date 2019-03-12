package jp.co.camnet.cmlb;

import org.eclipse.jetty.server.Server;

public class WebServer {

    public static void main(String[] args) throws Exception {
        makeServer(8080);
        makeServer(8081);
        makeServer(8082);
        makeServer(8083);

    }

    public static Server makeServer(int port) throws Exception {
        Server server2 = new Server(port);
        server2.setHandler(new JettyHandler());
        server2.start();
        //server2.join();
        return server2;
    }

}


