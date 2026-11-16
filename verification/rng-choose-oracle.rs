// Link against unchanged local Rand, using the libraries built by
// bash verification/run-rng-range-oracle.sh.
// rustc --test --edition 2021 -L dependency=build/rust-kotlin-range-parity --extern rand=build/rust-kotlin-range-parity/librand_upstream.rlib --extern rand_core=build/rust-kotlin-range-parity/librand_core.rlib verification/rng-choose-oracle.rs -o build/rust-kotlin-choose/choose-oracle
// build/rust-kotlin-choose/choose-oracle --nocapture
#[path = "rng-range-oracle.rs"]
mod ranges;
use ranges::Words;
use rand::seq::IteratorRandom;
pub use rand::RngCore;
#[path = "../tmp/rand/src/seq/coin_flipper.rs"]
mod coin_flipper;

struct Unhinted<I>(I);
impl<I: Iterator> Iterator for Unhinted<I> {
    type Item = I::Item;
    fn next(&mut self) -> Option<Self::Item> { self.0.next() }
}
#[test]
fn unhinted_choices_and_draw_counts() {
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
            let mut rng = Words::new(&vec![word; 1000], &[]);
            assert_eq!(Unhinted(0..len).choose(&mut rng), results[i]);
            assert_eq!(rng.counts(), (draws[i], 0, 0));
        }
    }
}
#[test]
fn large_denominators() {
    for d in [i32::MAX as usize, 1usize << 31, (1usize << 32) - 1, 1usize << 32, usize::MAX] {
        let rng = Words::new(&[u32::MAX; 8], &[]);
        let mut coin = coin_flipper::CoinFlipper::new(rng);
        let result = coin.random_ratio_one_over(d);
        assert!(!result);
        assert_eq!(coin.rng.counts(), (1, 0, 0));
    }
}

#[test]
fn nullable_items_do_not_end_iteration() {
    let mut rng = Words::new(&[0], &[]);
    assert_eq!(Unhinted([None, Some(1), None, Some(3)].into_iter()).choose(&mut rng), Some(Some(3)));
    assert_eq!(rng.counts(), (1, 0, 0));
}
#[test]
fn iterator_failure_after_a_null_item_propagates_without_random_draws() {
    let mut rng = Words::new(&[], &[]);
    let result = std::panic::catch_unwind(std::panic::AssertUnwindSafe(|| {
        let iter = (0..2).map(|i| { if i == 1 { panic!("iterator failure"); } None::<i32> });
        Unhinted(iter).choose(&mut rng)
    }));
    assert!(result.is_err());
    assert_eq!(rng.counts(), (0, 0, 0));
}

#[test]
fn heads_preserve_unsigned_numerators_above_the_signed_int_range() {
    for d in [i32::MAX as usize, 1usize << 31, u32::MAX as usize] {
        let mut coin = coin_flipper::CoinFlipper::new(Words::new(&[0], &[]));
        assert!(coin.random_ratio_one_over(d));
        assert_eq!(coin.rng.counts(), (1, 0, 0));
    }
}
#[test]
fn wrapping_tail_consumption_preserves_upstream_chunk_state() {
    let mut coin = coin_flipper::CoinFlipper::new(Words::new(&[1], &[]));
    assert!(!coin.random_ratio_one_over(usize::MAX));
    assert!(!coin.random_ratio_one_over(usize::MAX));
    assert_eq!(coin.rng.counts(), (1, 0, 0));
}
