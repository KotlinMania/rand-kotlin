use rand::distr::uniform::{SampleRange, SampleUniform};
use rand::{Rng, RngCore};
use std::collections::VecDeque;
use std::fmt::Debug;
use std::ops::RangeInclusive;
use std::panic::{catch_unwind, AssertUnwindSafe};

struct Words {
    words32: VecDeque<u32>,
    words64: VecDeque<u64>,
    calls32: usize,
    calls64: usize,
    fills: usize,
}

impl Words {
    fn new(words32: &[u32], words64: &[u64]) -> Self {
        Self {
            words32: words32.iter().copied().collect(),
            words64: words64.iter().copied().collect(),
            calls32: 0,
            calls64: 0,
            fills: 0,
        }
    }
}

impl RngCore for Words {
    fn next_u32(&mut self) -> u32 {
        self.calls32 += 1;
        self.words32.pop_front().expect("unexpected 32-bit draw")
    }
    fn next_u64(&mut self) -> u64 {
        self.calls64 += 1;
        self.words64.pop_front().expect("unexpected 64-bit draw")
    }
    fn fill_bytes(&mut self, _: &mut [u8]) {
        self.fills += 1;
        panic!("range sampling must use word draws");
    }
}

fn assert_draw<T>(mut rng: Words, range: RangeInclusive<T>, expected: T, calls: (usize, usize))
where
    T: SampleUniform + Debug + PartialEq,
    RangeInclusive<T>: SampleRange<T>,
{
    let actual: T = rng.random_range(range);
    assert_eq!(actual, expected);
    assert_eq!((rng.calls32, rng.calls64, rng.fills), (calls.0, calls.1, 0));
}

#[test]
fn signed_32_bit_range_uses_the_32_bit_uniform_sampler() {
    assert_draw(Words::new(&[0x80000000], &[]), -10i32..=10, 0, (1, 0));
}

#[test]
fn signed_64_bit_range_uses_the_64_bit_uniform_sampler() {
    assert_draw(
        Words::new(&[], &[0x8000000000000000]),
        -10i64..=10,
        0,
        (0, 1),
    );
}

#[test]
fn unsigned_32_bit_range_uses_the_32_bit_uniform_sampler() {
    assert_draw(Words::new(&[0x80000000], &[]), 100u32..=110, 105, (1, 0));
}

#[test]
fn unsigned_64_bit_range_preserves_large_endpoints() {
    assert_draw(
        Words::new(&[], &[0x8000000000000000]),
        (u64::MAX - 10)..=u64::MAX,
        u64::MAX - 5,
        (0, 1),
    );
}

#[test]
fn singleton_ranges_still_consume_one_word() {
    assert_draw(Words::new(&[42], &[]), 7i32..=7, 7, (1, 0));
    assert_draw(Words::new(&[], &[42]), -7i64..=-7, -7, (0, 1));
    assert_draw(
        Words::new(&[42], &[]),
        u32::MAX..=u32::MAX,
        u32::MAX,
        (1, 0),
    );
    assert_draw(
        Words::new(&[], &[42]),
        u64::MAX..=u64::MAX,
        u64::MAX,
        (0, 1),
    );
}

#[test]
fn full_domain_ranges_preserve_all_word_bits() {
    assert_draw(
        Words::new(&[0x80000000], &[]),
        i32::MIN..=i32::MAX,
        i32::MIN,
        (1, 0),
    );
    assert_draw(
        Words::new(&[], &[0x8000000000000000]),
        i64::MIN..=i64::MAX,
        i64::MIN,
        (0, 1),
    );
    assert_draw(
        Words::new(&[0x80000000], &[]),
        0u32..=u32::MAX,
        0x80000000,
        (1, 0),
    );
    assert_draw(
        Words::new(&[], &[0x8000000000000000]),
        0u64..=u64::MAX,
        0x8000000000000000,
        (0, 1),
    );
}

#[test]
fn inclusive_bounds_are_reachable() {
    assert_draw(Words::new(&[0], &[]), -10i32..=10, -10, (1, 0));
    assert_draw(Words::new(&[u32::MAX], &[]), -10i32..=10, 10, (1, 0));
    assert_draw(Words::new(&[], &[0]), -10i64..=10, -10, (0, 1));
    assert_draw(Words::new(&[], &[u64::MAX]), -10i64..=10, 10, (0, 1));
    assert_draw(Words::new(&[0], &[]), 100u32..=110, 100, (1, 0));
    assert_draw(Words::new(&[u32::MAX], &[]), 100u32..=110, 110, (1, 0));
    assert_draw(Words::new(&[], &[0]), 100u64..=110, 100, (0, 1));
    assert_draw(Words::new(&[], &[u64::MAX]), 100u64..=110, 110, (0, 1));
}

#[test]
fn canonical_correction_consumes_the_second_word() {
    assert_draw(Words::new(&[1, u32::MAX], &[]), -1i32..=i32::MAX, 0, (2, 0));
    assert_draw(Words::new(&[], &[1, u64::MAX]), -1i64..=i64::MAX, 0, (0, 2));
    assert_draw(
        Words::new(&[1, u32::MAX], &[]),
        0u32..=0x80000000,
        1,
        (2, 0),
    );
    assert_draw(
        Words::new(&[], &[1, u64::MAX]),
        0u64..=0x8000000000000000,
        1,
        (0, 2),
    );
    assert_draw(Words::new(&[1, 0], &[]), 0u32..=0x80000000, 0, (2, 0));
    assert_draw(
        Words::new(&[], &[1, 0]),
        0u64..=0x8000000000000000,
        0,
        (0, 2),
    );
}

#[test]
fn empty_ranges_panic_before_using_the_rng() {
    let mut rng = Words::new(&[], &[]);
    assert!(catch_unwind(AssertUnwindSafe(|| rng.random_range(3i32..=2))).is_err());
    assert!(catch_unwind(AssertUnwindSafe(|| rng.random_range(3i64..=2))).is_err());
    assert!(catch_unwind(AssertUnwindSafe(|| rng.random_range(3u32..=2))).is_err());
    assert!(catch_unwind(AssertUnwindSafe(|| rng.random_range(3u64..=2))).is_err());
    assert_eq!((rng.calls32, rng.calls64, rng.fills), (0, 0, 0));
}
