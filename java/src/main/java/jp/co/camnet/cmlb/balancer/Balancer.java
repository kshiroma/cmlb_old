package jp.co.camnet.cmlb.balancer;

import jp.co.camnet.cmlb.http.HttpRequestFirstLine;
import jp.co.camnet.cmlb.proxy.ProxyAgent;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Balancer implements ProxyAgent {
    public List<BalancerMember> members;
    private final BalancerMember instance;

    public Balancer(BalancerMember... members) {
        this.members = new ArrayList<>();
        for (BalancerMember m : members) {
            this.members.add(m);
        }
        int randomIndex = Math.abs(new Random().nextInt()) % (this.members.size() );
        instance = this.members.get(randomIndex);
    }

    protected BalancerMember get() {
        return instance;
    }


    @Override
    public byte[] read() {
        return get().read();
    }

    @Override
    public void close() {
        get().close();
    }

    @Override
    public Socket getSocketToServer() {
        return get().getSocketToServer();
    }

    @Override
    public void writeFirstLine(HttpRequestFirstLine firstLine) {
        get().writeFirstLine(firstLine);
    }


}
