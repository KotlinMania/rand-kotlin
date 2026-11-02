# Full shuffle algorithm and packed RNG parity

This step begins with clean Rand 138fbd6 and its earlier consumer-rebuild caveat.
Rand has 30 recorded graph consumers. Existing checkpoints and WIP a57f0cd are
prior work. JSON f85acc0 and concurrently dirty IndexMap are untouched. No prior
dirty Rand files were included.

## Source and translator comparison

Upstream full shuffle returns for length <= 1, then uses partial_shuffle with
m=0: forward swaps from a packed IncreasingUniform chooser. All Kotlin container
lengths fit Int and are below u32::MAX, so the source fast path covers their
representable sizes. Larger Rust slices have no Kotlin container counterpart.

Bounded increasing_uniform.rs translation completed with exit 0; scratch
IncreasingReview.kt:34 retains rng.randomRange. Exact slice source/translation
establishes the forward loop. No mod.rs workaround or generator change occurred.
Canonical IncreasingUniform now generates chunks with the existing u32 randomRange
entry rather than u64 modulo. Bound calculation and factoradic/chunk state are
unchanged. Eight full-shuffle bindings share an inline forward-swap helper:
MutableList, Array, IntArray, LongArray, ByteArray, DoubleArray, ShortArray,
FloatArray. Empty/singleton inputs draw nothing. The helper carries constant
auxiliary state and creates no output lists.

Public function headers are unchanged. All four existing partialShuffle bodies,
five regular weighted-choice bodies and the entire WeightedIndex file compare
unchanged against baseline. Partial-shuffle sampling/view semantics are not claimed
fixed. Private helper extraction ports only the full-shuffle path.

## Actual Rust observations and tests

```sh
bash verification/run-shuffle-oracle.sh
./gradlew jvmTest macosArm64Test detekt ktlintCheck --continue --console=plain
```

Before Kotlin edits, a standalone main called unchanged Rand 0.9.3 public shuffle
and captured eight permutations and u32 counts. These were frozen in
shuffle-cases.json and Rust/Kotlin expected literals. The runner re-observes current
source but never rewrites expected data. Final observations match the snapshots.
It executes ten actual Rust tests using unchanged Rand and cached rand_core with
std+alloc and the default sampler. No shuffle algorithm is copied into the oracle
or replaced with a simulation; optional unbiased feature behavior is not tested.

Cases cover lengths 0/1/2, factorial chunk at 12, refill at 13, and length 25 over
multiple chunks with zero, maximal, midpoint and mixed words. Mixed 0x9e3779b9
requires five u32 draws, checking correction draws and chunk reuse. Separate raw
Float/Double tests preserve negative zero, infinities and quiet NaN payloads;
Kotlin uses toRawBits to avoid hiding payload changes. RNG failure propagates
with the tested input unchanged. Rust rejects unexpected u64/byte draws; Kotlin
counts draw widths separately.

Eight Kotlin test functions failed before repair; empty/singleton controls passed.
Multi-adapter red tests stop at List, the payload test at Float and failure test at
List counters. Final green checks all eight bindings and both payload types.
Pre-patch Native was not run. Existing assertions were not edited or relaxed.

Final-source suites pass 199 JVM and 199 macOS ARM tests, including ten new cases
and all earlier sampler/input/update/error tests. All ten actual Rust oracle tests
pass. Detekt has zero findings. Ktlint still fails with five findings in
byte-identical UniformOther.kt and rngs/Os.kt; none is on authored lines. Current
Check attribution excludes old Format reports. Raw logs, observations and final
hashes are in build/rust-kotlin-shuffle/; the committed receipt preserves commands,
coverage limits and final timestamps. Earlier fixture compile history is unchanged.
An initial metadata assertion incorrectly expected eight partialShuffle methods;
source inspection confirmed four unchanged bodies. Only the evidence count was
corrected. No source/test expectation or validation run was changed for that audit.

## Limits

No new caller migration is needed; earlier return-type changes still require
consumer rebuilds. Downstream builds, other platforms, optional unbiased behavior,
helper indices beyond Int, usize lengths above Kotlin Int, custom-container side
effects, borrowing/Drop and allocation equivalence remain unverified. Partial and
mutable-index APIs remain separate gaps. No statistical/security claim, pins,
remote CI, push, PR, merge, tag, release, publication or configuration change was
made.
