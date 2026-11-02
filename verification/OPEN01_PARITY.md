# Open01 Float numeric conversion

This step begins with a clean Rand `3f27b9b` tree. No prior dirty changes were
included; the earlier WIP checkpoint `a57f0cd` is separate work. Rand serves 30
distinct workspace consumers in the dependency graph. This bounded defect in
an existing distribution was selected over adding typed diagnostic APIs or
moving into Serde's unresolved OsString platform representation.

## Source comparison

The local rust-kotlin CLI completed the bounded translation of
`tmp/rand/src/distr/float.rs` to `build/rust-kotlin-open01/FloatReview.kt`
with exit 0. It emits the IntoFloat declaration, distribution declarations and
edge tests, but omits the macro-generated implementations. This is a manual
consumer repair from the exact upstream macro body, not a generator fix or
runnable replacement generated file. The CLI and scratch hashes are recorded
in `open01-receipt.json`; there was no retry or duplicate translator process.

Rand 0.9.3 `float.rs:99–104` constructs a float by combining fraction and
exponent bits. Lines 141–151 sample Open01 by using the upper 23/52 RNG bits,
then subtracting `1 - EPSILON/2` from that floating-point value. The existing
Kotlin UIntIntoFloat helper already constructs the Float and widens its numeric
value to Double to satisfy the current shared interface. Open01 incorrectly
converted this value to Int, then used that integer as raw Float bits. For the
valid fraction range, this truncates every result in `[1,2)` to integer 1,
yielding a tiny subnormal bit pattern and then a negative sample after subtraction.

The one-line repair converts the helper's numeric Double result back to Float,
which is exact because the helper's value came from Float. It retains the
canonical helper, RNG word width, bit discard, subtraction and existing APIs.
Double sampling and all other distributions are unchanged. No caller migration
is required because no public signature changed; downstream builds were not run.

## Validation

`bash verification/run-open01-oracle.sh` compiles unchanged upstream Rand and
cached rand_core 0.9.3, then runs six actual Rust tests. No downloads or dependency
changes are needed. Fixed expected bit patterns cover zero and maximal words,
the first retained fraction bit, ignored low 9/12 bits and midpoint values.
Each sampled value must be strictly inside `(0,1)`. Each case checks exact RNG
consumption: one u32 for Float or one u64 for Double, with byte draws rejected.
The six test functions exercise 17 word/type pairs, mirrored in Kotlin.

Before repair, four Float tests failed and both Double controls passed. After
repair, final-source JVM and macOS ARM suites each pass 127 tests, including all
six Open01 tests and all six prior WeightedSamplingParityTest cases. The six
actual Rust oracle tests pass. Detekt reports zero findings. Ktlint fails with
five findings in byte-identical `UniformOther.kt` and `rngs/Os.kt`; none is on
authored code. Existing assertions were not changed. The durable JSON receipt
contains suite timestamps, red observations, final counts and source hashes;
raw execution logs and hash manifest are in `build/rust-kotlin-open01/`.

## Limits

This step does not implement missing macro expansion, SIMD distributions or
redesign the shared IntoFloat interface. Other targets, downstream compilation,
statistical quality and cryptographic suitability are unverified. Rand's
existing generic-weight, ownership and usize representation limits remain;
this repair does not claim they are resolved. No dependency pins, configuration,
remote workflows, releases or publications were changed.
