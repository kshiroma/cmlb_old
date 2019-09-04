extern crate rand;
extern crate regex;

use crate::routing_sample::createSampleConfig;

pub mod http;
pub mod server;
//pub mod study;
pub mod io;
pub mod routing_sample;


fn main() {
    let config = createSampleConfig();
    server::listen(config, 80);
}
