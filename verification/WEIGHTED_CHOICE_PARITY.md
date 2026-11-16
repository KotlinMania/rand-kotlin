# Weighted collection choice delegation

This step starts from clean Rand 1141f50, with 30 recorded graph consumers.
Earlier sampler/error/input/update checkpoints and WIP a57f0cd are prior work,
not new coverage attributed here. JSON f85acc0 and concurrently dirty IndexMap
are untouched. The earlier consumer-rebuild caveat remains, and the five public
weighted-choice return changes in this step independently require a rebuild.

## Exact source and tool comparison

Upstream seq/slice.rs:162–176 constructs WeightedIndex from a lazy mapped index
range, propagates its typed error, samples once and returns the selected element.
The completed bounded SliceReview.kt output/input/CLI hashes match the committed
indexed-choose receipt. Generated lines 148–150 retain WeightedIndex delegation
and result flow, but Kotlin IntRange.map materializes eagerly and Result<Output>
lacks the explicit error parameter. No duplicate translation, installed generated
file, mod.rs workaround or generator change was made.

All five existing regular chooseWeighted bindings (List, Array, IntArray,
LongArray, DoubleArray) now return the existing canonical Result<T, WeightError>
and delegate through WeightedIndex. indices.asSequence().map preserves Rust's
lazy iterator mapping, so callback evaluation stops on the first invalid weight.
A forwarding public WeightedIndex.new(Iterator<Double>) binding exposes its already
tested, unchanged private incremental constructor. Its old array/list bindings
and the typed update algorithm are unchanged. No validation or sampling algorithm
was duplicated or substituted. The old independent roll and nullable-result paths
are removed. Accepted null payload is now Ok(null), distinct from empty-input Err.

## Actual oracle and red/green evidence

```sh
bash verification/run-weighted-choice-oracle.sh
./gradlew jvmTest macosArm64Test detekt ktlintCheck --continue --console=plain
```

Ten actual Rust tests pass against unchanged Rand 0.9.3 with cached rand_core
0.9.3, std+alloc and f64 weights. Index payloads and an Option payload exercise
exact selections from specified u64 words, zero-weight boundaries, empty input,
invalid-prefix callback stopping, all-zero errors, accepted None, infinity followed
by invalid weight, nonfinite sampler panic before RNG use, callback panic carrying
WeightError and RNG panic. The Rust source runs directly; no simulated distribution
is used. Callback reads and u64 counts are asserted; u32/byte draws fail.

Kotlin final tests cover all five adapters using representable index-valued
payloads, plus nullable List/Array payloads. Provider and RNG exceptions retain
identity and are not converted into validation Err. Before repair eight test
functions failed; callback/RNG exception controls passed. Multi-adapter red tests
stop on the List failure, and some stop at result-type checks before payload checks;
this does not establish five independent red runs. Final green tests execute all
five adapters. Pre-patch Native was not run.

Final-source suites pass 189 JVM and 189 macOS ARM tests, including all ten new
cases and all earlier input/update/error/sampling suites. Detekt has zero findings.
Ktlint still fails with five findings in byte-identical UniformOther.kt and rngs/Os.kt;
none is on authored code. First-check 189/189 runtime passes and new fixture
formatting diagnostics are retained; lambdas were expanded without assertion changes
and all final checks rerun. Current Check attribution excludes old Format reports.
Raw logs and final-source hashes are in build/rust-kotlin-weighted-choice/; the
receipt preserves commands, red limitations, source hashes and final timestamps.

## Caller review and limits

weighted-choice-callers.json records pre-change and final source searches across
Kotlin, Java, Swift, JS and TS. Only local declarations, new regressions and one
existing test caller were found. That caller now unwraps success; its 1000-sample
loop and commonCount > 900 threshold are unchanged. Every pre-existing assertEquals
and assertTrue line in SliceTest matches baseline. No other owner's code was edited.
Five return types and JVM descriptors change from nullable element to Result, so
compiled consumers require rebuilding. Ignored/generated artifacts, binaries and
downstream builds were not audited; no downstream compatibility is established.

This covers existing Double-weight callbacks, not generic native integer/Float
weight arithmetic or borrowed-weight traits. Primitive adapters copy selected
values rather than enforcing Rust reference lifetimes. Concurrent collection
mutation, borrowing, capacity/size_hint allocation equivalence, other platforms
and full panic payload equivalence remain unverified. Mutable-index weighted and
multiple-weighted helpers are unchanged and not claimed equivalent. The ordinary
unweighted nullable choose API still has its separate presence limitation. Earlier
fixture compile-history evidence is preserved. No pins, remote CI, push, PR, merge,
tag, release, publication or configuration changes were made.
