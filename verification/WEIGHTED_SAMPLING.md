# WeightedIndex Double sampling parity

This repair starts from a clean `99e894c` Rand tree. No pre-existing dirty work
was included. The earlier 87-file WIP checkpoint `a57f0cd` is separate work.
Serde `3b93c15` and Futures `09c4126` were left unchanged. The dependency graph
shows Rand serving 30 distinct workspace consumers. Serde OsString remains
blocked on reconciling its portable tagged value with upstream platform-specific
variant rejection; this step does not alter its human-readable parsers.

The bounded rust-kotlin CLI translation of upstream Rand 0.9.3
`distr/weighted/weighted_index.rs` is in
`build/rust-kotlin-weighted-sampling/WeightedIndexReview.kt`. It retains the
cached sampler at construction, refresh after successful updates, and sampler
use when choosing the partition point (generated lines 99, 190, 254, 298).
Its unresolved generic associated sampler types are not a runnable replacement.
This is a manual consumer adaptation of that output and exact Rust source,
not a generator fix. The CLI binary and generated output hashes are recorded
in `weighted-sampling-receipt.json`.

Upstream source lines 128–135 initialize `X::Sampler`, line 236 refreshes it,
and lines 362–366 sample it before partitioning. For `f64`,
`uniform_float.rs:67–84,140–153` uses high 52 RNG bits, then multiply/add.
The previous Kotlin full-u64 normalization consumed one word but could change
which index was selected: `[1, 0]` with `u64::MAX` returned index 1 instead of 0.
The repair reuses the existing canonical `UniformFloat<Double>` and refreshes
its cached range on updates. Existing cumulative arithmetic, binary search,
public signatures, dependency pins and old test assertions are unchanged.
There are no local direct constructor callers beyond `create`; no signature
migration or downstream build campaign was performed.

## Evidence

Run `bash verification/run-weighted-sampling-oracle.sh` for six tests against
unchanged Rust sources, compiled with the already cached rand_core 0.9.3.
No downloads, repository changes or publication are involved. The corresponding
`WeightedSamplingParityTest` checks exact choices and u64 draw counts; u32 or
byte draws fail immediately. Cases cover maximal words, discarded low bits,
zero-weight boundaries, successful cache refresh, rejected-update preservation,
and actual subnormal rounding. The latter deliberately expects index 1 for
`[f64::from_bits(1), 0]` with the maximal word: upstream rounds to the total here.
No endpoint clamp or alternate sampling algorithm was introduced.

Before the production fix, four of six Kotlin cases failed. The cache-update
and subnormal controls passed. After the fix, final-source JVM and macOS ARM
suites each pass 121 tests, including all six new cases. All six actual Rust
oracle tests pass. Detekt has zero findings. Ktlint still fails with five
findings in byte-identical `UniformOther.kt` and `rngs/Os.kt`; none is on authored
code. The first check found two class-signature formatting issues in the new
fixture; those were fixed, and both complete runtime suites and static checks
were rerun on the final Kotlin source. Check receipts exclude old Format reports.
Raw logs and hash checks are in `build/rust-kotlin-weighted-sampling/`;
the durable receipt records final suite timestamps, hashes, red cases and counts.

## Limits

Coverage is the existing Double representation with finite positive totals;
this does not implement Rust's generic weight or integer sampler types. Existing
integer convenience conversions still lose precision through Double. Existing
constructor/overflow error differences remain: the Kotlin public raw cumulative
constructor has no upstream public equivalent, and now creates the same checked
canonical sampler as `create`. Invalid/nonfinite raw totals may therefore fail
at initialization; arbitrary invalid raw state is not promised compatible.
The source's floating-point update failure/panic behavior beyond tested cases,
borrowing/clone/serialization equivalence, usize widths, other targets and
downstream consumers remain unverified. No statistical or security claim is made.
