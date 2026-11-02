use rand::{seq::IndexedRandom, RngCore};
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
fn check<T: Copy + PartialEq + core::fmt::Debug>(items: &[T], words: &[u32], expected: Option<T>) {
    let mut rng = Words::new(words);
    assert_eq!(items.choose(&mut rng).copied(), expected);
    assert_eq!(rng.draws, words.len());
    assert!(rng.words.is_empty());
}
fn adapters(size: usize, words: &[u32], expected: Option<usize>) {
    check(
        &(0..size).map(|v| v as i32).collect::<Vec<_>>(),
        words,
        expected.map(|v| v as i32),
    );
    check(
        &(0..size).map(|v| v as i64).collect::<Vec<_>>(),
        words,
        expected.map(|v| v as i64),
    );
    check(
        &(0..size).map(|v| v as i16).collect::<Vec<_>>(),
        words,
        expected.map(|v| v as i16),
    );
    check(
        &(0..size).map(|v| v as i8).collect::<Vec<_>>(),
        words,
        expected.map(|v| v as i8),
    );
    check(
        &(0..size).map(|v| v as f32).collect::<Vec<_>>(),
        words,
        expected.map(|v| v as f32),
    );
    check(
        &(0..size).map(|v| v as f64).collect::<Vec<_>>(),
        words,
        expected.map(|v| v as f64),
    );
}
#[test]
fn empty_does_not_draw() {
    adapters(0, &[], None);
}
#[test]
fn singleton_draws_u32() {
    adapters(1, &[u32::MAX], Some(0));
}
#[test]
fn high_bits_select_the_index() {
    adapters(4, &[0x80000000], Some(2));
}
#[test]
fn correction_without_overflow() {
    adapters(52, &[0x3b13b13b, 0], Some(11));
}
#[test]
fn correction_with_overflow() {
    adapters(52, &[0x3b13b13b, u32::MAX], Some(12));
}
#[test]
fn maximal_word() {
    adapters(3, &[u32::MAX], Some(2));
}
#[test]
fn selected_none_is_present() {
    let items = [Some(0), None, Some(2)];
    let mut rng = Words::new(&[0x80000000]);
    assert_eq!(items.choose(&mut rng), Some(&None));
    assert_eq!(rng.draws, 1);
    assert!(rng.words.is_empty());
}
