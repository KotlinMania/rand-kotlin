use rand::{
    distr::{
        weighted::{Error, WeightedIndex},
        Distribution,
    },
    RngCore,
};
#[test]
fn finite_overflow_panics() {
    assert!(std::panic::catch_unwind(|| WeightedIndex::new([f64::MAX, f64::MAX])).is_err());
}
#[test]
fn positive_infinity_panics() {
    assert!(std::panic::catch_unwind(|| WeightedIndex::new([1.0, f64::INFINITY])).is_err());
}
#[test]
fn invalid_after_infinity_is_invalid_weight() {
    assert!(matches!(
        WeightedIndex::new([1.0, f64::INFINITY, -1.0]),
        Err(Error::InvalidWeight)
    ));
}
#[test]
fn invalid_after_overflow_is_invalid_weight() {
    assert!(matches!(
        WeightedIndex::new([f64::MAX, f64::MAX, f64::NAN]),
        Err(Error::InvalidWeight)
    ));
}
#[test]
fn negative_infinity_is_invalid_weight() {
    assert!(matches!(
        WeightedIndex::new([1.0, f64::NEG_INFINITY]),
        Err(Error::InvalidWeight)
    ));
}
#[test]
fn zero_and_empty_errors_are_preserved() {
    assert!(matches!(
        WeightedIndex::new([0.0, 0.0]),
        Err(Error::InsufficientNonZero)
    ));
    assert!(matches!(
        WeightedIndex::<f64>::new(core::iter::empty::<f64>()),
        Err(Error::InvalidInput)
    ));
}
struct Word {
    draws: usize,
}
impl RngCore for Word {
    fn next_u32(&mut self) -> u32 {
        panic!("unexpected u32 draw")
    }
    fn next_u64(&mut self) -> u64 {
        self.draws += 1;
        u64::MAX
    }
    fn fill_bytes(&mut self, _: &mut [u8]) {
        panic!("unexpected byte draw")
    }
}
#[test]
fn finite_large_weights_sample() {
    let dist = WeightedIndex::new([f64::MAX / 4.0, f64::MAX / 4.0]).unwrap();
    assert_eq!(dist.total_weight(), f64::MAX / 2.0);
    let mut rng = Word { draws: 0 };
    assert_eq!(dist.sample(&mut rng), 1);
    assert_eq!(rng.draws, 1);
}
