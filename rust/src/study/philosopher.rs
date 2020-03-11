use std::sync::Arc;
use std::sync::Mutex;
use std::thread;
use std::time::Duration;

struct Philosopher {
    name: String,
    left: usize,
    right: usize,
}

struct Table {
    forks: Vec<Mutex<()>>,
}


impl Philosopher {
    fn new(name: &str, left: usize, right: usize) -> Philosopher {
        Philosopher {
            name: name.to_string(),
            left: left,
            right: right,
        }
    }

    fn eat(&self, table: &Table) {
        //println!("{} begin.", self.name);
        let _left = table.forks[self.left].lock().unwrap();
        //thread::sleep(Duration::from_millis(1500));
        let _right = table.forks[self.right].lock().unwrap();

        println!("{} is eating.", self.name);
        thread::sleep(Duration::from_millis(100));
        println!("{} is done eating", self.name);
    }


    fn eat2(&self, table: Table) {
        let _left = table.forks[self.left].lock().unwrap();
        thread::sleep(Duration::from_millis(150));
        let _right = table.forks[self.right].lock().unwrap();

        println!("{} is eating.", self.name);
        thread::sleep(Duration::from_millis(10));
        println!("{} is done eating", self.name);
    }
}


#[test]
fn a() {
    let table = Arc::new(Table {
        forks: vec![
            Mutex::new(()),
            Mutex::new(()),
            Mutex::new(()),
            Mutex::new(()),
            Mutex::new(()),
        ]
    });

    let philosophers = vec![
        Philosopher::new("1.Judith", 0, 1),
        Philosopher::new("2.Giles", 1, 2),
        Philosopher::new("3.Karl", 2, 3),
        Philosopher::new("4.Emma", 3, 4),
        Philosopher::new("5.Michel", 0, 4),
    ];


    let handles: Vec<_> = philosophers.into_iter().map(|p| {
        let table = table.clone();
        thread::spawn(move || { p.eat(&table); })
    }).collect();

    for h in handles {
        h.join().unwrap();
    }
}