package jp.co.camnet.cmlb;

import jp.co.camnet.cmlb.config.CmlbConfig;
import jp.co.camnet.cmlb.connector.RequestConnection;
import jp.co.camnet.cmlb.connector.ResponseConnection;
import jp.co.camnet.cmlb.http.BadRequest;
import jp.co.camnet.cmlb.http.HttpRequestFirstLine;
import jp.co.camnet.cmlb.proxy.ProxyAgent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class CmlbConnection implements Runnable {

    private final CmlbConfig config;
    private final Socket conSock;//クライアントからの接続
    private final OutputStream os;
    private final InputStream is;

    public CmlbConnection(CmlbConfig config, Socket conSock) throws IOException {
        this.config = config;
        this.conSock = conSock;
        os = conSock.getOutputStream();
        is = conSock.getInputStream();
    }

    @Override
    public void run() {
        CmlbRouter router = new CmlbRouter(config);
        try {
            String firstLine = readLine();
            HttpRequestFirstLine requestFirstLine = new HttpRequestFirstLine(firstLine);
            ProxyAgent agent = router.route(requestFirstLine);
            if (agent == null) {
                conSock.close();
                return;
            }
            agent.writeFirstLine(requestFirstLine);

            RequestConnection request = new RequestConnection(is, agent.getSocketToServer().getOutputStream());
            ResponseConnection response = new ResponseConnection(agent.getSocketToServer().getInputStream(), os);

            Thread requestThread = new Thread(request);
            requestThread.start();//

            Thread responseThread = new Thread(response);
            responseThread.start();

            responseThread.join();

            conSock.close();
            agent.getSocketToServer().close();
            Cmlb.logger.info("close sockets");
        } catch (BadRequest badRequest) {
        } catch (IOException e) {
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected String readLine() throws IOException {
        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        try {
            byte[] data = new byte[1];
            int length = 0;
            do {
                length = is.read(data);
                if (length > 0) {
                    bao.write(data);
                    if (data[0] == 10) {//10 is LF
                        break;
                    } else {
                    }
                }
            } while (length > 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(bao.toByteArray());
    }


}
