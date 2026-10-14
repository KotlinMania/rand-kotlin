// port-lint: source distr/mod.rs
package io.github.kotlinmania.rand.distr

/**
 * Re-exports for the `distr` module.
 *
 * This file provides access to all distribution types in a single place,
 * mirroring the Rust `distr::mod` re-export structure.
 */

// StandardUniform is defined in Other.kt
// Distribution, Iter, Map are defined in Distribution.kt
// Bernoulli, BernoulliError are defined in Bernoulli.kt
// Open01, OpenClosed01 are defined in Other.kt
// Alphabetic, Alphanumeric are defined in Other.kt
// Uniform is defined in Uniform.kt
// Choose is defined in Slice.kt
// WeightedIndex is in the weighted subpackage

/**
 * The standard uniform distribution.
 *
 * This is a type alias for the [StandardUniform] distribution, which samples
 * values uniformly across the full range of the target type.
 */
public typealias StandardUniformDistribution = StandardUniform