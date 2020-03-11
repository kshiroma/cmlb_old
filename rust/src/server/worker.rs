use std::io::prelude::*;
use std::net::TcpStream;
use std::sync::Arc;

use chrono::Local;

use crate::http::http_status::{HttpStatus, not_found};
use crate::server::config::{RelayConnectionInfo, ServerConfig};
use crate::server::downstream::Downstream;
use crate::server::http_request::read_http_request;
use crate::server::upstream::Upstream;

pub struct Worker {
    config: Arc<ServerConfig>,
}

impl Worker {
    pub fn new(config: Arc<ServerConfig>) -> Self {
        Worker {
            config,
        }
    }

    pub fn handle(&self, mut _stream: TcpStream) -> std::io::Result<()> {
        let b = Box::new(_stream);
        let mut reader = b.try_clone().unwrap();
        let mut writer = b.try_clone().unwrap();
        self.handle_read_writer(&mut reader, &mut writer);
        //終わり
        writer.flush().unwrap();
        return Ok(());
    }

    fn handle_read_writer(&self, reader: &mut Read, writer: &mut Write) -> std::io::Result<()> {
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
        let bRelay = std::rc::Rc::new(relay).clone();
        let bRequest = std::rc::Rc::new(request).clone();
        let mut upstream = Upstream::new(bRelay, bRequest).unwrap();

        upstream.sendFirstLine();
        upstream.sendHeader();
        upstream.sendBody(reader);
        upstream.flush();
        let response_info = upstream.read_http_response_info().unwrap();
        let downstream = Downstream::new(response_info);
        downstream.sendFirstLine(writer);
        downstream.sendHeaders(writer);
        downstream.sendBody(&mut upstream.stream, writer);
        writer.flush();
        return Ok(());
    }
}


