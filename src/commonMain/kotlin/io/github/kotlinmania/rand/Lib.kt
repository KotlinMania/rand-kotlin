// port-lint: source rand/src/lib.rs
package io.github.kotlinmania.rand

import io.github.kotlinmania.rand.distr.Distribution
import io.github.kotlinmania.rand.distr.StandardUniform

/**
 * Top-level rand namespace module.
 */
public object Rand {
    public const val VERSION: String = "0.9.0"

    private val threadLocalRng = RandomRng()

    public fun rng(): Rng = threadLocalRng

    public fun randomBool(p: Double = 0.5): Boolean = rng().randomBool(p)

    public fun randomRatio(numerator: UInt, denominator: UInt): Boolean = rng().randomRatio(numerator, denominator)

    public fun randomRange(range: IntRange): Int = rng().randomRange(range)

    public fun randomRange(range: ClosedFloatingPointRange<Double>): Double = rng().randomRange(range)

    public fun randomRange(range: ClosedFloatingPointRange<Float>): Float = rng().randomRange(range)

    public fun fill(dest: ByteArray): Unit = rng().fill(dest)
}

public fun rng(): Rng = Rand.rng()

public fun randomBool(p: Double = 0.5): Boolean = Rand.randomBool(p)

public fun randomRatio(numerator: UInt, denominator: UInt): Boolean = Rand.randomRatio(numerator, denominator)

public fun randomRange(range: IntRange): Int = Rand.randomRange(range)

public fun randomRange(range: ClosedFloatingPointRange<Double>): Double = Rand.randomRange(range)

public fun randomRange(range: ClosedFloatingPointRange<Float>): Float = Rand.randomRange(range)

public fun fill(dest: ByteArray): Unit = Rand.fill(dest)

public fun random(): Double = StandardUniform.sample(Rand.rng())

public fun randomInt(): Int = StandardUniform.sampleInt(Rand.rng())

public fun randomLong(): Long = StandardUniform.sampleLong(Rand.rng())

public fun randomBoolean(): Boolean = StandardUniform.sampleBoolean(Rand.rng())

public fun <T> sample(distribution: Distribution<T>): T = distribution.sample(Rand.rng())
