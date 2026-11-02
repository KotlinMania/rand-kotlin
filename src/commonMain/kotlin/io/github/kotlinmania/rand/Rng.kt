// port-lint: source rng.rs
package io.github.kotlinmania.rand

import io.github.kotlinmania.rand.distr.Bernoulli
import io.github.kotlinmania.rand.distr.Distribution
import io.github.kotlinmania.rand.distr.Iter
import io.github.kotlinmania.rand.distr.Uniform
import io.github.kotlinmania.rand.distr.sampleIter
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
    public fun <T> sample(distr: Distribution<T>): T = distr.sample(this)

    public fun <T> sampleIter(distr: Distribution<T>): Iter<T> = distr.sampleIter(this)

    public fun randomBool(p: Double = 0.5): Boolean {
        val d = Bernoulli.create(p)
        return sample(d)
    }

    public fun randomRatio(numerator: UInt, denominator: UInt): Boolean {
        val d = Bernoulli.fromRatio(numerator, denominator)
        return sample(d)
    }

    /**
     * Generate a random value in the given inclusive [range].
     *
     * This is optimized for a single sample. [Uniform] may be faster when
     * sampling repeatedly from the same range.
     *
     * Throws if the range is empty.
     */
    public fun randomRange(range: IntRange): Int {
        require(!range.isEmpty()) { "cannot sample empty range" }
        return Uniform.sampleSingleInclusive(range.first, range.last, this).unwrap()
    }

    /** Generate a random value in the inclusive [range], throwing if it is empty. */
    public fun randomRange(range: LongRange): Long {
        require(!range.isEmpty()) { "cannot sample empty range" }
        return Uniform.sampleSingleInclusive(range.first, range.last, this).unwrap()
    }

    /**
     * Generate a random value in the inclusive [range], throwing if it is empty.
     *
     * An exclusive upper bound can be written with `until`:
     *
     * ```
     * fun example(rng: Rng) {
     *     val exclusive = rng.randomRange(0u until 10u)
     *     val inclusive = rng.randomRange(0u..10u)
     *     println(exclusive)
     *     println(inclusive)
     * }
     * ```
     */
    public fun randomRange(range: UIntRange): UInt {
        require(!range.isEmpty()) { "cannot sample empty range" }
        return Uniform.sampleSingleInclusive(range.first, range.last, this).unwrap()
    }

    /** Generate a random value in the inclusive [range], throwing if it is empty. */
    public fun randomRange(range: ULongRange): ULong {
        require(!range.isEmpty()) { "cannot sample empty range" }
        return Uniform.sampleSingleInclusive(range.first, range.last, this).unwrap()
    }

    public fun randomRange(range: ClosedFloatingPointRange<Double>): Double = Uniform.tryFromDouble(range).unwrap().sample(this)

    public fun randomRange(range: ClosedFloatingPointRange<Float>): Float = Uniform.tryFromFloat(range).unwrap().sample(this)

    public fun fill(dest: ByteArray) {
        fillBytes(dest)
    }

    public fun fill(dest: IntArray) {
        for (i in dest.indices) {
            dest[i] = nextU32().toInt()
        }
    }

    public fun fill(dest: LongArray) {
        for (i in dest.indices) {
            dest[i] = nextU64().toLong()
        }
    }
}

public fun Rng.fill(dest: UIntArray) {
    for (i in dest.indices) {
        dest[i] = nextU32()
    }
}

public fun Rng.fill(dest: ULongArray) {
    for (i in dest.indices) {
        dest[i] = nextU64()
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
