use rand::{
    distr::{slice::Choose, Distribution},
    RngCore,
};
struct Words {
    values: std::collections::VecDeque<u32>,
    draws32: usize,
    draws64: usize,
}
impl Words {
    fn new(values: &[u32]) -> Self {
        Self {
            values: values.iter().copied().collect(),
            draws32: 0,
            draws64: 0,
        }
    }
}
impl RngCore for Words {
    fn next_u32(&mut self) -> u32 {
        self.draws32 += 1;
        self.values.pop_front().expect("extra draw")
    }
    fn next_u64(&mut self) -> u64 {
        self.draws64 += 1;
        panic!("unexpected u64 draw")
    }
    fn fill_bytes(&mut self, _: &mut [u8]) {
        panic!("unexpected byte draw")
    }
}
fn check<T: PartialEq + core::fmt::Debug>(items: &[T], words: &[u32], expected: &[T]) {
    let dist = Choose::new(items).unwrap();
    assert_eq!(dist.num_choices().get(), items.len());
    let mut rng = Words::new(words);
    let actual: Vec<_> = expected.iter().map(|_| dist.sample(&mut rng)).collect();
    assert_eq!(actual, expected.iter().collect::<Vec<_>>());
    assert_eq!(rng.draws32, words.len());
    assert_eq!(rng.draws64, 0);
    assert!(rng.values.is_empty());
}
#[test]
fn singleton_draws_u32() {
    check(&[9], &[u32::MAX], &[9]);
}
#[test]
fn power_of_two_uses_high_bits() {
    check(
        &[0, 1, 2, 3],
        &[0, 0x40000000, 0x80000000, u32::MAX],
        &[0, 1, 2, 3],
    );
}
#[test]
fn rejected_word_is_retried() {
    check(&[0, 1, 2], &[0, u32::MAX], &[2]);
}
#[test]
fn repeated_samples_use_the_same_range() {
    check(&[0, 1, 2], &[0, u32::MAX, 0x80000000], &[2, 1]);
}
#[test]
fn selected_none_is_a_value() {
    check(&[Some(0), None, Some(2)], &[0x80000000], &[None]);
}
#[test]
fn array_elements_preserve_selection() {
    check(&["a", "b", "c", "d"], &[0x80000000], &["c"]);
}
#[test]
fn empty_is_rejected() {
    assert!(Choose::<u8>::new(&[]).is_err());
}

#[test]
fn empty_has_exact_error_type_and_display() {
    use rand::distr::slice::Empty;
    let result: Result<Choose<'_, u8>, Empty> = Choose::new(&[]);
    let error = match result {
        Err(error) => error,
        Ok(_) => panic!("empty slice must return Err"),
    };
    assert_eq!(
        error.to_string(),
        "Tried to create a `rand::distr::slice::Choose` with an empty slice"
    );
}
#[test]
fn payload_needs_no_clone_and_is_borrowed() {
    struct NoClone;
    let items = [NoClone];
    let dist = Choose::new(&items).unwrap();
    let mut rng = Words::new(&[u32::MAX]);
    assert_eq!(dist.num_choices().get(), 1);
    assert!(core::ptr::eq(dist.sample(&mut rng), &items[0]));
    assert_eq!(rng.draws32, 1);
    assert_eq!(rng.draws64, 0);
}
