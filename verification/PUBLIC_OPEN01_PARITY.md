# Public Open01 exact sampling parity

This step starts from clean Rand `a800eac`, preserving the cached slice and
weighted samplers and the Float helper correction. There was no pre-existing
dirty work to checkpoint. The earlier WIP checkpoint `a57f0cd` is separate work.
Rand serves 30 distinct workspace consumers in the dependency graph. Repairing
its public distribution closes a material path missed by the previous helper
repair, without entering another owner's files or a blocked representation design.

## Provenance and repair

The bounded rust-kotlin translation of `tmp/rand/src/distr/other.rs`, matching
the marker in Other.kt, completed with exit 0 and is preserved in
`build/rust-kotlin-public-open01/OtherReview.kt`. It contains Alphanumeric and
Alphabetic but no Open01: upstream owns Open01 in `distr/float.rs` instead.
The existing completed Float scratch from the f98d2c1 step was reused rather
than launching a duplicate translation. It contains declarations and tests but
omits the macro-generated implementation. This consumer repair follows the
exact unchanged float.rs macro at lines 141–151 through the existing canonical
UniformFloatOpen01. No generated file was installed and no generator was changed.

The public Open01 in Other.kt previously ORed the retained fraction with 1
and divided by 2^23 or 2^52. Upstream constructs a float with exponent zero,
then subtracts `1 - EPSILON/2`. The formulas have different output bits:
for a zero word, the public Float result was 2^-23 instead of 2^-24, and the
Double result was 2^-52 instead of 2^-53. Existing tests only checked `(0,1)`
bounds, which both formulas satisfied.

Both public methods now delegate to UniformFloatOpen01, whose Float conversion
was fixed and checked in f98d2c1. This removes the independent formula while
retaining exact upstream output and one draw of the correct word width.
Public signatures, old tests, OpenClosed01, other distributions and dependency
pins are unchanged. No caller migration is required; downstream builds were not run.

## Evidence

PublicOpen01ParityTest checks fixed expected bits for 17 word/type pairs across
six tests: zero/maximal words, the first fraction step, discarded low bits and
midpoints. It asserts one u32 for Float, one u64 for Double and rejects byte
draws. Double tests use `rng.sample(Open01)` to exercise the public Distribution
entry. All six tests failed before repair.

`bash verification/run-public-open01-oracle.sh` recompiles unchanged Rand 0.9.3
and cached rand_core 0.9.3 in isolated build output, then executes the existing
unchanged `verification/open01-oracle.rs`. All six actual Rust oracle cases pass;
this step does not alter their expected bits or producer behavior. Final-source
Kotlin suites pass 140 JVM and 140 macOS ARM tests. They include all six new tests,
the six helper controls, six weighted tests and seven slice tests. Existing
bounds-only assertions were preserved rather than replaced or relaxed.

Detekt reports zero findings. Ktlint fails with five findings in byte-identical
UniformOther.kt and rngs/Os.kt; none is on authored lines. Attribution uses current
Check XML and excludes old Format reports. The durable JSON receipt contains
red observations, exact commands, suite timestamps and source/scratch hashes.
Raw logs and the final hash manifest are in `build/rust-kotlin-public-open01/`.

## Limits

Missing macro-generated code in translator output remains unresolved. SIMD,
other targets, downstream compilation and statistical or cryptographic quality
are unverified. Existing typed-error, borrowed-list, generic-weight and usize
limitations from the other distributions remain. This step does not redesign
the IntoFloat interface or claim complete Rand parity. No remote workflows,
pushes, releases, publications or configuration changes were performed.
