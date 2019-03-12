package jp.co.camnet.cmlb;

import jp.co.camnet.cmlb.config.CmlbConfig;
import jp.co.camnet.cmlb.config.ProxyConfig;
import jp.co.camnet.cmlb.http.HttpRequestFirstLine;
import jp.co.camnet.cmlb.proxy.ProxyAgent;

public class CmlbRouter {

    protected final CmlbConfig config;

    public CmlbRouter(CmlbConfig config) {
        this.config = config;
    }


    public ProxyAgent route(HttpRequestFirstLine requestFirstLine) {
        ProxyAgent agent = route0(requestFirstLine);
        return agent;
    }

    private ProxyAgent route0(HttpRequestFirstLine requestFirstLine) {
        String uri = requestFirstLine.getUri();
        for (ProxyConfig proxyConfig : config.proxyConfigList) {
            if (uri.matches(proxyConfig.getPattern())) {
                return proxyConfig.getProxyAgent();
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String uri = "/aaa/bbb/ccc";
        String pattern = "/aaa/.*";
        boolean result = uri.matches(pattern);
        //boolean result = pattern.matches(uri);
        System.out.println(result);
    }

}
