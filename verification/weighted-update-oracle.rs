use rand::{
    distr::{
        weighted::{Error, WeightedIndex},
        Distribution,
    },
    RngCore,
};
use std::panic::{catch_unwind, AssertUnwindSafe};
struct Word {
    draws: usize,
}
impl RngCore for Word {
    fn next_u32(&mut self) -> u32 {
        panic!("unexpected u32")
    }
    fn next_u64(&mut self) -> u64 {
        self.draws += 1;
        u64::MAX
    }
    fn fill_bytes(&mut self, _: &mut [u8]) {
        panic!("unexpected byte draw")
    }
}
fn unchanged(dist: &WeightedIndex<f64>) {
    assert_eq!(dist.weights().collect::<Vec<_>>(), vec![1.0, 2.0]);
    assert_eq!(dist.total_weight(), 3.0);
    let mut rng = Word { draws: 0 };
    assert_eq!(dist.sample(&mut rng), 1);
    assert_eq!(rng.draws, 1);
}
fn rejected(updates: &[(usize, &f64)], expected: Error) {
    let mut dist = WeightedIndex::new([1.0, 2.0]).unwrap();
    assert_eq!(dist.update_weights(updates), Err(expected));
    unchanged(&dist);
}
#[test]
fn empty_update_returns_ok() {
    let mut dist = WeightedIndex::new([1.0, 2.0]).unwrap();
    assert_eq!(dist.update_weights(&[]), Ok(()));
    unchanged(&dist);
}
#[test]
fn successful_update_returns_ok() {
    let mut dist = WeightedIndex::new([1.0, 2.0]).unwrap();
    assert_eq!(dist.update_weights(&[(0, &3.0)]), Ok(()));
    assert_eq!(dist.weights().collect::<Vec<_>>(), vec![3.0, 2.0]);
    assert_eq!(dist.total_weight(), 5.0);
    let mut rng = Word { draws: 0 };
    assert_eq!(dist.sample(&mut rng), 1);
    assert_eq!(rng.draws, 1);
}
#[test]
fn negative_weight_returns_error() {
    rejected(&[(0, &-1.0)], Error::InvalidWeight);
}
#[test]
fn invalid_weight_precedes_out_of_bounds() {
    rejected(&[(8, &-1.0)], Error::InvalidWeight);
}
#[test]
fn duplicate_index_precedes_invalid_weight() {
    rejected(&[(0, &3.0), (0, &-1.0)], Error::InvalidInput);
}
#[test]
fn out_of_bounds_returns_error() {
    rejected(&[(8, &1.0)], Error::InvalidInput);
}
#[test]
fn zero_total_returns_error() {
    rejected(&[(0, &0.0), (1, &0.0)], Error::InsufficientNonZero);
}
#[test]
fn nonfinite_sampler_failure_still_panics() {
    let mut dist = WeightedIndex::new([1.0, 2.0]).unwrap();
    assert!(catch_unwind(AssertUnwindSafe(
        || dist.update_weights(&[(0, &f64::INFINITY)])
    ))
    .is_err());
}
#[test]
fn nan_total_after_prior_panic_is_not_insufficient_nonzero() {
    let mut dist = WeightedIndex::new([1.0, 2.0]).unwrap();
    assert!(catch_unwind(AssertUnwindSafe(
        || dist.update_weights(&[(0, &f64::INFINITY)])
    ))
    .is_err());
    assert!(catch_unwind(AssertUnwindSafe(|| dist.update_weights(&[(0, &1.0)]))).is_err());
    assert!(dist.total_weight().is_nan());
}
