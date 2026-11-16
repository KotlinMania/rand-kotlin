# Incremental weighted input validation

This bounded step starts from clean Rand f009a4a. Earlier sampler/error/update
checkpoints and WIP a57f0cd are prior work; no pre-existing dirty Rand files were
included. Rand has 30 recorded graph consumers. JSON f85acc0 and the concurrently
dirty IndexMap checkout are untouched. No public signature changes in this step;
the earlier f009a4a consumer-rebuild requirement still applies.

## Source comparison and repair

The exact upstream weighted_index.rs constructor and completed bounded generated
output/CLI hashes match the earlier weighted-sampling receipt. Their constructors
consume one weight, validate it, then continue the iterator, returning at the first
invalid weight. No duplicate translation or mod.rs workaround was used. This is
a manual consumer correction, not a generator fix.

Kotlin new(List) and create(List) previously copied the entire input list to a
DoubleArray before validation. A computed or failing suffix could therefore throw
even when an earlier weight should already have returned InvalidWeight. The
factories now use one private incremental iterator constructor. It returns the
existing canonical Result for validation errors, accumulates the same cumulative
weights in source order, and initializes the same canonical UniformFloat sampler.
Array/list new bindings delegate directly. Existing throwing create bindings throw
only explicit validation Err values; they do not catch input failures.

This last distinction matters when an element provider throws WeightError itself.
It must remain an input failure, rather than being mistaken for a validation Err.
No catch around the scan is used. No alternative sampling/accumulation algorithm,
public iterator API, duplicate result type or error type was introduced. The
nonfinite-total and typed-update behavior established by previous commits remains.

## Actual red/green evidence

```sh
bash verification/run-weighted-input-oracle.sh
./gradlew jvmTest macosArm64Test detekt ktlintCheck --continue --console=plain
```

Nine actual Rust tests pass against unchanged Rand 0.9.3 and cached rand_core
0.9.3. A real Iterator probe passed by mutable reference records successful and
failing element requests. Cases cover first/later negative values, NaN, infinity
followed by an invalid value, valid full input, empty input, final sampler panic,
ordinary provider failure and a provider panic carrying the weight-error enum.
The latter is observed through catch_unwind/panic_any, distinct from Result::Err.
Kotlin checks the same reads and outcomes; provider exceptions retain identity.
Valid construction checks cumulative-derived weights, total and exact selected
index with one u64 draw, rejecting u32/byte draws.

The first eight-case logs are retained. A ninth source-backed provider-error
control was added before production edits to prevent an overbroad exception catch;
no earlier assertion changed. All nine Rust cases pass. Four Kotlin test functions
failed before repair because new(List) requested a failing suffix; five controls
passed. Red failures stop before reaching create(List), so no separate red claim
is made for that path. Final green tests exercise both factory paths. Pre-patch
Native was not run.

Final-source suites pass 179 JVM and 179 macOS ARM tests, including all nine new
cases and preserved update/error/sampling suites. Detekt has zero findings. Ktlint
still fails with five findings in byte-identical UniformOther.kt and rngs/Os.kt;
none is on authored lines. The first final check found one trailing blank line in
the new fixture; it was corrected and both runtime suites/static checks were rerun.
Current Check reports exclude historical Format reports from attribution.
Existing assertions were not edited. The earlier E0283 fixture compile-history
receipt/log hash was verified unchanged. Raw evidence and final-source hashes are
in build/rust-kotlin-weighted-input/; the committed receipt records commands,
observations, chronology and actual final suite timestamps.

## Limits

Public headers and the typed update algorithm match f009a4a; no caller migration
is needed for this step. Previous source-level caller review remains limited and
downstream binaries still require rebuilding for that earlier return-type change.
General Rust IntoIterator, size_hint/preallocation, allocation-failure timing,
integer/Float trait precision, ownership/borrow enforcement and concurrent input
mutation are not modeled or verified. The cumulative builder may allocate more
than the old pre-sized DoubleArray path. Other targets and downstream builds are
unverified. No pins, canonical sampler implementation, remote CI, push, PR, merge,
tag, release, publication or configuration changes were made.
