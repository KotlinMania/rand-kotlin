// port-lint: source rng.rs
package io.github.kotlinmania.rand

import kotlin.random.Random

/**
 * Implementation-level interface for Random (Number) Generators.
 */
public interface RngCore {
    public fun nextU32(): UInt

    public fun nextU64(): ULong

    public fun fillBytes(dest: ByteArray)
}

/**
 * Marker interface for cryptographically secure random number generators.
 */
public interface CryptoRng : RngCore

/**
 * Trait for seedable random number generators.
 */
public interface SeedableRng : RngCore {
    public fun seedFromU64(seed: ULong)
}

/**
 * User-level interface on RNGs.
 */
public interface Rng : RngCore {
    public fun randomBool(p: Double = 0.5): Boolean {
        require(p in 0.0..1.0) { "p=$p is outside range [0.0, 1.0]" }
        if (p == 0.0) return false
        if (p == 1.0) return true
        val u = (nextU64().toDouble()) / ULong.MAX_VALUE.toDouble()
        return u < p
    }

    public fun randomRatio(numerator: UInt, denominator: UInt): Boolean {
        require(denominator > 0u) { "denominator cannot be 0" }
        require(numerator <= denominator) { "p=$numerator/$denominator is outside range [0.0, 1.0]" }
        if (numerator == 0u) return false
        if (numerator == denominator) return true
        val randVal = nextU64() % denominator.toULong()
        return randVal < numerator.toULong()
    }

    public fun randomRange(range: IntRange): Int {
        require(!range.isEmpty()) { "cannot sample empty range" }
        val span = (range.last.toLong() - range.first.toLong() + 1L).toULong()
        if (span == 1uL) return range.first
        val offset = (nextU64() % span).toLong()
        return (range.first.toLong() + offset).toInt()
    }

    public fun randomRange(range: ClosedFloatingPointRange<Double>): Double {
        require(range.start <= range.endInclusive) { "cannot sample empty range" }
        if (range.start == range.endInclusive) return range.start
        val u = (nextU64().toDouble()) / ULong.MAX_VALUE.toDouble()
        return range.start + u * (range.endInclusive - range.start)
    }

    public fun randomRange(range: ClosedFloatingPointRange<Float>): Float {
        require(range.start <= range.endInclusive) { "cannot sample empty range" }
        if (range.start == range.endInclusive) return range.start
        val u = (nextU32().toFloat()) / UInt.MAX_VALUE.toFloat()
        return range.start + u * (range.endInclusive - range.start)
    }

    public fun fill(dest: ByteArray) {
        fillBytes(dest)
    }
}

/**
 * Standard Kotlin Random wrapper implementing [Rng].
 */
public class RandomRng(
    private val random: Random = Random.Default,
) : Rng {
    override fun nextU32(): UInt = random.nextInt().toUInt()

    override fun nextU64(): ULong = random.nextLong().toULong()

    override fun fillBytes(dest: ByteArray) {
        random.nextBytes(dest)
    }
}

/**
 * Step generator yielding constant or incremental sequences for testing.
 */
public class StepRng(
    private var initial: ULong,
    private val increment: ULong = 0uL,
) : Rng {
    override fun nextU32(): UInt = nextU64().toUInt()

    override fun nextU64(): ULong {
        val result = initial
        initial += increment
        return result
    }

    override fun fillBytes(dest: ByteArray) {
        var i = 0
        while (i < dest.size) {
            val v = nextU64()
            for (j in 0 until 8) {
                if (i < dest.size) {
                    dest[i++] = ((v shr (j * 8)) and 0xFFuL).toByte()
                }
            }
        }
    }
}
