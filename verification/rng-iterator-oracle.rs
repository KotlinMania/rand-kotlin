// Run after bash verification/run-rng-range-oracle.sh:
// rustc --test --edition 2021 -L dependency=build/rust-kotlin-range-parity --extern rand=build/rust-kotlin-range-parity/librand_upstream.rlib --extern rand_core=build/rust-kotlin-range-parity/librand_core.rlib verification/rng-iterator-oracle.rs -o build/rust-kotlin-reservoir/iterator-oracle
// build/rust-kotlin-reservoir/iterator-oracle
#[path = "rng-range-oracle.rs"]
mod ranges;
use ranges::Words;
use rand::seq::IteratorRandom;
use rand::Rng;

#[test]
fn replacement_preserves_reservoir_order_and_word_draws() {
    let mut rng = Words::new(&[0, u32::MAX, 0], &[]);
    assert_eq!((0..5).choose_multiple(&mut rng, 2), vec![4, 1]);
    assert_eq!(rng.counts(), (3, 0, 0));
}
#[test]
fn fill_uses_the_same_replacements() {
    let mut rng = Words::new(&[0, u32::MAX, 0], &[]);
    let mut buf = [-1; 2];
    assert_eq!((0..5).choose_multiple_fill(&mut rng, &mut buf), 2);
    assert_eq!(buf, [4, 1]);
    assert_eq!(rng.counts(), (3, 0, 0));
}
#[test]
fn short_and_exact_inputs_need_no_randomness() {
    let mut rng = Words::new(&[], &[]);
    assert_eq!((0..3).choose_multiple(&mut rng, 5), vec![0, 1, 2]);
    assert_eq!((0..3).choose_multiple(&mut rng, 3), vec![0, 1, 2]);
    let mut buf = [-1; 5];
    assert_eq!((0..3).choose_multiple_fill(&mut rng, &mut buf), 3);
    assert_eq!(buf, [0, 1, 2, -1, -1]);
    assert_eq!(rng.counts(), (0, 0, 0));
}
#[test]
fn zero_capacity_still_consumes_input_and_samples_each_item() {
    let mut rng = Words::new(&[0, 0, 0], &[]);
    let mut iter = 0..3;
    assert_eq!((&mut iter).choose_multiple(&mut rng, 0), Vec::<i32>::new());
    assert_eq!(iter.next(), None);
    assert_eq!(rng.counts(), (3, 0, 0));
    let mut rng = Words::new(&[0, 0, 0], &[]);
    let mut iter = 0..3;
    assert_eq!((&mut iter).choose_multiple_fill(&mut rng, &mut []), 0);
    assert_eq!(iter.next(), None);
    assert_eq!(rng.counts(), (3, 0, 0));
}
struct Restarting { calls: usize }
impl Iterator for Restarting {
    type Item = i32;
    fn next(&mut self) -> Option<i32> {
        self.calls += 1;
        match self.calls { 1 => Some(7), 2 => None, 3 => Some(9), _ => None }
    }
}
#[test]
fn first_exhaustion_stops_both_operations() {
    let mut rng = Words::new(&[], &[]);
    let mut iter = Restarting { calls: 0 };
    assert_eq!((&mut iter).choose_multiple(&mut rng, 3), vec![7]);
    assert_eq!(iter.calls, 2);
    assert_eq!(iter.next(), Some(9));
    let mut iter = Restarting { calls: 0 };
    let mut buf = [-1; 3];
    assert_eq!((&mut iter).choose_multiple_fill(&mut rng, &mut buf), 1);
    assert_eq!(buf, [7, -1, -1]);
    assert_eq!(iter.calls, 2);
    assert_eq!(rng.counts(), (0, 0, 0));
}
#[test]
fn optional_items_remain_items() {
    let mut rng = Words::new(&[], &[]);
    assert_eq!([None, Some(1)].into_iter().choose_multiple(&mut rng, 3), vec![None, Some(1)]);
    let mut buf = [Some(-1); 3];
    assert_eq!([None, Some(1)].into_iter().choose_multiple_fill(&mut rng, &mut buf), 2);
    assert_eq!(buf, [None, Some(1), Some(-1)]);
    assert_eq!(rng.counts(), (0, 0, 0));
}
#[test]
#[cfg(target_pointer_width = "64")]
fn usize_range_switches_word_width_at_exclusive_upper_bound() {
    let mut rng = Words::new(&[0], &[]);
    assert_eq!(rng.random_range(0usize..u32::MAX as usize), 0);
    assert_eq!(rng.counts(), (1, 0, 0));
    for upper in [u32::MAX as usize + 1, usize::MAX] {
        let mut rng = Words::new(&[], &[0]);
        assert_eq!(rng.random_range(0usize..upper), 0);
        assert_eq!(rng.counts(), (0, 1, 0));
    }
}

#[test]
fn iterator_failure_preserves_the_filled_prefix_without_random_draws() {
    let mut rng = Words::new(&[], &[]);
    let mut buf = [-1; 3];
    let result = std::panic::catch_unwind(std::panic::AssertUnwindSafe(|| {
        (0..3).map(|x| { if x == 2 { panic!("iterator failure") } x })
            .choose_multiple_fill(&mut rng, &mut buf)
    }));
    assert!(result.is_err());
    assert_eq!(buf, [0, 1, -1]);
    assert_eq!(rng.counts(), (0, 0, 0));
}
