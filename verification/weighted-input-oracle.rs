use rand::{
    distr::{
        weighted::{Error, WeightedIndex},
        Distribution,
    },
    RngCore,
};
use std::panic::{catch_unwind, AssertUnwindSafe};
struct Probe {
    values: Vec<f64>,
    reads: Vec<usize>,
    index: usize,
    fail_at: Option<usize>,
    panic_error: bool,
}
impl Probe {
    fn new(values: &[f64], fail_at: Option<usize>) -> Self {
        Self {
            values: values.to_vec(),
            reads: vec![],
            index: 0,
            fail_at,
            panic_error: false,
        }
    }
}
impl Iterator for Probe {
    type Item = f64;
    fn next(&mut self) -> Option<f64> {
        let value = *self.values.get(self.index)?;
        self.reads.push(self.index);
        if self.fail_at == Some(self.index) {
            if self.panic_error {
                std::panic::panic_any(Error::InvalidWeight);
            }
            panic!("element failure");
        }
        self.index += 1;
        Some(value)
    }
}
fn invalid(values: &[f64], fail_at: usize, expected: &[usize]) {
    let mut probe = Probe::new(values, Some(fail_at));
    assert!(matches!(
        WeightedIndex::new(&mut probe),
        Err(Error::InvalidWeight)
    ));
    assert_eq!(probe.reads, expected);
}
#[test]
fn first_invalid_stops_input() {
    invalid(&[-1.0, 4.0], 1, &[0]);
}
#[test]
fn later_invalid_stops_input() {
    invalid(&[1.0, -1.0, 4.0], 2, &[0, 1]);
}
#[test]
fn nan_stops_input() {
    invalid(&[1.0, f64::NAN, 4.0], 2, &[0, 1]);
}
#[test]
fn infinity_then_invalid_stops_input() {
    invalid(&[1.0, f64::INFINITY, -1.0, 4.0], 3, &[0, 1, 2]);
}
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
#[test]
fn valid_input_is_consumed_and_sampled() {
    let mut probe = Probe::new(&[1.0, 0.0, 2.0], None);
    let dist = WeightedIndex::new(&mut probe).unwrap();
    assert_eq!(probe.reads, vec![0, 1, 2]);
    assert_eq!(dist.weights().collect::<Vec<_>>(), vec![1.0, 0.0, 2.0]);
    assert_eq!(dist.total_weight(), 3.0);
    let mut rng = Word { draws: 0 };
    assert_eq!(dist.sample(&mut rng), 2);
    assert_eq!(rng.draws, 1);
}
#[test]
fn empty_input_is_invalid_without_element_reads() {
    let mut probe = Probe::new(&[], None);
    assert!(matches!(
        WeightedIndex::new(&mut probe),
        Err(Error::InvalidInput)
    ));
    assert!(probe.reads.is_empty());
}
#[test]
fn infinite_total_reads_all_then_panics() {
    let mut probe = Probe::new(&[f64::MAX, f64::MAX], None);
    assert!(catch_unwind(AssertUnwindSafe(|| WeightedIndex::new(&mut probe))).is_err());
    assert_eq!(probe.reads, vec![0, 1]);
}
#[test]
fn element_failure_propagates() {
    let mut probe = Probe::new(&[1.0, 4.0], Some(1));
    let result = catch_unwind(AssertUnwindSafe(|| WeightedIndex::new(&mut probe)));
    let payload = result.unwrap_err();
    assert_eq!(payload.downcast_ref::<&str>(), Some(&"element failure"));
    assert_eq!(probe.reads, vec![0, 1]);
}

#[test]
fn weight_error_payload_from_input_still_panics() {
    let mut probe = Probe::new(&[1.0, 4.0], Some(1));
    probe.panic_error = true;
    let result = catch_unwind(AssertUnwindSafe(|| WeightedIndex::new(&mut probe)));
    let payload = result.unwrap_err();
    assert_eq!(payload.downcast_ref::<Error>(), Some(&Error::InvalidWeight));
    assert_eq!(probe.reads, vec![0, 1]);
}
