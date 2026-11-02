# Indexed choose single-sample parity

This repair starts from clean Rand `ae32485`; no prior dirty work was included.
Earlier WIP checkpoint `a57f0cd` is separate work. Graph review confirmed Rand
has 30 distinct consumers and JSON has 21. A read-only JSON pointer review was
followed by an attempted scratch translation, rejected before execution by
automatic approval review because it considered the active scope Rand-only.
There was no retry, workaround or JSON edit. Work continued within Rand.

## Exact source comparison

The bounded translation of unchanged Rand 0.9.3 `seq/slice.rs` completed with
exit 0. Scratch `build/rust-kotlin-indexed-choose/SliceReview.kt:53–58` retains
the empty check and `rng.randomRange(0 until this.len())`. This is a manual
consumer adaptation of that output and exact Rust source; the generated file
was not installed and no generator was changed.

Upstream `IndexedRandom::choose` returns None for empty input, otherwise samples
one usize index via random_range. The small-range UniformUsize single-sample
path delegates to the u32 Canon correction algorithm. This differs from the
cached UniformUsize rejection path used by the Choose distribution. The repair
uses the existing canonical UniformUsize.sampleSingle in all eight existing
choose adapters: List, Array, IntArray, LongArray, ByteArray, ShortArray,
FloatArray and DoubleArray. It does not change that canonical sampler, signatures,
empty handling, element indexing or other methods in the file.

For 52 items, first word 0x3b13b13b yields high 11 and low 0xfffffffc when
multiplied by 52. The correction path consumes another word: second zero selects
index 11, and second u32::MAX selects 12. These cases distinguish exact upstream
single-sample consumption from modulo or cached-range sampling. A singleton
consumes one u32; empty input consumes nothing.

## Actual regression evidence

`bash verification/run-indexed-choose-oracle.sh` compiles unchanged Rand with
cached rand_core 0.9.3, std+alloc and no optional unbiased feature. Seven actual
Rust tests cover empty/singleton inputs, high-bit selection, maximal words, both
correction outcomes and a present selected None. Six numeric element types are
checked in Rust. Kotlin final tests check all eight adapters using exactly
representable integer-valued elements, selected null and u32/u64 draw counts.
Unexpected byte draws fail. No downloads or configuration changes are used.

Six new Kotlin tests failed before repair; the empty-input control passed.
Multi-adapter red tests stop on the first List failure, so they do not establish
eight independent red executions. Final green tests execute every adapter.
Final-source JVM and macOS ARM suites each pass 154 tests, including all seven
new cases and all prior helper/public Open01, weighted, slice-distribution and
Alphabetic suites. All seven actual Rust oracle tests pass. Existing assertions
were not changed. Detekt has zero findings. Ktlint still fails with five findings
in byte-identical UniformOther.kt and rngs/Os.kt; none is on authored code.
Attribution excludes old Format reports. Raw logs and the source hash manifest
are in `build/rust-kotlin-indexed-choose/`; the durable receipt records commands,
red coverage limits, suite timestamps and hashes.

## Remaining limits

No public signature changed, so no caller migration was required; downstream
builds were not run. Kotlin T? still cannot distinguish an accepted null item
from absence, while the Rust oracle distinguishes Some(None) from None. The
selected-null regression does not claim to resolve this API limitation.
Borrowing/lifetimes, usize ranges beyond Kotlin container sizes, other platforms
and the optional unbiased feature remain unverified. Existing chooseMut and
chooseMutIndex still return indices and use modulo; they were not changed or
verified here. Weighted/multiple selection and shuffling are outside this bounded
repair. No complete Rand parity or statistical/security claim is made. No remote
CI, push, PR, merge, tag, publication or configuration action was performed.
