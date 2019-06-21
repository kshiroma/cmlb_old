package jp.co.camnet.cmlb.config;

import jp.co.camnet.cmlb.proxy.ProxyAgent;

import java.util.function.Supplier;

public class ProxyConfig {


    private String pattern;
    private Supplier<ProxyAgent> supplier;

    public ProxyConfig(String pattern, Supplier<ProxyAgent> supplier) {
        this.pattern = pattern;
        this.supplier = supplier;
    }

    public String getPattern() {
        return pattern;
    }

    public ProxyAgent getProxyAgent() {
        return supplier.get();
    }


}
