use std::fmt::Pointer;
use std::io::prelude::*;
use std::net::TcpStream;
use std::rc::Rc;

use crate::server::http_response::HttpResponseInfo;

pub struct Downstream {
    response: HttpResponseInfo,
    //writer: Rc<Write>,
}

impl Downstream {
    pub fn new(response: HttpResponseInfo) -> Self {
        let downstream = Downstream {
            response
        };
        return downstream;
    }

    pub fn sendFirstLine(&self, writer: &mut Write) {
        writer.write(self.response.http_first_line.protocol_version.as_bytes());
        writer.write(b" ");
        writer.write(self.response.http_first_line.http_status_code.to_string().as_bytes());
        writer.write(b" ");
        writer.write(self.response.http_first_line.http_status.as_bytes());
        writer.write(b"\r\n");
    }

    pub fn sendHeaders(&self, writer: &mut Write) {
        if self.response.http_response_header.keep_alive {}
        if self.response.http_response_header.content_length > 0 {
            writer.write(b"Content-Length: ");
            writer.write(self.response.http_response_header.content_length.to_string().as_bytes());
            writer.write(b"\r\n");
        }
        let response = &self.response;
        for header in &response.http_response_header.headers {
            let name = &header.name;
            let value = &header.value;
            writer.write(name.as_bytes());
            writer.write(b": ");
            writer.write(value.as_bytes());
            writer.write(b"\r\n");
        }
        writer.write(b"\r\n");
        println!("end send response header.")
    }

    pub fn sendBody(&self, reader: &mut Read, writer: &mut Write) {
        let data_length = self.response.http_response_header.content_length;
        let mut buf = [0; 1024];
        if data_length > 0 {
            let mut unsend_data_length = self.response.http_response_header.content_length;
            while unsend_data_length > 0 {
                let size = reader.read(&mut buf).unwrap();
                let d = size.to_string();
                let data_length: i64 = d.parse().unwrap();
                writer.write(&buf[0..size]);
                log::trace!("response {} data",d);
                unsend_data_length = unsend_data_length - data_length;
            }
        } else if data_length == 0 {
            //何もしない
            log::trace!("response nothing");
        } else {
            let mut send_data_length = 0;
            loop {
                let size = reader.read(&mut buf).unwrap();
                if size == 0 {
                    break;
                }
                let d = size.to_string();
                let data_length: i64 = d.parse().unwrap();
                writer.write(&buf[0..size]);
                log::trace!("response {} data",d);
                send_data_length = send_data_length + data_length;
                if (send_data_length > 512) {
                    writer.flush();
                    send_data_length = 0;
                }
            }
        }
    }
}

