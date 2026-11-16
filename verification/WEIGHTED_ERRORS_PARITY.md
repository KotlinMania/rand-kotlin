# Double-weight construction error parity

This step starts from clean Rand `46e4736`. The earlier WIP checkpoint a57f0cd
and existing sampler work are prior checkpoints, not new coverage attributed
to this repair. JSON f85acc0 and the concurrently dirty IndexMap checkout were
not modified. Graph priority remains Rand 30 distinct consumers, JSON 21; inspected
JSON numeric scanners already use ASCII ranges. This confirmed Double-weight
error contract closes a previously documented construction limitation.

## Exact source contract

The completed bounded weighted_index.rs translation from the earlier sampling
step was reused after confirming that its output, upstream input and current
CLI hashes match the committed receipt. No duplicate CLI job or mod.rs workaround
was launched. Its checkedAddAssign call must be interpreted using the exact
upstream weighted/mod.rs Float Weight implementation: it adds and always returns
Ok because infinity represents floating-point overflow. Integer Weight separately
checks overflow. Kotlin's existing canonical FloatWeight already documents and
implements this same floating-point contract.

WeightedIndex<Double> incorrectly threw WeightError.Overflow immediately on an
infinite total. This both changed panic-versus-Result behavior and hid later
invalid weights. Upstream continues validating weights, so [1, infinity, -1]
and [MAX, MAX, NaN] return InvalidWeight. When all weights pass validation but
the total is infinite, UniformFloat construction/unwrap panics. The repair removes
one premature overflow check; existing validation, accumulation, canonical
sampler initialization, error types and sampling algorithm remain unchanged.
The public Double constructor comment now states the observed nonfinite-total
behavior. No signature changed and no caller migration was required.

## Actual oracle and red/green receipts

Run from the repository root:

```sh
bash verification/run-weighted-errors-oracle.sh
./gradlew jvmTest macosArm64Test detekt ktlintCheck --continue --console=plain
```

The standalone oracle compiles unchanged Rand 0.9.3 with cached rand_core 0.9.3
and std+alloc. Its initial compilation failed with E0283 because an empty array
left the IntoIterator item ambiguous. Giving the empty iterator an explicit f64
type corrected only the fixture typing; assertions and production sources were
unchanged. The initial compiler log is retained and hashed in the receipt.
The subsequent seven actual Rust tests all pass.

Cases cover finite overflow, explicit positive infinity, invalid weights after
each infinite-total path, negative infinity, empty/all-zero errors and large
finite weights. The finite sample checks exact selected index and one u64 draw,
rejecting u32/byte draws. Rust catch_unwind observes sampler panic; Kotlin tests
observe the existing canonical unwrap's IllegalStateException. This validates
panic-versus-typed-error behavior, not identical panic payloads or backtraces.

Before repair four Kotlin test functions failed; three controls passed. Panic
helpers stop at their first create-array failure in the red run, so no claim is
made of four independent wrapper failures per case. Final green tests execute
create/new array/list paths. Final-source suites pass 161 JVM and 161 macOS ARM
tests, including all seven new cases and all previous sampling regressions.
Detekt has zero findings. Ktlint still fails with five findings in byte-identical
UniformOther.kt and rngs/Os.kt; none is on authored code. Attribution uses current
Check reports and excludes old Format reports. Existing assertions were not
changed. Raw logs, final-source hashes and attribution are in
build/rust-kotlin-weighted-errors/; weighted-errors-receipt.json preserves the
commands, red observations, fixture correction and final suite timestamps.

## Limits

This fixes Double factory construction, not Rust's generic Weight representation.
Existing integer/Float convenience conversions still pass through Double and do
not implement native-width integer overflow or full f32 arithmetic. Update-weight
contracts outside previously tested cases, mutable borrowing, ownership, usize,
other targets and downstream compatibility remain unverified. The shared public
Overflow type and canonical sampler/FloatWeight implementations are unchanged.
Project pins are unchanged. No statistical/security claim, remote CI, push, PR,
merge, tag, publication or configuration action was performed.
