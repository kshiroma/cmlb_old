use std::io::prelude::*;

#[warn(dead_code)]
pub fn read_line(reader: &mut Read) -> String {
    let mut line: Vec<u8> = Vec::new();
    loop {
        let mut data = [0; 1];
        let mut prevIsCR = false;
        let _ = reader.read(&mut data).unwrap_or(2);
        if let _ = 1 {
            let a = data[0];
            if a == b'\n' {
                break;
            }
            if prevIsCR {
                line.push(b'\r');
            }
            prevIsCR = a == b'\r';
            if prevIsCR {} else {
                line.push(data[0]);
            }
        }
    }
    let string = String::from_utf8(line).unwrap();
    //println!("{}", string);
    string
}
