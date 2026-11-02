use rand::{seq::SliceRandom, RngCore};
struct Words {
    word: u32,
    draws32: usize,
    draws64: usize,
    fail_at: Option<usize>,
}
impl Words {
    fn next(&self) {
        if self.fail_at == Some(self.draws32 + self.draws64) {
            panic!("rng failure");
        }
    }
}
impl RngCore for Words {
    fn next_u32(&mut self) -> u32 {
        self.draws32 += 1;
        self.next();
        self.word
    }
    fn next_u64(&mut self) -> u64 {
        self.draws64 += 1;
        self.next();
        self.word.into()
    }
    fn fill_bytes(&mut self, _: &mut [u8]) {
        panic!("unexpected byte draw")
    }
}
#[derive(Debug, PartialEq)]
struct Snapshot {
    items: Vec<usize>,
    selected: Option<Vec<usize>>,
    remainder: Option<Vec<usize>>,
    draws32: usize,
    draws64: usize,
    panicked: bool,
}
fn observe(len: usize, amount: usize, word: u32, fail_at: Option<usize>) -> Snapshot {
    let mut rng = Words {
        word,
        draws32: 0,
        draws64: 0,
        fail_at,
    };
    let mut items: Vec<_> = (0..len).collect();
    let outcome = std::panic::catch_unwind(std::panic::AssertUnwindSafe(|| {
        let (a, b) = items.partial_shuffle(&mut rng, amount);
        (a.to_vec(), b.to_vec())
    }));
    let (selected, remainder, panicked) = match outcome {
        Ok((a, b)) => (Some(a), Some(b), false),
        Err(payload) => {
            assert_eq!(payload.downcast_ref::<&str>(), Some(&"rng failure"));
            (None, None, true)
        }
    };
    Snapshot {
        items,
        selected,
        remainder,
        draws32: rng.draws32,
        draws64: rng.draws64,
        panicked,
    }
}
fn main() {
    let cases = [
        ("empty", 0, 0, 0, None),
        ("singletonNoop", 1, 0, u32::MAX, None),
        ("singletonSelected", 1, 1, u32::MAX, None),
        ("zeroAmount", 5, 0, 0x9e3779b9, None),
        ("oneSelected", 5, 1, 0, None),
        ("partialZero", 5, 3, 0, None),
        ("partialMax", 5, 3, u32::MAX, None),
        ("highStart", 25, 3, 0x9e3779b9, None),
        ("crossesChunks", 25, 13, 0x9e3779b9, None),
        ("oversize", 5, 8, 0, None),
        ("largestAmount", 5, 2147483647, u32::MAX, None),
        ("firstDrawFailure", 5, 3, 0, Some(1)),
        ("refillFailure", 25, 24, 0, Some(2)),
    ];
    for (name, len, amount, word, fail_at) in cases {
        let s = observe(len, amount, word, fail_at);
        let selected = s
            .selected
            .as_ref()
            .map(|x| format!("{:?}", x))
            .unwrap_or("null".into());
        let remainder = s
            .remainder
            .as_ref()
            .map(|x| format!("{:?}", x))
            .unwrap_or("null".into());
        let fail = fail_at.map(|x| x.to_string()).unwrap_or("null".into());
        println!("{{\"name\":\"{}\",\"len\":{},\"amount\":{},\"word\":{},\"failAt\":{},\"items\":{:?},\"selected\":{},\"remainder\":{},\"draws32\":{},\"draws64\":{},\"panicked\":{}}}",name,len,amount,word,fail,s.items,selected,remainder,s.draws32,s.draws64,s.panicked);
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    fn check(len: usize, amount: usize, word: u32, fail_at: Option<usize>, expected: Snapshot) {
        assert_eq!(observe(len, amount, word, fail_at), expected);
    }
    #[test]
    fn empty() {
        check(
            0,
            0,
            0u32,
            None,
            Snapshot {
                items: vec![],
                selected: Some(vec![]),
                remainder: Some(vec![]),
                draws32: 0,
                draws64: 0,
                panicked: false,
            },
        );
    }
    #[test]
    fn singleton_noop() {
        check(
            1,
            0,
            4294967295u32,
            None,
            Snapshot {
                items: vec![0],
                selected: Some(vec![]),
                remainder: Some(vec![0]),
                draws32: 0,
                draws64: 0,
                panicked: false,
            },
        );
    }
    #[test]
    fn singleton_selected() {
        check(
            1,
            1,
            4294967295u32,
            None,
            Snapshot {
                items: vec![0],
                selected: Some(vec![0]),
                remainder: Some(vec![]),
                draws32: 0,
                draws64: 0,
                panicked: false,
            },
        );
    }
    #[test]
    fn zero_amount() {
        check(
            5,
            0,
            2654435769u32,
            None,
            Snapshot {
                items: vec![0, 1, 2, 3, 4],
                selected: Some(vec![]),
                remainder: Some(vec![0, 1, 2, 3, 4]),
                draws32: 0,
                draws64: 0,
                panicked: false,
            },
        );
    }
    #[test]
    fn one_selected() {
        check(
            5,
            1,
            0u32,
            None,
            Snapshot {
                items: vec![4, 1, 2, 3, 0],
                selected: Some(vec![0]),
                remainder: Some(vec![4, 1, 2, 3]),
                draws32: 1,
                draws64: 0,
                panicked: false,
            },
        );
    }
    #[test]
    fn partial_zero() {
        check(
            5,
            3,
            0u32,
            None,
            Snapshot {
                items: vec![4, 1, 0, 2, 3],
                selected: Some(vec![0, 2, 3]),
                remainder: Some(vec![4, 1]),
                draws32: 1,
                draws64: 0,
                panicked: false,
            },
        );
    }
    #[test]
    fn partial_max() {
        check(
            5,
            3,
            4294967295u32,
            None,
            Snapshot {
                items: vec![0, 1, 2, 3, 4],
                selected: Some(vec![2, 3, 4]),
                remainder: Some(vec![0, 1]),
                draws32: 1,
                draws64: 0,
                panicked: false,
            },
        );
    }
    #[test]
    fn high_start() {
        check(
            25,
            3,
            2654435769u32,
            None,
            Snapshot {
                items: vec![
                    0, 24, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 22, 15, 16, 17, 18, 19, 20, 23,
                    14, 21, 1,
                ],
                selected: Some(vec![14, 21, 1]),
                remainder: Some(vec![
                    0, 24, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 22, 15, 16, 17, 18, 19, 20, 23,
                ]),
                draws32: 1,
                draws64: 0,
                panicked: false,
            },
        );
    }
    #[test]
    fn crosses_chunks() {
        check(
            25,
            13,
            2654435769u32,
            None,
            Snapshot {
                items: vec![
                    0, 24, 2, 15, 4, 5, 16, 22, 12, 9, 10, 18, 13, 21, 14, 3, 6, 23, 11, 19, 1, 17,
                    7, 8, 20,
                ],
                selected: Some(vec![13, 21, 14, 3, 6, 23, 11, 19, 1, 17, 7, 8, 20]),
                remainder: Some(vec![0, 24, 2, 15, 4, 5, 16, 22, 12, 9, 10, 18]),
                draws32: 4,
                draws64: 0,
                panicked: false,
            },
        );
    }
    #[test]
    fn oversize() {
        check(
            5,
            8,
            0u32,
            None,
            Snapshot {
                items: vec![4, 0, 1, 2, 3],
                selected: Some(vec![4, 0, 1, 2, 3]),
                remainder: Some(vec![]),
                draws32: 1,
                draws64: 0,
                panicked: false,
            },
        );
    }
    #[test]
    fn largest_amount() {
        check(
            5,
            2147483647,
            4294967295u32,
            None,
            Snapshot {
                items: vec![0, 1, 2, 3, 4],
                selected: Some(vec![0, 1, 2, 3, 4]),
                remainder: Some(vec![]),
                draws32: 1,
                draws64: 0,
                panicked: false,
            },
        );
    }
    #[test]
    fn first_draw_failure() {
        check(
            5,
            3,
            0u32,
            Some(1),
            Snapshot {
                items: vec![0, 1, 2, 3, 4],
                selected: None,
                remainder: None,
                draws32: 1,
                draws64: 0,
                panicked: true,
            },
        );
    }
    #[test]
    fn refill_failure() {
        check(
            25,
            24,
            0u32,
            Some(2),
            Snapshot {
                items: vec![
                    11, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
                    22, 23, 24,
                ],
                selected: None,
                remainder: None,
                draws32: 2,
                draws64: 0,
                panicked: true,
            },
        );
    }
    #[test]
    fn raw_payload_snapshots() {
        let mut values = [0x80000000u32, 0x7fc01234, 0x7f800000, 0xff800000].map(f32::from_bits);
        let mut rng = Words {
            word: 0,
            draws32: 0,
            draws64: 0,
            fail_at: None,
        };
        let (a, b) = values.partial_shuffle(&mut rng, 2);
        assert_eq!(
            a.iter().map(|x| x.to_bits()).collect::<Vec<_>>(),
            vec![0x80000000, 0x7f800000]
        );
        assert_eq!(
            b.iter().map(|x| x.to_bits()).collect::<Vec<_>>(),
            vec![0xff800000, 0x7fc01234]
        );
        assert_eq!(
            values.map(f32::to_bits),
            [0xff800000, 0x7fc01234, 0x80000000, 0x7f800000]
        );
        assert_eq!((rng.draws32, rng.draws64), (1, 0));
        let mut values = [
            0x8000000000000000u64,
            0x7ff8000000001234,
            0x7ff0000000000000,
            0xfff0000000000000,
        ]
        .map(f64::from_bits);
        let mut rng = Words {
            word: 0,
            draws32: 0,
            draws64: 0,
            fail_at: None,
        };
        let (a, b) = values.partial_shuffle(&mut rng, 2);
        assert_eq!(
            a.iter().map(|x| x.to_bits()).collect::<Vec<_>>(),
            vec![0x8000000000000000, 0x7ff0000000000000]
        );
        assert_eq!(
            b.iter().map(|x| x.to_bits()).collect::<Vec<_>>(),
            vec![0xfff0000000000000, 0x7ff8000000001234]
        );
        assert_eq!(
            values.map(f64::to_bits),
            [
                0xfff0000000000000,
                0x7ff8000000001234,
                0x8000000000000000,
                0x7ff0000000000000
            ]
        );
        assert_eq!((rng.draws32, rng.draws64), (1, 0));
    }
}
