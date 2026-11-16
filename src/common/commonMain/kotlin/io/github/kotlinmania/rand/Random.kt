package io.github.kotlinmania.rand

import io.github.kotlinmania.rand.distr.Distribution
import io.github.kotlinmania.rand.distr.Iter
import io.github.kotlinmania.rand.distr.StandardUniform
import io.github.kotlinmania.rand.rngs.rng

/**
 * Generate a random value using the thread-local random number generator.
 */
public fun random(): Double = StandardUniform.sample(rng())

public fun randomInt(): Int = StandardUniform.sampleInt(rng())

public fun randomLong(): Long = StandardUniform.sampleLong(rng())

public fun randomBoolean(): Boolean = StandardUniform.sampleBoolean(rng())

/**
 * Return an iterator over [StandardUniform] variates from the thread-local generator.
 */
public fun randomIter(): Iter<Double> = rng().sampleIter(StandardUniform)

/**
 * Generate a random value in the given range using the thread-local random number generator.
 */
public fun randomRange(range: IntRange): Int = rng().randomRange(range)

public fun randomRange(range: ClosedFloatingPointRange<Double>): Double = rng().randomRange(range)

public fun randomRange(range: ClosedFloatingPointRange<Float>): Float = rng().randomRange(range)

/**
 * Return a boolean with a probability [p] of being true.
 */
public fun randomBool(p: Double = 0.5): Boolean = rng().randomBool(p)

/**
 * Return a boolean with a probability of `numerator / denominator` of being true.
 */
public fun randomRatio(numerator: UInt, denominator: UInt): Boolean = rng().randomRatio(numerator, denominator)

/**
 * Fill [dest] with random data using the thread-local generator.
 */
public fun fill(dest: ByteArray): Unit = rng().fill(dest)

/**
 * Sample a value from [distribution] using the thread-local generator.
 */
public fun <T> sample(distribution: Distribution<T>): T = distribution.sample(rng())
