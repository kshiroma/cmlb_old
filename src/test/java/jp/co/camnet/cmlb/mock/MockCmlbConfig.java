package jp.co.camnet.cmlb.mock;

import jp.co.camnet.cmlb.config.CmlbConfig;

import java.util.ArrayList;

public class MockCmlbConfig extends CmlbConfig {

    //private List<ProxyConfig> proxyConfigList;

    public MockCmlbConfig() {
        proxyConfigList = new ArrayList<>();

//        ProxyConfig pc1 = new ProxyConfig("/", () -> new Balancer(
//                new BalancerMember("localhost", 8000),
//                new BalancerMember("localhost", 8000)
//        ));


//        proxyConfigList.add(pc1);
    }


}
