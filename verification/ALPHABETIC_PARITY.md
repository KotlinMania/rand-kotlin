# Alphabetic single-sample range parity

This repair begins with clean Rand `8e8737e`; no pre-existing dirty work is
included. The earlier WIP checkpoint `a57f0cd` remains separate work. Rand has
30 recorded distinct workspace consumers. This material public sampler defect
was selected without changing dependency pins or entering another owner's files.

## Source and translator comparison

The recent completed bounded translation of unchanged Rand 0.9.3 `other.rs`
is reused from `build/rust-kotlin-public-open01/OtherReview.kt`. Its output,
upstream source and CLI binary hashes were confirmed unchanged against the
committed prior receipt. No duplicate translator was launched. Generated lines
113–118 retain the source's UByte range and randomRange call. The generated
unsigned range syntax is not a runnable replacement. This manual consumer
adaptation calls the existing canonical UniformInt.sampleSingle UByte overload;
there is no generator or canonical sampler implementation change.

Upstream `other.rs:156–165` samples `rng.random_range(0..52u8)` then maps the
index to ASCII uppercase/lowercase. The default upstream integer sampler uses
Canon's single-sample correction in `uniform_int.rs:174–208`, not cached-range
rejection. The Kotlin Alphabetic implementation instead used `nextU32() % 52`,
changing both byte values and RNG consumption. Only that selection expression
is repaired; existing letter mapping and append loops are unchanged.

The correction cases use first word 0x3b13b13b: widening multiply by 52 produces
high 11 and low 0xfffffffc. The low exceeds the correction threshold 0xffffffcc,
so upstream consumes a second u32. A second zero leaves index 11 ('L'); a second
maximal word has high 51 and triggers overflow, producing index 12 ('M').
These cases distinguish the exact single-sample algorithm from a cached sampler
or a substituted rejection loop. The selected canonical Kotlin sampler already
implements this contract. Its source is unchanged from the clean baseline.

## Regressions and receipts

`bash verification/run-alphabetic-oracle.sh` compiles unchanged upstream Rand
with cached rand_core 0.9.3 and runs seven actual Rust tests. The oracle uses
std+alloc and the default sampler without Rand's optional unbiased feature.
Fixed expected ASCII bytes cover endpoints, midpoints and both correction
outcomes. String cases cover a non-ASCII prefix, correction draws during
sampleString and zero-length no-draw behavior. All u32 draws are counted;
u64 or byte draws fail immediately. No downloads are performed.

Before repair, six new Kotlin cases failed and the zero-length control passed.
Final-source JVM and macOS ARM suites each pass 147 tests, including all seven
new cases, helper/public Open01, weighted and slice regressions. All seven
actual Rust oracle cases pass. Existing tests and assertions are unchanged.
Detekt has zero findings. Ktlint still fails with five findings in byte-identical
UniformOther.kt and rngs/Os.kt; none is on authored lines. Attribution uses current
Check reports, excluding old Format reports. Raw logs and the hash manifest are
in `build/rust-kotlin-alphabetic/`; the durable JSON receipt records commands,
red observations, final suite timestamps, source hashes and reused translator
provenance.

## Limits

There are no public signature changes or caller migrations. Fixed-seed byte
sequences change intentionally to match upstream; downstream builds were not run.
Kotlin's signed Byte represents these ASCII values exactly, but this does not
implement all Rust u8 APIs or prove generic numeric type parity. String capacity,
reserve_exact allocation behavior, borrowing, usize widths and other platforms
are not covered. Rand's optional unbiased feature is not implemented or verified
by this step. Existing typed-error, mutable-list borrow and generic-weight limits
remain. No statistical or security claim, publication or remote action is made.
