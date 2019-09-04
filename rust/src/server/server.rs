use std::io::{Read, Write};
use std::io;
use std::net::TcpListener;
use std::net::TcpStream;
use std::rc::Rc;
use std::sync::Arc;
use std::thread;

use chrono::Local;
use regex::Regex;

use crate::http::http_status::HttpStatus;
use crate::io::*;
use crate::routing_sample::*;
use crate::server::config::{RelayConnectionInfo, RoutingRule, ServerConfig};
use crate::server::downstream::Downstream;
use crate::server::http_request::{HttpRequestHeader, HttpRequestInfo, read_http_request};
use crate::server::http_response::read_http_response_info;
use crate::server::upstream::Upstream;

struct Worker {
    config: Arc<ServerConfig>
}

impl Worker {
    fn new(config: Arc<ServerConfig>) -> Self {
        Worker {
            config
        }
    }

    fn handle(&self, mut _stream: TcpStream) -> io::Result<()> {
        let b = Box::new(_stream);
        let mut reader = b.try_clone().unwrap();
        let mut writer = b.try_clone().unwrap();
        self.handle_read_writer(&mut reader, &mut writer);
        //終わり
        writer.flush().unwrap();
        return Ok(());
    }

    fn handle_read_writer(&self, reader: &mut Read, writer: &mut Write) -> io::Result<()> {
        let request = read_http_request(reader);
        if request.is_err() {}
        let request = request.unwrap();
        let relay: Option<RelayConnectionInfo> = self.config.route(&request);

        if relay.is_none() {
            println!("not found relay connection {}", request.http_first_line.uri);
            not_found(writer);
            return Ok(());
        }
        let relay = relay.unwrap();

        println!("relay connection host is {}:{}", relay.host, relay.port);
        //
        let bRelay = Rc::new(relay);
        let bRequest = Rc::new(request);
        let mut upstream = Upstream::new(bRelay.clone(), bRequest.clone()).unwrap();

        upstream.sendFirstLine();
        upstream.sendHeader();
        upstream.sendBody(reader);
        upstream.flush();
        //ここまでsend
        //ここからread
        let response_info = upstream.read_http_response_info().unwrap();
        let downstream = Downstream::new(response_info);
        downstream.sendFirstLine(writer);
        downstream.sendHeaders(writer);
        downstream.sendBody(&mut upstream.stream, writer);
        writer.flush();
        return Ok(());
    }
}

pub fn listen(config: ServerConfig, port: i32) -> io::Result<()> {
    let rc = Arc::new(config);
    let lis: TcpListener = TcpListener::bind(format!("127.0.0.1:{}", port))?;
    for stream in lis.incoming() {
        let rc0 = rc.clone();
        let stream = match stream {
            Ok(stream) => stream,
            Err(e) => {
                println!("An error occurred while accepting a connection:{}", e);
                continue;
            }
        };
        let _ = thread::spawn(|| -> io::Result<()> {
            let worker = Worker::new(rc0);
            worker.handle(stream)
        });
    }
    Ok(())
}

fn createConfig() -> ServerConfig {
    let routeingRule1 = RoutingRule::new("routing1".to_string(), routing1);
    let routeingRule2 = RoutingRule::new("routing2".to_string(), routing2);
    let mut config = ServerConfig::new();
    config.add(routeingRule1);
    config.add(routeingRule2);

    return config;
}

#[test]
fn test() {
    let config = createConfig();
    listen(config, 80);
    println!("end");
}

#[warn(dead_code)]
fn complete(writer: &mut Write) -> io::Result<()> {
    write!(writer, "HTTP/1.1 200 OK\r\n")?;
    write!(writer, "Date: {} \r\n", Local::now())?;
    write!(writer, "Content-Length: 11")?;
//write!(stream, "Transfer-Encoding: chunked\r\n")?;
    write!(writer, "\r\n")?;
    write!(writer, "abcedefgs\r\n")?;
    return Ok(());
}

fn not_found(writer: &mut Write) -> io::Result<()> {
    let status = HttpStatus::NotFound;
    let code = status.get().unwrap();
    let string = status.get_as_string().unwrap();
    write!(writer, "HTTP/1.1 {} {}\r\n", code, string)?;
    write!(writer, "Date: {} \r\n", Local::now())?;
    let buf = b"<html><body><h1>Not Found</h1></body></html>";
    let length = buf.len();
    write!(writer, "Content-Length: {}", length)?;
    write!(writer, "\r\n")?;
    write!(writer, "\r\n")?;
    writer.write(buf);
    write!(writer, "\r\n");
    return Ok(());
}


#[test]
fn test_readFirstLine() -> io::Result<()> {
    use std::fs;
    use std::fs::File;
//use std::io::Read;
    let path = "test/httprequest/requets_get.txt";
    let _string = fs::read_to_string(path).unwrap();

    let mut file = File::open(path).unwrap();
    let firstLine = read_line(&mut file);
    assert_eq!(firstLine, "GET /favicon.ico HTTP/1.1");
    return Ok(());
}

#[test]
fn test_handle() -> io::Result<()> {
    use std::fs::File;
    let path = "test/httprequest/requets_post.txt";
    let mut file = File::open(path).unwrap();
    let stdout = std::io::stdout();
    let mut stdout = stdout.lock();
    std::io::copy(&mut file, &mut stdout);
    Ok(())
}

