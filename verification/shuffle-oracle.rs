use rand::{seq::SliceRandom, RngCore};
struct Words {
    word: u32,
    draws: usize,
    fail: bool,
}
impl RngCore for Words {
    fn next_u32(&mut self) -> u32 {
        self.draws += 1;
        if self.fail {
            panic!("rng failure");
        }
        self.word
    }
    fn next_u64(&mut self) -> u64 {
        panic!("unexpected u64")
    }
    fn fill_bytes(&mut self, _: &mut [u8]) {
        panic!("unexpected byte draw")
    }
}
fn observe(len: usize, word: u32) -> (Vec<usize>, usize) {
    let mut rng = Words {
        word,
        draws: 0,
        fail: false,
    };
    let mut items: Vec<_> = (0..len).collect();
    items.shuffle(&mut rng);
    (items, rng.draws)
}
fn main() {
    for (len, word) in [
        (0, 0),
        (1, u32::MAX),
        (2, 0),
        (12, u32::MAX),
        (13, u32::MAX),
        (25, 0),
        (25, 0x9e3779b9),
        (25, 0x80000000),
    ] {
        let (values, draws) = observe(len, word);
        println!(
            "{{\"len\":{},\"word\":{},\"values\":{:?},\"draws\":{}}}",
            len, word, values, draws
        );
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    fn check(len: usize, word: u32, expected: &[usize], draws: usize) {
        let (values, actual_draws) = observe(len, word);
        assert_eq!(values, expected);
        assert_eq!(actual_draws, draws);
    }
    #[test]
    fn empty() {
        check(0, 0u32, &[], 0);
    }
    #[test]
    fn singleton() {
        check(1, 4294967295u32, &[0], 0);
    }
    #[test]
    fn two_zero() {
        check(2, 0u32, &[1, 0], 1);
    }
    #[test]
    fn factorial_chunk() {
        check(
            12,
            4294967295u32,
            &[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11],
            1,
        );
    }
    #[test]
    fn refill_boundary() {
        check(
            13,
            4294967295u32,
            &[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12],
            2,
        );
    }
    #[test]
    fn zero_across_chunks() {
        check(
            25,
            0u32,
            &[
                24, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21,
                22, 23,
            ],
            3,
        );
    }
    #[test]
    fn mixed_across_chunks() {
        check(
            25,
            2654435769u32,
            &[
                2, 24, 3, 15, 10, 9, 16, 22, 12, 0, 4, 18, 13, 21, 14, 5, 6, 23, 8, 19, 7, 17, 11,
                1, 20,
            ],
            5,
        );
    }
    #[test]
    fn midpoint_across_chunks() {
        check(
            25,
            2147483648u32,
            &[
                24, 0, 1, 2, 3, 4, 11, 6, 7, 18, 9, 5, 10, 12, 13, 14, 15, 8, 17, 16, 19, 20, 21,
                22, 23,
            ],
            3,
        );
    }
    #[test]
    fn float_payload_bits_are_preserved() {
        let mut floats = [0x80000000u32, 0x7fc01234, 0x7f800000, 0xff800000].map(f32::from_bits);
        let mut rng = Words {
            word: 0,
            draws: 0,
            fail: false,
        };
        floats.shuffle(&mut rng);
        assert_eq!(
            floats.map(f32::to_bits),
            [0xff800000, 0x80000000, 0x7fc01234, 0x7f800000]
        );
        assert_eq!(rng.draws, 1);
        let mut doubles = [
            0x8000000000000000u64,
            0x7ff8000000001234,
            0x7ff0000000000000,
            0xfff0000000000000,
        ]
        .map(f64::from_bits);
        let mut rng = Words {
            word: 0,
            draws: 0,
            fail: false,
        };
        doubles.shuffle(&mut rng);
        assert_eq!(
            doubles.map(f64::to_bits),
            [
                0xfff0000000000000,
                0x8000000000000000,
                0x7ff8000000001234,
                0x7ff0000000000000
            ]
        );
        assert_eq!(rng.draws, 1);
    }
    #[test]
    fn rng_failure_preserves_input_and_propagates() {
        let mut values = [0, 1, 2, 3];
        let mut rng = Words {
            word: 0,
            draws: 0,
            fail: true,
        };
        let result =
            std::panic::catch_unwind(std::panic::AssertUnwindSafe(|| values.shuffle(&mut rng)));
        assert_eq!(
            result.unwrap_err().downcast_ref::<&str>(),
            Some(&"rng failure")
        );
        assert_eq!(rng.draws, 1);
        assert_eq!(values, [0, 1, 2, 3]);
    }
}
