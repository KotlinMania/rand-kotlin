use rand::{
    distr::{Bernoulli, Distribution},
    Rng, RngCore,
};
pub const WORDS: [u64; 17] = [
    0,
    1,
    4294967296,
    4294967297,
    6148914691236516863,
    6148914691236516864,
    6148914691236516865,
    9223372036854775807,
    9223372036854775808,
    9223372036854775809,
    12297829382473033727,
    12297829382473033728,
    12297829382473033729,
    18446744069414584319,
    18446744069414584320,
    18446744069414584321,
    u64::MAX,
];
pub const RATIOS: [(u32, u32); 8] = [
    (0, 1),
    (1, 1),
    (1, 2),
    (1, 3),
    (2, 3),
    (1, u32::MAX),
    (u32::MAX - 1, u32::MAX),
    (u32::MAX, u32::MAX),
];
pub struct Words {
    pub word: u64,
    pub draws: usize,
    pub fail: bool,
}
impl RngCore for Words {
    fn next_u32(&mut self) -> u32 {
        panic!("unexpected 32-bit draw")
    }
    fn next_u64(&mut self) -> u64 {
        self.draws += 1;
        assert!(!self.fail, "rng failure");
        self.word
    }
    fn fill_bytes(&mut self, _: &mut [u8]) {
        panic!("unexpected byte draw")
    }
}
pub fn row(n: u32, d: u32) -> (u64, Vec<bool>, Vec<usize>) {
    let dist = Bernoulli::from_ratio(n, d).unwrap();
    let mut values = vec![];
    let mut counts = vec![];
    for word in WORDS {
        let mut direct = Words {
            word,
            draws: 0,
            fail: false,
        };
        let mut wrapped = Words {
            word,
            draws: 0,
            fail: false,
        };
        let value = dist.sample(&mut direct);
        assert_eq!(value, wrapped.random_ratio(n, d));
        assert_eq!(direct.draws, if n == d { 0 } else { 1 });
        assert_eq!(direct.draws, wrapped.draws);
        values.push(value);
        counts.push(direct.draws);
    }
    (dist.p().to_bits(), values, counts)
}
#[cfg(test)]
mod tests {
    use super::*;
    use rand::distr::BernoulliError;
    use std::panic::{catch_unwind, AssertUnwindSafe};
    #[test]
    fn invalid_ratios_are_typed_errors() {
        for (n, d) in [(0, 0), (1, 0), (2, 1), (u32::MAX, u32::MAX - 1)] {
            assert_eq!(
                Bernoulli::from_ratio(n, d),
                Err(BernoulliError::InvalidProbability)
            );
        }
        assert_eq!(
            BernoulliError::InvalidProbability.to_string(),
            "p is outside [0, 1] in Bernoulli distribution"
        );
    }
    #[test]
    fn probability_bits_and_boundary_samples() {
        assert_eq!(row(0, 1).0, 0);
        assert_eq!(row(1, 1).0, 1f64.to_bits());
        assert_eq!(row(1, 2).0, 0.5f64.to_bits());
        assert!(row(0, 1).1.iter().all(|x| !x));
        assert!(row(1, 1).1.iter().all(|x| *x));
        assert!(row(1, 2).1[7]);
        assert!(!row(1, 2).1[8]);
    }
    #[test]
    fn direct_and_public_matrix_match_with_exact_draws() {
        for (n, d) in RATIOS {
            row(n, d);
        }
    }
    #[test]
    fn public_invalid_ratio_panics_before_draw() {
        for (n, d) in [(0, 0), (1, 0), (2, 1), (u32::MAX, u32::MAX - 1)] {
            let mut rng = Words {
                word: 0,
                draws: 0,
                fail: false,
            };
            let err = catch_unwind(AssertUnwindSafe(|| rng.random_ratio(n, d))).unwrap_err();
            let text = if let Some(s) = err.downcast_ref::<&str>() {
                s.to_string()
            } else {
                err.downcast_ref::<String>().unwrap().clone()
            };
            assert_eq!(text, format!("p={}/{} is outside range [0.0, 1.0]", n, d));
            assert_eq!(rng.draws, 0);
        }
    }
    #[test]
    fn only_probability_one_bypasses_failing_rng() {
        let mut direct = Words {
            word: 0,
            draws: 0,
            fail: true,
        };
        assert!(Bernoulli::from_ratio(1, 1).unwrap().sample(&mut direct));
        assert_eq!(direct.draws, 0);
        let mut wrapped = Words {
            word: 0,
            draws: 0,
            fail: true,
        };
        assert!(wrapped.random_ratio(u32::MAX, u32::MAX));
        assert_eq!(wrapped.draws, 0);
        assert!(catch_unwind(AssertUnwindSafe(|| Bernoulli::from_ratio(0, 1)
            .unwrap()
            .sample(&mut direct)))
        .is_err());
        assert_eq!(direct.draws, 1);
        assert!(catch_unwind(AssertUnwindSafe(|| wrapped.random_ratio(0, 1))).is_err());
        assert_eq!(wrapped.draws, 1);
    }
}
