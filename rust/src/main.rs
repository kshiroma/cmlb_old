//use std::io::{Read, Write};
//use std::io;
//use std::net::TcpListener;
//use std::net::TcpStream;
//use std::thread;

extern crate rand;
extern crate regex;

pub mod http;
pub mod server;
pub mod study;
pub mod io;
pub mod routing_sample;


fn main() {
    //server::server::server(8082);
//    thread::spawn(|| { server::server(8081) }).join();
//    thread::spawn(server::server(8082)).join();
    //let _ = thread::spawn(server::server(8082));
}
