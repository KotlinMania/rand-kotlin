use rand::{
    distr::{weighted::WeightedIndex, Distribution},
    RngCore,
};
struct Words {
    words: std::collections::VecDeque<u64>,
    draws: usize,
}
impl Words {
    fn new(words: &[u64]) -> Self {
        Self {
            words: words.iter().copied().collect(),
            draws: 0,
        }
    }
}
impl RngCore for Words {
    fn next_u32(&mut self) -> u32 {
        panic!("unexpected u32 draw")
    }
    fn next_u64(&mut self) -> u64 {
        self.draws += 1;
        self.words.pop_front().expect("unexpected extra draw")
    }
    fn fill_bytes(&mut self, _: &mut [u8]) {
        panic!("unexpected byte draw")
    }
}
fn choices(weights: &[f64], words: &[u64], expected: &[usize]) {
    let dist = WeightedIndex::new(weights).unwrap();
    let mut rng = Words::new(words);
    let got: Vec<_> = words.iter().map(|_| dist.sample(&mut rng)).collect();
    assert_eq!(got, expected);
    assert_eq!(rng.draws, words.len());
    assert!(rng.words.is_empty());
}
#[test]
fn maximal_word_normal_weight() {
    choices(&[1.0, 0.0], &[u64::MAX], &[0]);
}
#[test]
fn low_twelve_bits_are_discarded() {
    choices(
        &[1.0, 2.0],
        &[0x5555555555555000, 0x5555555555555fff],
        &[0, 0],
    );
}
#[test]
fn zero_weight_boundaries() {
    choices(
        &[0.0, 2.0, 0.0, 3.0, 0.0],
        &[0, 0x8000000000000000, u64::MAX],
        &[1, 3, 3],
    );
}
#[test]
fn successful_update_refreshes_sampler() {
    let mut dist = WeightedIndex::new([1.0, 1.0]).unwrap();
    let mut rng = Words::new(&[0xc000000000000000, 0xc000000000000000]);
    assert_eq!(dist.sample(&mut rng), 1);
    dist.update_weights(&[(0, &3.0)]).unwrap();
    assert_eq!(dist.weights().collect::<Vec<_>>(), vec![3.0, 1.0]);
    assert_eq!(dist.total_weight(), 4.0);
    assert_eq!(dist.sample(&mut rng), 1);
    assert_eq!(rng.draws, 2);
}
#[test]
fn rejected_update_preserves_sampler() {
    let mut dist = WeightedIndex::new([1.0, 2.0]).unwrap();
    assert_eq!(
        dist.update_weights(&[(0, &-1.0)]),
        Err(rand::distr::weighted::Error::InvalidWeight)
    );
    assert_eq!(dist.weights().collect::<Vec<_>>(), vec![1.0, 2.0]);
    assert_eq!(dist.total_weight(), 3.0);
    let mut rng = Words::new(&[0x5555555555555555]);
    assert_eq!(dist.sample(&mut rng), 0);
    assert_eq!(rng.draws, 1);
}
#[test]
fn subnormal_rounding_is_not_clamped() {
    choices(&[f64::from_bits(1), 0.0], &[u64::MAX], &[1]);
}
