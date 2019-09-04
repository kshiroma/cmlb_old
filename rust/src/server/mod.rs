use std::net::TcpListener;

use crate::routing_sample::createSampleConfig;
use crate::server::config::ServerConfig;

pub mod config;
pub mod http_request;
pub mod http_response;
mod worker;
mod upstream;
mod downstream;


pub fn listen(config: ServerConfig, port: i32) -> std::io::Result<()> {
    let rc = std::sync::Arc::new(config);
    let listener = std::net::TcpListener::bind(format!("127.0.0.1:{}", port))?;
    for stream in listener.incoming() {
        let rc0 = rc.clone();
        let stream = match stream {
            Ok(stream) => stream,
            Err(e) => {
                println!("An error occurred while accepting a connection:{}", e);
                continue;
            }
        };
        let _ = std::thread::spawn(|| -> std::io::Result<()> {
            let worker = worker::Worker::new(rc0);
            worker.handle(stream)
        });
    }
    Ok(())
}

#[test]
fn test() {
    let config = createSampleConfig();
    listen(config, 80);
    println!("end");
}