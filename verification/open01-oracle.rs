use rand::{
    distr::{Distribution, Open01},
    RngCore,
};
struct Word {
    value: u64,
    draws32: usize,
    draws64: usize,
}
impl RngCore for Word {
    fn next_u32(&mut self) -> u32 {
        self.draws32 += 1;
        self.value as u32
    }
    fn next_u64(&mut self) -> u64 {
        self.draws64 += 1;
        self.value
    }
    fn fill_bytes(&mut self, _: &mut [u8]) {
        panic!("unexpected byte draw")
    }
}
fn float(word: u32, bits: u32) {
    let mut rng = Word {
        value: word.into(),
        draws32: 0,
        draws64: 0,
    };
    let actual: f32 = Open01.sample(&mut rng);
    assert_eq!(actual.to_bits(), bits, "word {word:#x}");
    assert!(actual > 0.0 && actual < 1.0);
    assert_eq!((rng.draws32, rng.draws64), (1, 0));
}
fn double(word: u64, bits: u64) {
    let mut rng = Word {
        value: word,
        draws32: 0,
        draws64: 0,
    };
    let actual: f64 = Open01.sample(&mut rng);
    assert_eq!(actual.to_bits(), bits, "word {word:#x}");
    assert!(actual > 0.0 && actual < 1.0);
    assert_eq!((rng.draws32, rng.draws64), (0, 1));
}
#[test]
fn float_zero_word() {
    float(0, 0x33800000);
}
#[test]
fn float_max_word() {
    float(u32::MAX, 0x3f7fffff);
}
#[test]
fn float_first_fraction_step() {
    float(512, 0x34400000);
}
#[test]
fn float_discarded_bits_and_midpoint() {
    for word in [0, 511] {
        float(word, 0x33800000);
    }
    for word in [512, 1023] {
        float(word, 0x34400000);
    }
    for word in [0x80000000, 0x800001ff] {
        float(word, 0x3f000001);
    }
}
#[test]
fn double_endpoints() {
    double(0, 0x3ca0000000000000);
    double(u64::MAX, 0x3fefffffffffffff);
}
#[test]
fn double_discarded_bits_and_midpoint() {
    for word in [0, 4095] {
        double(word, 0x3ca0000000000000);
    }
    for word in [4096, 8191] {
        double(word, 0x3cb8000000000000);
    }
    for word in [0x8000000000000000, 0x8000000000000fff] {
        double(word, 0x3fe0000000000001);
    }
}
