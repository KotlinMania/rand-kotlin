# Slice Choose cached sampler parity

This repair starts from clean Rand `f98d2c1`. No prior dirty changes were
included. Earlier WIP checkpoint `a57f0cd` is separate work. Rand has 30 distinct
workspace consumers in the dependency graph; this source-level distribution
defect was selected ahead of adding diagnostics or redesigning blocked APIs.
Serde `3b93c15`, Futures `09c4126`, and other owners were not modified.

## Source and generated output

A bounded CLI translation of unchanged Rand 0.9.3 `distr/slice.rs` completed
with exit 0. `build/rust-kotlin-slice-choose/SliceReview.kt` retains the cached
UniformUsize range (line 54), sampling through that range (line 65), and its
initialization (line 108). The generated namespace, NonZeroUsize and Rust helper
calls are not a compilable replacement. This manual consumer adaptation uses
the already owned Kotlin UniformUsize; it does not add a duplicate sampler or
fix the generator. The receipt includes the exact CLI command and hashes.

Upstream `Choose::new` creates `UniformUsize::new(0, len)` once, and its
Distribution implementation calls `range.sample`. `uniform_int.rs` implements
the small-range path with a u32 widening multiply and rejection threshold,
including one draw for a singleton. The previous Kotlin `nextU64() % size`
changed both selections and RNG consumption. For a three-element slice and
script `[0, u32::MAX]`, Rust rejects zero and then returns index 2, whereas the
old Kotlin code returned index 0 from its first u64 draw.

The repair adds the same cached range and delegates selection to it. Public
signatures, factories, element storage and existing assertions are unchanged.
No exact-API caller migration is required; downstream builds were not run.
There is no modulo replacement, alternate distribution, or singleton shortcut.

## Regressions and final-source validation

Run `bash verification/run-slice-choose-oracle.sh` to compile unchanged upstream
Rand with cached rand_core 0.9.3 and run seven real Rust oracle cases. The Kotlin
suite mirrors singleton draw width, power-of-two high-bit selections, a rejected
word followed by an accepted word, repeated sampling, a selected null payload,
array/list selection and empty-input rejection. Rust's corresponding payload
case uses Option::None. This is a selected-value control, not a claim that Kotlin
null implements every Rust Option API. Exact chosen values and u32/u64 counts are
asserted, and unexpected byte draws fail. No downloads or configuration changes
are used.

Before repair, six Kotlin tests failed; empty-input rejection passed. After
repair, final-source JVM and macOS ARM suites each pass 134 tests, including
all seven new cases and all six tests in each of the preserved Open01 and
WeightedSamplingParity suites. All seven actual Rust oracle tests pass.
Detekt has zero findings. Ktlint still fails with five findings in byte-identical
UniformOther.kt and rngs/Os.kt; none is on authored lines. Old Format reports
are excluded from attribution. Raw red/final logs, source hash checks and static
attribution are in `build/rust-kotlin-slice-choose/`; the durable JSON receipt
records suite timestamps, hashes and failure observations.

## Remaining limits

Kotlin still uses an exception for empty input instead of the source's typed
Empty Result, and Int instead of NonZeroUsize for the count. Kotlin's List type
does not enforce a Rust immutable slice borrow: resizing an underlying mutable
list while this distribution exists violates the source's fixed-length invariant
and is not covered. The existing fromArray factory copies the array to a list;
this step does not claim equivalent borrowing, allocation or lifetime behavior.
The cached sampler covers representable Kotlin list sizes. Larger usize ranges,
SampleString/char support, other platforms, statistical quality and downstream
compatibility are not verified here. No dependency pins, remote workflows,
pushes, releases or publications were changed.
