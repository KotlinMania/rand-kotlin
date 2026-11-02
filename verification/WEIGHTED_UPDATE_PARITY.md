# WeightedIndex typed update results

This step begins with clean Rand f6d4511. Existing sampler/error fixes and WIP
a57f0cd are separate prior checkpoints. Rand has 30 graph consumers; JSON f85acc0
and the concurrently dirty IndexMap checkout are untouched. No prior dirty Rand
files were included. This is a consumer API/validation correction, not a generator
or whole-project resolver change.

## Source contract and repair

Upstream weighted_index.rs update_weights returns Result<(), Error>. The existing
bounded generated output retains result-oriented control flow and final success,
but its Result<Unit> shape lacks the explicit error parameter. The exact source
and completed output/CLI hashes were previously verified unchanged, so no duplicate
translation or mod.rs workaround was used. The consumer now uses the already owned
canonical Result<Unit, WeightError>; no new result or error type is introduced.

The primary Kotlin method previously returned Unit and threw recoverable validation
errors. It now returns Ok(Unit) or typed Err, preserving source validation order:
sorted indices before weight validation, then bounds, then total. Recoverable
errors leave weights, total and cached sampler unchanged. Validated updates are
applied before the sampler is rebuilt; sampler initialization failures still throw.
The existing updateWeightsResult wrapper directly forwards the primary result,
so it cannot turn an Err into Ok. No new compatibility overload was added.

The total check now uses the source's exact <= zero predicate. A NaN total after
a caught prior sampler panic therefore reaches sampler failure rather than a
fabricated InsufficientNonZero result. The Rust fixture explicitly uses
AssertUnwindSafe to observe this safe-source execution. This regression records
actual algorithm behavior, not a general guarantee of recovery after panic.

## Actual Rust and Kotlin evidence

```sh
bash verification/run-weighted-update-oracle.sh
./gradlew jvmTest macosArm64Test detekt ktlintCheck --continue --console=plain
```

Nine actual Rust oracle tests pass against unchanged Rand 0.9.3 and cached
rand_core 0.9.3. They cover empty/successful results, invalid weight, invalid
weight versus bounds, duplicate index versus invalid weight, valid out-of-bounds
weight, zero total, nonfinite sampler panic and NaN after a caught prior panic.
Recoverable failures assert unchanged state and a subsequent exact selection
with one u64 draw; unexpected u32/byte draws fail.

Eight Kotlin tests failed before repair; the pre-existing sampler-panic control
passed. Red rejected tests stop at the primary API, so wrapper/state checks were
not all reached in red. Final green tests execute both primary and existing wrapper
paths and the state/cache checks. Final-source suites pass 170 JVM and 170 macOS
ARM tests, including all nine new cases and all previous sampler/error suites.
Detekt has zero findings. Ktlint still fails with five findings in byte-identical
UniformOther.kt and rngs/Os.kt; no findings are on authored lines. Attribution
uses current Check reports, excluding old Format reports. Raw logs and final
source hashes are in build/rust-kotlin-weighted-update/; the committed receipt
records actual timestamps and counts. The previous fixture's E0283 compile-history
receipt/log hash was verified unchanged; this step's Rust fixture passed initially.

## Caller review and assertion preservation

weighted-update-callers.json records pre-change and final workspace searches.
Final Kotlin/Java/Swift source review found only local calls: two tests plus the
existing result wrapper, and the new regressions. All existing local callers are
adapted. Every old assertEquals/assertTrue line in the two adapted tests matches
the baseline. The old thrown InvalidWeight assertion becomes an assertion of the
same concrete InvalidWeight inside Err; all weights, total, selection and RNG
checks remain. Successful updates gain explicit Ok assertions. Existing constructor
exception checks are unchanged. No unrelated owner's source was edited.

The public return changes from Unit to Result, including the JVM method descriptor;
compiled consumers require a rebuild. No external source calls were found within
the search scope, but ignored/generated artifacts, binaries and downstream builds
were not audited. This is source-level impact evidence, not downstream compatibility.

## Remaining limits

Generic weight traits, integer/Float convenience conversion precision, usize widths,
borrow/ownership guarantees, panic payload equivalence, other platforms and full
panic recovery remain unverified. Negative Kotlin indices have no usize counterpart.
Project pins and canonical sampler/error implementations are unchanged. No remote
CI, push, PR, merge, tag, release, publication or configuration change was performed.
