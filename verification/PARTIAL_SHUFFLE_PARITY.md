# Partial-shuffle mutation and RNG parity

This bounded step begins with clean Rand 3054492. Existing checkpoints and WIP
a57f0cd are prior work; no dirty Rand files were included. Rand has 30 recorded
graph consumers. JSON f85acc0 and concurrently dirty IndexMap remain untouched.
Earlier return-type changes still require consumer rebuilds.

## Independent source comparison

Upstream partial_shuffle directly computes m = len.saturating_sub(amount), starts
IncreasingUniform at m, swaps m..len and returns the selected suffix before the
remaining prefix. Exact source, completed bounded slice translation and CLI hashes
match the committed earlier receipt. This repair follows that partial algorithm,
not full shuffle followed by slicing. No duplicate CLI, mod.rs workaround, copied
algorithm in an oracle, or generator change was used.

Four existing bindings (MutableList, Array, IntArray, LongArray) now use a private
start-index swap core with the already corrected canonical IncreasingUniform.
The existing full-shuffle private helper delegates to that core at zero while
retaining its <=1 early return. Eight full-shuffle public bodies, public headers,
IncreasingUniform and WeightedIndex compare unchanged to baseline. Partial return
copy blocks also compare unchanged; source borrowing is not fixed by this step.

## Actual Rust snapshots and checks

```sh
bash verification/run-partial-shuffle-oracle.sh
./gradlew jvmTest macosArm64Test detekt ktlintCheck --continue --console=plain
```

A standalone main called unchanged Rand 0.9.3 public partial_shuffle before Kotlin
edits. Thirteen observations were frozen in partial-shuffle-cases.json and in
Rust/Kotlin expected literals. The runner re-observes current source but does not
rewrite expected data. Final observations match every frozen field. Rust's real
returned views are copied in the oracle only to compare values; this is not an
aliasing-equivalence test. Thirteen fixed cases plus one raw-payload test yield
fourteen actual Rust tests, compiled with cached rand_core and std+alloc/default
sampling. No optional unbiased feature behavior is asserted.

Cases cover empty/singleton input, zero amount, partial counts, nonzero/high start,
chunk crossings, oversized and Int.MAX_VALUE amounts, and RNG failure at the first
draw or later refill. Selecting three of 25 items starts at 22 and takes one u32
draw; the thirteen-of-25 mixed-word case takes four. These snapshots differ from
full shuffle and expose a wrong zero-start implementation. The later failure
case records the already-mutated prefix before the next refill throws. Source
panic observation uses AssertUnwindSafe; it is not a recovery/transaction promise.

Raw Float/Double generic-array cases compare mutated input, selected and remainder
bits, including negative zero, infinities and quiet NaN payloads via toRawBits.
RNG draw widths/counts and exception identity are checked. Returned order is
selected suffix first, remaining prefix second.

Before repair eleven Kotlin test functions failed; empty, singleton-noop and
zero-amount controls passed. Red adapter loops stop at List and raw-payload checks
stop at Float counters; final green checks all four bindings and both payload
kinds. No independent red-per-adapter claim is made. Pre-patch Native was not run.
Existing assertions and expected data were not relaxed or changed. Fixture calls
were laid out for formatting before final-source checks.

Final-source suites pass 213 JVM and 213 macOS ARM tests, including all fourteen
new cases and the preserved full-shuffle/sampling/input/update/error tests.
All fourteen actual Rust oracle tests pass. Detekt has zero findings. Ktlint still
fails with five findings in byte-identical UniformOther.kt and rngs/Os.kt; none is
on authored code. Current Check attribution excludes historical Format reports.
Earlier fixture compile-history hashes are unchanged. Raw logs, source hashes and
observations are in build/rust-kotlin-partial-shuffle/; the committed receipt
preserves commands, coverage limits and actual final timestamps.

## Explicit remaining representation gap

Kotlin returns Pair<List<...>, List<...>> snapshots. Rust returns live mutable
borrowed slices into the input. The existing copy code and this difference remain.
Sampling/swaps take the source O(amount) path, but Kotlin's output copies can still
cost O(n) time/space. This is mutation, sampling and returned-value snapshot parity,
not full partial_shuffle API/ownership equivalence. No new view type, aliasing
substitute or public signature change was introduced.

Only nonnegative representable Int amounts/container lengths are covered; negative
Kotlin amounts have no usize counterpart. Rust larger-slice paths, usize beyond
Int, custom container side effects, Drop/borrow enforcement, allocation-failure
behavior, other platforms and downstream builds remain unverified. No new caller
migration is needed by this step; older consumer-rebuild caveats remain. No pins,
remote CI, push, PR, merge, tag, release, publication or configuration changes were
made.
