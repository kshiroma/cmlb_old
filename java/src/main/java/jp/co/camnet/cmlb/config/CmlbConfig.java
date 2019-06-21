package jp.co.camnet.cmlb.config;

import jp.co.camnet.cmlb.balancer.Balancer;
import jp.co.camnet.cmlb.balancer.BalancerMember;

import java.util.ArrayList;
import java.util.List;

public class CmlbConfig {
    public int serverPort = 80;
    public List<ProxyConfig> proxyConfigList;

    public CmlbConfig() {
        proxyConfigList = new ArrayList<>();

        ProxyConfig config1 = new ProxyConfig("^/cattleya/.*", () -> new Balancer(
                new BalancerMember("http://localhost:8000/cattleya")
                //, new BalancerMember("http://localhost:8083/ccc/bbb")
        ));
        ProxyConfig config2 = new ProxyConfig("^/bbb/.*", () -> new Balancer(
                new BalancerMember("http://localhost:8081/ccc/ddd"),
                new BalancerMember("http://localhost:8082/ccc/ddd")
        ));
        ProxyConfig config3 = new ProxyConfig("^/yahoo/.*", () -> new Balancer(
                new BalancerMember("http://www.yahoo.co.jp/")
                //new BalancerMember("http://localhost:8083/ccc/bbb")
        ));
        proxyConfigList.add(config1);
        proxyConfigList.add(config2);
        proxyConfigList.add(config3);
    }
}
