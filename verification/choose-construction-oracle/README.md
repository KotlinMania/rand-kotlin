# Choose typed construction contract

## Exact source and API repair

`tmp/rand/src/distr/slice.rs` defines `Choose::new(&[T]) -> Result<Choose<T>, Empty>`, rejects empty input before constructing its cached UniformUsize, and gives Empty the exact Display string ``Tried to create a `rand::distr::slice::Choose` with an empty slice``. Rust's fields are private. Kotlin instead exposed a public constructor guarded by require plus throwing fromList/fromArray factories, with no new/Empty source APIs.

This bounded manual repair makes the constructor private, restores Result-returning new overloads for existing List/Array representations, and adds the source-owned Empty class/Display in the existing configured distr package. It reuses canonical distr.Result and UniformUsize; no duplicate shared symbol, compatibility factory, alternate sampling algorithm or generic error helper is added. The existing range.sample selection, numChoices property, array-to-list adaptation and generic payload bounds are unchanged.

## Evidence before changing the historical test

The earlier completed SliceReview.kt translation is reused only after verifying original source and emitted output hashes against verification/slice-choose-receipt.json. Original invocation/binary hash and the whole prior receipt are copied into the new evidence folder. The emitted new/Empty declarations expose this omission. No duplicate CLI, generator investigation/edit/rebuild or source-guard bypass occurred; this is recorded-binary structural evidence, not current-build attestation or wholesale adoption.

Run `cargo test --offline --manifest-path verification/choose-construction-oracle/Cargo.toml --target-dir build/rust-kotlin-choose-construction/rust-target` against actual upstream Choose. Its first seven sampling tests are copied from the existing oracle, with assertions/inputs unchanged. Two added groups prove the exact Result<Choose,Empty>/Display and that an uncloneable payload is sampled by reference with one u32/no u64 draw. All nine groups passed before editing any production or existing test. Kotlin then reproduced six missing new/Empty compilation diagnostics; no Kotlin tests executed in that red compile. New regression bytes remain unchanged after it.

The existing emptyIsRejected Kotlin test expected IllegalArgumentException, while the earlier Rust empty_is_rejected test only asserted is_err. The prior seven-case pass therefore established rejection, not equivalent typed-return behavior; it must not be retrospectively presented as full constructor parity. The fresh Rust type/Display evidence confirms that the historical Kotlin characterization was wrong. That one test now asserts Result.Err<Empty> and the exact Display. It is not deleted, disabled or replaced with a generic success/no-throw check. Original source/receipt and the correction mapping are retained.

The six existing nonempty sampling cases migrate only from fromList/fromArray to new(...).unwrap(). Their cached-range behavior, words, expected elements (including null), numChoices and exact u32/u64 draw assertions are unchanged. The helper bodies/assertions remain intact. New tests additionally cover typed errors for both empty List and Array plus a payload with no clone capability and reference identity; they do not impose an unsupported T:Clone bound. Final full JVM/macOS ARM/static receipts, original sources and independent report copies are recorded against final source.

## Caller impact, ownership and limits

A bounded read-only scan covered 1,511 existing Kotlin/Java/Swift source roots including ignored/untracked source, excluding build/tmp/target/caches. Exact Choose names, import aliases via original import names, distr wildcard imports and same-package users were reviewed. The only exact existing consumers are six valid and one empty-construction calls in SliceChooseParityTest; no production consumer exists outside its own factory. External matches are comments or LALRPOP SymbolKind/PatternKind variants, not this imported Rand distribution. No affected external owner was found or edited; no downstream build was run.

fromList/fromArray are removed and direct construction becomes private. Consumers must use Choose.new and handle Result (unwrap for known valid inputs or match Empty), then rebuild. The source review does not establish binary/downstream compatibility. No original sampling assertion is relaxed to preserve these old helper names.

Baseline 050c25b was clean; no prior dirty work or dependency pin/configuration/concurrent-owner changes are included. The preceding Bernoulli Result/rebuild caveat and Futures c00aae5 remain separate. Rand has 30 reverse-dependent libraries.

Remaining differences include Rust borrowed-slice/exclusive ownership versus Kotlin List aliasing and array snapshot adaptation, NonZeroUsize versus Int counts, full usize/platform ranges, Clone/Copy/Debug/std::error::Error trait mapping, character SampleString support and Drop/GC. Mutable alias changes are not source-equivalent and are not validated here. No complete Choose/source port or downstream compatibility claim is made. Runtime passes do not imply a clean global ktlint gate; all remote/configuration/agent-contact holds remain respected.
