// Build the libraries with bash verification/run-rng-range-oracle.sh, then:
// rustc --test --edition 2021 -L dependency=build/rust-kotlin-range-parity --extern rand=build/rust-kotlin-range-parity/librand_upstream.rlib --extern rand_core=build/rust-kotlin-range-parity/librand_core.rlib verification/rng-stable-oracle.rs -o build/rust-kotlin-stable/stable-oracle
// build/rust-kotlin-stable/stable-oracle
#[path = "rng-range-oracle.rs"]
mod ranges;
use ranges::Words;
use rand::seq::IteratorRandom;

struct Unhinted<I>(I);
impl<I: Iterator> Iterator for Unhinted<I> {
    type Item = I::Item;
    fn next(&mut self) -> Option<Self::Item> { self.0.next() }
}
struct PartialHint<I>(I);
impl<I: ExactSizeIterator> Iterator for PartialHint<I> {
    type Item = I::Item;
    fn next(&mut self) -> Option<Self::Item> { self.0.next() }
    fn size_hint(&self) -> (usize, Option<usize>) { (self.0.len() / 2, None) }
}
#[test]
fn exact_partial_and_absent_hints_preserve_choices_and_draws() {
    let cases = [
        (0, [None, None, None], [0, 0, 0]),
        (1, [Some(0), Some(0), Some(0)], [0, 0, 0]),
        (2, [Some(1), Some(0), Some(0)], [1, 1, 1]),
        (3, [Some(2), Some(0), Some(2)], [1, 1, 1]),
        (7, [Some(6), Some(0), Some(2)], [1, 1, 1]),
        (16, [Some(15), Some(0), Some(7)], [2, 1, 1]),
        (33, [Some(32), Some(0), Some(7)], [5, 1, 2]),
        (100, [Some(99), Some(0), Some(7)], [18, 4, 5]),
        (1000, [Some(999), Some(0), Some(7)], [281, 32, 50]),
    ];
    for (len, results, draws) in cases {
        for (i, word) in [0, u32::MAX, 0x9e3779b9].into_iter().enumerate() {
            for hints in 0..3 {
                let mut rng = Words::new(&vec![word; 1000], &[]);
                let chosen = match hints {
                    0 => Unhinted(0..len).choose_stable(&mut rng),
                    1 => PartialHint(0..len).choose_stable(&mut rng),
                    _ => (0..len).choose_stable(&mut rng),
                };
                assert_eq!(chosen, results[i]);
                assert_eq!(rng.counts(), (draws[i], 0, 0));
            }
        }
    }
}
struct Constructed<'a> {
    inner: std::ops::Range<i32>,
    produced: &'a mut usize,
}
impl Iterator for Constructed<'_> {
    type Item = i32;
    fn next(&mut self) -> Option<i32> {
        self.inner.next().map(|x| { *self.produced += 1; x })
    }
    fn nth(&mut self, n: usize) -> Option<i32> {
        self.inner.nth(n).map(|x| { *self.produced += 1; x })
    }
    fn size_hint(&self) -> (usize, Option<usize>) { self.inner.size_hint() }
}
#[test]
fn hint_optimization_changes_construction_but_not_rng_or_selection() {
    let mut produced = 0;
    let mut rng = Words::new(&[u32::MAX; 4], &[]);
    let result = Constructed { inner: 0..100, produced: &mut produced }.choose_stable(&mut rng);
    assert_eq!(result, Some(0));
    assert_eq!(produced, 1);
    assert_eq!(rng.counts(), (4, 0, 0));
    let mut produced = 0;
    let mut rng = Words::new(&[u32::MAX; 4], &[]);
    let result = Unhinted(Constructed { inner: 0..100, produced: &mut produced }).choose_stable(&mut rng);
    assert_eq!(result, Some(0));
    assert_eq!(produced, 100);
    assert_eq!(rng.counts(), (4, 0, 0));
}
#[test]
fn nullable_items_are_not_exhaustion() {
    let mut rng = Words::new(&[0], &[]);
    assert_eq!(Unhinted([None, Some(1), None, Some(3)].into_iter()).choose_stable(&mut rng), Some(Some(3)));
    assert_eq!(rng.counts(), (1, 0, 0));
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
fn first_exhaustion_ends_sampling() {
    let mut rng = Words::new(&[], &[]);
    let mut iter = Restarting { calls: 0 };
    assert_eq!((&mut iter).choose_stable(&mut rng), Some(7));
    assert_eq!(iter.calls, 2);
    assert_eq!(iter.next(), Some(9));
    assert_eq!(rng.counts(), (0, 0, 0));
}
#[test]
fn failure_after_first_item_propagates_before_any_draw() {
    let mut rng = Words::new(&[], &[]);
    let result = std::panic::catch_unwind(std::panic::AssertUnwindSafe(|| {
        Unhinted((0..2).map(|i| { if i == 1 { panic!("iterator failure"); } i }))
            .choose_stable(&mut rng)
    }));
    assert!(result.is_err());
    assert_eq!(rng.counts(), (0, 0, 0));
}
