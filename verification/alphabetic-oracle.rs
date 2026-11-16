use rand::{
    distr::{Alphabetic, Distribution, SampleString},
    RngCore,
};
struct Words {
    words: std::collections::VecDeque<u32>,
    draws: usize,
}
impl Words {
    fn new(words: &[u32]) -> Self {
        Self {
            words: words.iter().copied().collect(),
            draws: 0,
        }
    }
}
impl RngCore for Words {
    fn next_u32(&mut self) -> u32 {
        self.draws += 1;
        self.words.pop_front().expect("extra draw")
    }
    fn next_u64(&mut self) -> u64 {
        panic!("unexpected u64 draw")
    }
    fn fill_bytes(&mut self, _: &mut [u8]) {
        panic!("unexpected byte draw")
    }
}
fn check(words: &[u32], expected: &[u8]) {
    let mut rng = Words::new(words);
    let got: Vec<u8> = expected
        .iter()
        .map(|_| Alphabetic.sample(&mut rng))
        .collect();
    assert_eq!(got, expected);
    assert_eq!(rng.draws, words.len());
    assert!(rng.words.is_empty());
}
#[test]
fn endpoints() {
    check(&[0, u32::MAX], b"Az");
}
#[test]
fn midpoints() {
    check(&[0x40000000, 0x80000000, 0xc0000000], b"Nan");
}
#[test]
fn correction_without_overflow() {
    check(&[0x3b13b13b, 0], b"L");
}
#[test]
fn correction_with_overflow() {
    check(&[0x3b13b13b, u32::MAX], b"M");
}
#[test]
fn append_keeps_prefix() {
    let mut rng = Words::new(&[0, 0x80000000, u32::MAX]);
    let mut s = String::from("π:");
    Alphabetic.append_string(&mut rng, &mut s, 3);
    assert_eq!(s, "π:Aaz");
    assert_eq!(rng.draws, 3);
    assert!(rng.words.is_empty());
}
#[test]
fn sampled_string_preserves_correction_draws() {
    let mut rng = Words::new(&[0, 0x3b13b13b, u32::MAX, u32::MAX]);
    assert_eq!(Alphabetic.sample_string(&mut rng, 3), "AMz");
    assert_eq!(rng.draws, 4);
    assert!(rng.words.is_empty());
}
#[test]
fn zero_length_does_not_draw() {
    let mut rng = Words::new(&[]);
    let mut s = String::from("π:");
    Alphabetic.append_string(&mut rng, &mut s, 0);
    assert_eq!(s, "π:");
    assert_eq!(Alphabetic.sample_string(&mut rng, 0), "");
    assert_eq!(rng.draws, 0);
}
