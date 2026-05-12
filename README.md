# rand-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Frand--kotlin-blue.svg)](https://github.com/KotlinMania/rand-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/rand-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/rand-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/rand-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/rand-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`rust-random/rand`](https://github.com/rust-random/rand).

**Original Project:** This port is based on [`rust-random/rand`](https://github.com/rust-random/rand). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## Upstream README — `rust-random/rand`

> The text below is reproduced and lightly edited from [`https://github.com/rust-random/rand`](https://github.com/rust-random/rand). It is the upstream project's own description and remains under the upstream authors' authorship; links have been rewritten to absolute upstream URLs so they continue to resolve from this repository.

## Rand

[![Test Status](https://github.com/rust-random/rand/actions/workflows/test.yml/badge.svg?event=push)](https://github.com/rust-random/rand/actions)
[![Crate](https://img.shields.io/crates/v/rand.svg)](https://crates.io/crates/rand)
[![Book](https://img.shields.io/badge/book-master-yellow.svg)](https://rust-random.github.io/book/)
[![API](https://docs.rs/rand/badge.svg)](https://docs.rs/rand)

Rand is a set of crates supporting (pseudo-)random generators:

-   Built over a standard RNG trait: [`rand_core::RngCore`](https://docs.rs/rand_core/latest/rand_core/trait.RngCore.html)
-   With fast implementations of both [strong](https://rust-random.github.io/book/guide-rngs.html#cryptographically-secure-pseudo-random-number-generators-csprngs) and
    [small](https://rust-random.github.io/book/guide-rngs.html#basic-pseudo-random-number-generators-prngs) generators: [`rand::rngs`](https://docs.rs/rand/latest/rand/rngs/index.html), and more RNGs: [`chacha20`](https://docs.rs/chacha20), [`rand_xoshiro`](https://docs.rs/rand_xoshiro/), [`rand_pcg`](https://docs.rs/rand_pcg/), [`rand_sfc`](https://docs.rs/rand_sfc/), [`rand_seeder`](https://docs.rs/rand_seeder/), [rngs repo](https://github.com/rust-random/rngs/)
-   [`rand::rng`](https://docs.rs/rand/latest/rand/fn.rng.html) is an asymptotically-fast, automatically-seeded and reasonably strong generator available on all `std` targets
-   Direct support for seeding generators from the [getrandom] crate

With broad support for random value generation and random processes:

-   [`StandardUniform`](https://docs.rs/rand/latest/rand/distr/struct.StandardUniform.html) random value sampling,
    [`Uniform`](https://docs.rs/rand/latest/rand/distr/struct.Uniform.html)-ranged value sampling
    and [more](https://docs.rs/rand/latest/rand/distr/index.html)
-   Samplers for a large number of non-uniform random number distributions via our own
    [`rand_distr`](https://docs.rs/rand_distr) and via
    the [`statrs`](https://docs.rs/statrs)
-   Random processes (mostly choose and shuffle) via [`rand::seq`](https://docs.rs/rand/latest/rand/seq/index.html) traits

All with:

-   [Portably reproducible output](https://rust-random.github.io/book/portability.html)
-   `#[no_std]` compatibility (partial)
-   *Many* performance optimisations thanks to contributions from the wide
    user-base

Rand **is not**:

-   Small (LoC). Most low-level crates are small, but the higher-level `rand`
    and `rand_distr` each contain a lot of functionality.
-   Simple (implementation). We have a strong focus on correctness, speed and flexibility, but
    not simplicity. If you prefer a small-and-simple library, there are
    alternatives including [fastrand](https://crates.io/crates/fastrand)
    and [oorandom](https://crates.io/crates/oorandom).
-   Primarily a cryptographic library. `rand` does provide some generators which
    aim to support unpredictable value generation under certain constraints;
    see [SECURITY.md](https://github.com/rust-random/rand/blob/master/SECURITY.md) for details.
    Users are expected to determine for themselves
    whether `rand`'s functionality meets their own security requirements.

Documentation:

-   [The Rust Rand Book](https://rust-random.github.io/book)
-   [API reference (docs.rs)](https://docs.rs/rand)


## Versions

Rand is *mature* (suitable for general usage, with infrequent breaking releases
which minimise breakage) but not yet at 1.0. Current `MAJOR.MINOR` versions are:

-   Version 0.10 was released in February 2026.

See the [CHANGELOG](https://github.com/rust-random/rand/blob/master/CHANGELOG.md) or [Upgrade Guide](https://rust-random.github.io/book/update.html) for more details.

## Crate Features

Rand is built with these features enabled by default:

-   `std` enables functionality dependent on the `std` lib
-   `alloc` (implied by `std`) enables functionality requiring an allocator; a
    significant portion of sequence and distribution functionality requires this
-   `sys_rng` enables `rand::rngs::SysRng` (uses the [getrandom] crate)
-   `std_rng` enables `rand::rngs::StdRng` (uses the [chacha20] crate)
-   `thread_rng` (implies `std`, `std_rng`, `sys_rng`) enables `rand::rngs::ThreadRng` and `rand::rng()`

Optionally, the following dependencies can be enabled:

-   `chacha` enables `rand::rngs::{ChaCha8Rng, ChaCha12Rng, ChaCha20Rng}` (uses the [chacha20] crate)

Additionally, these features configure Rand:

-   `simd_support` (experimental) enables sampling of SIMD values (uniformly
    random SIMD integers and floats). Since `std::simd` is not yet stable this
    feature requires nightly Rust and may cause build failures.
-   `unbiased` use unbiased sampling for algorithms supporting this option: Uniform distribution.

    (By default, bias affecting no more than one in  2^48 samples is accepted.)

    Note: enabling this option is expected to affect reproducibility of results.

## Portability

### Reproducibility

Achieving reproducible results requires not only deterministic algorithms with fixed inputs but also a commitment to stability of algorithms and some platform-specific considerations. A subset of `rand` does aim to support reproducibility; read more about this in the book: [Portability](https://rust-random.github.io/book/portability.html).

### WebAssembly support

The [WASI](https://github.com/WebAssembly/WASI/tree/main) and Emscripten
targets are directly supported. The `wasm32-unknown-unknown` target is not
*automatically* supported. To enable support for this target, refer to the
[`getrandom` documentation for WebAssembly](https://docs.rs/getrandom/latest/getrandom/#webassembly-support).
Alternatively, the `sys_rng` feature may be disabled.

# License

Rand is distributed under the terms of both the MIT license and the
Apache License (Version 2.0).

See [LICENSE-APACHE](https://github.com/rust-random/rand/blob/master/LICENSE-APACHE) and [LICENSE-MIT](https://github.com/rust-random/rand/blob/master/LICENSE-MIT), and
[COPYRIGHT](https://github.com/rust-random/rand/blob/master/COPYRIGHT) for details.

[getrandom]: https://crates.io/crates/getrandom
[chacha20]: https://crates.io/crates/chacha20

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:rand-kotlin:0.1.0")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [CLAUDE.md](CLAUDE.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same MIT license as the upstream [`rust-random/rand`](https://github.com/rust-random/rand). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the rand authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`rust-random/rand`](https://github.com/rust-random/rand) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.
