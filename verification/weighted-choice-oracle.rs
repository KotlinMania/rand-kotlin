use rand::{distr::weighted::Error, seq::IndexedRandom, RngCore};
use std::{
    cell::RefCell,
    panic::{catch_unwind, AssertUnwindSafe},
};
struct Words {
    words: std::collections::VecDeque<u64>,
    draws: usize,
    fail: bool,
}
impl Words {
    fn new(words: &[u64]) -> Self {
        Self {
            words: words.iter().copied().collect(),
            draws: 0,
            fail: false,
        }
    }
}
impl RngCore for Words {
    fn next_u32(&mut self) -> u32 {
        panic!("unexpected u32")
    }
    fn next_u64(&mut self) -> u64 {
        self.draws += 1;
        if self.fail {
            panic!("rng failure");
        }
        self.words.pop_front().expect("extra draw")
    }
    fn fill_bytes(&mut self, _: &mut [u8]) {
        panic!("unexpected byte draw")
    }
}
fn check(
    weights: &[f64],
    words: &[u64],
    expected: Result<usize, Error>,
    expected_reads: &[usize],
    poison: Option<usize>,
) {
    let items: Vec<_> = (0..weights.len()).collect();
    let reads = RefCell::new(vec![]);
    let mut rng = Words::new(words);
    let result = items.choose_weighted(&mut rng, |&index| {
        reads.borrow_mut().push(index);
        if Some(index) == poison {
            panic!("visited suffix");
        }
        weights[index]
    });
    assert_eq!(result.map(|&index| index), expected);
    assert_eq!(*reads.borrow(), expected_reads);
    assert_eq!(rng.draws, words.len());
    assert!(rng.words.is_empty());
}
#[test]
fn valid_choices_use_canonical_bits() {
    check(&[1.0, 2.0], &[u64::MAX], Ok(1), &[0, 1], None);
    check(&[1.0, 2.0], &[0x5555555555555555], Ok(0), &[0, 1], None);
}
#[test]
fn leading_zero_weight_is_skipped() {
    check(&[0.0, 1.0], &[0], Ok(1), &[0, 1], None);
}
#[test]
fn empty_is_error() {
    check(&[], &[], Err(Error::InvalidInput), &[], None);
}
#[test]
fn invalid_prefix_stops_callback() {
    check(&[-1.0, 1.0], &[], Err(Error::InvalidWeight), &[0], Some(1));
}
#[test]
fn all_zero_is_error() {
    check(
        &[0.0, 0.0],
        &[],
        Err(Error::InsufficientNonZero),
        &[0, 1],
        None,
    );
}
#[test]
fn selected_none_is_ok() {
    let items = [Some(0), None, Some(2)];
    let calls = RefCell::new(0);
    let mut rng = Words::new(&[0]);
    assert_eq!(
        items.choose_weighted(&mut rng, |item| {
            *calls.borrow_mut() += 1;
            if item.is_none() {
                1.0
            } else {
                0.0
            }
        }),
        Ok(&None)
    );
    assert_eq!(*calls.borrow(), 3);
    assert_eq!(rng.draws, 1);
}
#[test]
fn infinity_then_invalid_stops_callback() {
    check(
        &[1.0, f64::INFINITY, -1.0, 1.0],
        &[],
        Err(Error::InvalidWeight),
        &[0, 1, 2],
        Some(3),
    );
}
#[test]
fn nonfinite_sampler_panics_before_rng() {
    let items = [0, 1];
    let reads = RefCell::new(vec![]);
    let mut rng = Words::new(&[0]);
    assert!(catch_unwind(AssertUnwindSafe(|| items.choose_weighted(
        &mut rng,
        |&index| {
            reads.borrow_mut().push(index);
            [1.0, f64::INFINITY][index]
        }
    )))
    .is_err());
    assert_eq!(*reads.borrow(), vec![0, 1]);
    assert_eq!(rng.draws, 0);
}
#[test]
fn weight_failure_propagates() {
    let items = [0, 1];
    let reads = RefCell::new(vec![]);
    let mut rng = Words::new(&[]);
    let result = catch_unwind(AssertUnwindSafe(|| {
        items.choose_weighted(&mut rng, |&index| {
            reads.borrow_mut().push(index);
            if index == 1 {
                std::panic::panic_any(Error::InvalidWeight);
            }
            1.0
        })
    }));
    assert_eq!(
        result.unwrap_err().downcast_ref::<Error>(),
        Some(&Error::InvalidWeight)
    );
    assert_eq!(*reads.borrow(), vec![0, 1]);
    assert_eq!(rng.draws, 0);
}
#[test]
fn rng_failure_propagates() {
    let items = [0, 1];
    let reads = RefCell::new(vec![]);
    let mut rng = Words::new(&[]);
    rng.fail = true;
    let result = catch_unwind(AssertUnwindSafe(|| {
        items.choose_weighted(&mut rng, |&index| {
            reads.borrow_mut().push(index);
            1.0
        })
    }));
    assert_eq!(
        result.unwrap_err().downcast_ref::<&str>(),
        Some(&"rng failure")
    );
    assert_eq!(*reads.borrow(), vec![0, 1]);
    assert_eq!(rng.draws, 1);
}
