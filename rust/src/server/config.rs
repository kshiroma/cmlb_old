use std::borrow::Borrow;
use std::io::Read;
use std::net::TcpStream;
use std::ops::Deref;
use std::panic::resume_unwind;

use regex::Regex;

use crate::server::http_request::{HttpRequestFirstLine, HttpRequestHeader, HttpRequestInfo, read_http_request};

pub struct RoutingRule {
    name: String,
    routing_rule: fn(&HttpRequestInfo) -> Option<RelayConnectionInfo>,
    //HttpRequestInfoを受け取って、URLを返す
}

pub struct RelayConnectionInfo {
    pub host: String,
    pub port: i32,
    pub path: String,
}

impl RelayConnectionInfo {
    pub fn get_address(&self) -> String {
        let mut host = (&self.host).to_string();
        let port = &self.port;
        let port = *(port);
        if port > 1 && port != 80 {
            host.push(':');
            host = host + &port.to_string();
        }
        return host;
    }

    pub fn connect_relay(&self) -> std::io::Result<TcpStream> {
        let host = self.get_address();
        return std::net::TcpStream::connect(host);
    }
}

impl RoutingRule {
    pub fn new(name: String, routing_rule: fn(&HttpRequestInfo) -> Option<RelayConnectionInfo>) -> Self {
        RoutingRule {
            name,
            routing_rule,
        }
    }

    pub fn route(&self, requet: &HttpRequestInfo) -> Option<RelayConnectionInfo> {
        let func: fn(&HttpRequestInfo) -> Option<RelayConnectionInfo> = self.routing_rule;
        return func(requet);
    }
}

pub struct ServerConfig {
    routingRules: Vec<RoutingRule>,
}

impl ServerConfig {
    pub fn new() -> Self {
        let vec: Vec<RoutingRule> = Vec::new();
        ServerConfig {
            routingRules: vec
        }
    }

    pub fn add(&mut self, rule: RoutingRule) {
        self.routingRules.push(rule);
    }

    fn findRoutingRule(&self, request: &HttpRequestInfo) -> Option<&RoutingRule> {
        for rule in self.routingRules.iter() {
            if let Some(r) = (rule.routing_rule)(request) {
                return Some(rule);
            }
        }
        return None;
    }

    pub fn route(&self, request: &HttpRequestInfo) -> Option<RelayConnectionInfo> {
        for rule in self.routingRules.iter() {
            println!("checking {}", rule.name);
            if let Some(r) = (rule.routing_rule)(request) {
                return Some(r);
            }
        }
        return None;
    }
}


#[test]
fn test() {
    let routeingRule1 = RoutingRule::new("routeingRule1".to_string(), routing1);
    let routeingRule2 = RoutingRule::new("routeingRule2".to_string(), routing2);
    let mut config = ServerConfig::new();
    config.add(routeingRule1);
    config.add(routeingRule2);

    use std::fs::File;
    let path = "test/httprequest/requets_post.txt";
    let mut file = File::open(path).unwrap();
    let request = read_http_request(&mut file).unwrap();
    //let b = Box::new(request);
    //let req = b.clone();
    let result = config.route(&request);
    if result.is_some() {
        println!("{}", result.unwrap().path);
    } else {
        println!("None");
    }
}

fn routing1(request: &HttpRequestInfo) -> Option<RelayConnectionInfo> {
    let re = Regex::new(r"^/cattleya/.*").unwrap();
    let relay = if re.is_match(&request.http_first_line.uri) {
        Some(RelayConnectionInfo {
            host: "localhost".to_string(),
            port: 8000,
            path: "/cattleya".to_string(),
        })
    } else {
        None
    };
    return relay;
}

fn routing2(request: &HttpRequestInfo) -> Option<RelayConnectionInfo> {
    let re = Regex::new(r"^/bbb/.*").unwrap();
    let relay = if re.is_match(&request.http_first_line.uri) {
        Some(RelayConnectionInfo {
            host: "localhost".to_string(),
            port: 8001,
            path: "/ccc/ddd".to_string(),
        })
    } else {
        None
    };
    return relay;
}
