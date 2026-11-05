// port-lint: source rng.rs
package io.github.kotlinmania.rand

import io.github.kotlinmania.rand.distr.Bernoulli
import io.github.kotlinmania.rand.distr.Distribution
import io.github.kotlinmania.rand.distr.Iter
import io.github.kotlinmania.rand.distr.Result
import io.github.kotlinmania.rand.distr.SampleRange
import io.github.kotlinmania.rand.distr.StandardUniform
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
 * Types which may be filled with random data.
 *
 * This interface allows buffers and custom containers to be efficiently filled
 * with portable random data.
 */
public fun interface Fill {
    /**
     * Fill self with random data from [rng].
     */
    public fun fill(rng: Rng)
}

/**
 * User-level interface for RNGs.
 *
 * [RngCore] is the implementation-level interface for Random (Number) Generators.
 * [Rng] provides the user-level sampling, range, boolean, ratio, and array-fill API.
 */
public interface Rng : RngCore {
    /**
     * Return a random `Double` in `[0, 1)` via the [StandardUniform] distribution.
     */
    public fun random(): Double = StandardUniform.sample(this)

    /**
     * Return a random value sampled from [distr].
     */
    public fun <T> random(distr: Distribution<T>): T = distr.sample(this)

    /**
     * Return an iterator over [StandardUniform] `Double` variates.
     */
    public fun randomIter(): Iter<Double> = StandardUniform.sampleIter(this)

    /**
     * Return an iterator over [distr] variates.
     */
    public fun <T> randomIter(distr: Distribution<T>): Iter<T> = distr.sampleIter(this)

    /**
     * Sample a new value using the given [distr] distribution.
     */
    public fun <T> sample(distr: Distribution<T>): T = distr.sample(this)

    /**
     * Create an iterator that generates values using the given [distr] distribution.
     */
    public fun <T> sampleIter(distr: Distribution<T>): Iter<T> = distr.sampleIter(this)

    /**
     * Return a boolean with a probability [p] of being `true`.
     *
     * Throws if `p < 0.0` or `p > 1.0` or `p` is `NaN`.
     */
    public fun randomBool(p: Double = 0.5): Boolean =
        sample(Bernoulli.create(p))

    /**
     * Return a boolean with a probability of `numerator / denominator` of being `true`.
     *
     * Throws if `denominator == 0u` or `numerator > denominator`.
     */
    public fun randomRatio(numerator: UInt, denominator: UInt): Boolean =
        when (val result = Bernoulli.fromRatio(numerator, denominator)) {
            is Result.Ok -> sample(result.value)
            is Result.Err -> error("p=$numerator/$denominator is outside range [0.0, 1.0]")
        }

    /**
     * Generate a random value in the given [SampleRange], throwing if it is empty.
     */
    public fun <T> randomRange(range: SampleRange<T>): T {
        require(!range.isEmpty()) { "cannot sample empty range" }
        return range.sampleSingle(this).unwrap()
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
     * Generate a random `UInt` in the unit-step [range], throwing if it is empty.
     */
    public fun randomRange(range: UIntProgression): UInt {
        require(!range.isEmpty() && range.step == 1) { "cannot sample empty range" }
        return Uniform.sampleSingleInclusive(range.first, range.last, this).unwrap()
    }

    /**
     * Generate a random `ULong` in the unit-step [range], throwing if it is empty.
     */
    public fun randomRange(range: ULongProgression): ULong {
        require(!range.isEmpty() && range.step == 1L) { "cannot sample empty range" }
        return Uniform.sampleSingleInclusive(range.first, range.last, this).unwrap()
    }

    /**
     * Generate a random `UInt` in `[low, highInclusive]`, throwing if `low > highInclusive`.
     */
    public fun randomRangeInclusive(
        low: UInt,
        highInclusive: UInt,
    ): UInt {
        require(low <= highInclusive) { "cannot sample empty range" }
        return Uniform.sampleSingleInclusive(low, highInclusive, this).unwrap()
    }

    /**
     * Generate a random `ULong` in `[low, highInclusive]`, throwing if `low > highInclusive`.
     */
    public fun randomRangeInclusive(
        low: ULong,
        highInclusive: ULong,
    ): ULong {
        require(low <= highInclusive) { "cannot sample empty range" }
        return Uniform.sampleSingleInclusive(low, highInclusive, this).unwrap()
    }

    /** Generate a random `Double` in the inclusive [range], throwing if it is empty or non-finite. */
    public fun randomRange(range: ClosedFloatingPointRange<Double>): Double {
        require(!range.isEmpty()) { "cannot sample empty range" }
        return Uniform.tryFromDouble(range).unwrap().sample(this)
    }

    /** Generate a random `Float` in the inclusive [range], throwing if it is empty or non-finite. */
    public fun randomRange(range: ClosedFloatingPointRange<Float>): Float {
        require(!range.isEmpty()) { "cannot sample empty range" }
        return Uniform.tryFromFloat(range).unwrap().sample(this)
    }

    /** Generate a random `Double` in `[low, highExclusive)`, throwing if it is empty or non-finite. */
    public fun randomRange(
        low: Double,
        highExclusive: Double,
    ): Double {
        require(low < highExclusive) { "cannot sample empty range" }
        return Uniform.sampleSingle(low, highExclusive, this).unwrap()
    }

    /** Generate a random `Float` in `[low, highExclusive)`, throwing if it is empty or non-finite. */
    public fun randomRange(
        low: Float,
        highExclusive: Float,
    ): Float {
        require(low < highExclusive) { "cannot sample empty range" }
        return Uniform.sampleSingle(low, highExclusive, this).unwrap()
    }

    /**
     * Fill any type implementing [Fill] with random data.
     */
    public fun fill(dest: Fill) {
        dest.fill(this)
    }

    public fun fill(dest: ByteArray) {
        fillBytes(dest)
    }

    public fun fill(dest: BooleanArray) {
        for (i in dest.indices) {
            dest[i] = StandardUniform.sampleBoolean(this)
        }
    }

    public fun fill(dest: CharArray) {
        for (i in dest.indices) {
            dest[i] = StandardUniform.sampleChar(this)
        }
    }

    public fun fill(dest: FloatArray) {
        for (i in dest.indices) {
            dest[i] = StandardUniform.sampleFloat(this)
        }
    }

    public fun fill(dest: DoubleArray) {
        for (i in dest.indices) {
            dest[i] = StandardUniform.sample(this)
        }
    }

    public fun fill(dest: ShortArray) {
        if (dest.isNotEmpty()) {
            unsafe()
            val bytes = ByteArray(dest.size * Short.SIZE_BYTES)
            fillBytes(bytes)
            for (i in dest.indices) {
                val base = i * Short.SIZE_BYTES
                val lo = bytes[base].toInt() and 0xFF
                val hi = bytes[base + 1].toInt() and 0xFF
                dest[i] = (lo or (hi shl 8)).toShort()
            }
        }
    }

    public fun fill(dest: IntArray) {
        if (dest.isNotEmpty()) {
            unsafe()
            val bytes = ByteArray(dest.size * Int.SIZE_BYTES)
            fillBytes(bytes)
            for (i in dest.indices) {
                val base = i * Int.SIZE_BYTES
                var word = 0
                for (b in 0 until Int.SIZE_BYTES) {
                    word = word or ((bytes[base + b].toInt() and 0xFF) shl (b * 8))
                }
                dest[i] = word
            }
        }
    }

    public fun fill(dest: LongArray) {
        if (dest.isNotEmpty()) {
            unsafe()
            val bytes = ByteArray(dest.size * Long.SIZE_BYTES)
            fillBytes(bytes)
            for (i in dest.indices) {
                val base = i * Long.SIZE_BYTES
                var word = 0L
                for (b in 0 until Long.SIZE_BYTES) {
                    word = word or ((bytes[base + b].toLong() and 0xFFL) shl (b * 8))
                }
                dest[i] = word
            }
        }
    }

    /** Alias for [Rng.random]. */
    public fun gen(): Double = random()

    /** Alias for [Rng.randomRange]. */
    public fun <T> genRange(range: SampleRange<T>): T = randomRange(range)

    /** Alias for [Rng.randomRange]. */
    public fun genRange(range: IntRange): Int = randomRange(range)

    /** Alias for [Rng.randomRange]. */
    public fun genRange(range: LongRange): Long = randomRange(range)

    /** Alias for [Rng.randomRange]. */
    public fun genRange(range: UIntProgression): UInt = randomRange(range)

    /** Alias for [Rng.randomRange]. */
    public fun genRange(range: ULongProgression): ULong = randomRange(range)

    /** Alias for [Rng.randomRangeInclusive]. */
    public fun genRangeInclusive(
        low: UInt,
        highInclusive: UInt,
    ): UInt = randomRangeInclusive(low, highInclusive)

    /** Alias for [Rng.randomRangeInclusive]. */
    public fun genRangeInclusive(
        low: ULong,
        highInclusive: ULong,
    ): ULong = randomRangeInclusive(low, highInclusive)

    /** Alias for [Rng.randomRange]. */
    public fun genRange(range: ClosedFloatingPointRange<Double>): Double = randomRange(range)

    /** Alias for [Rng.randomRange]. */
    public fun genRange(range: ClosedFloatingPointRange<Float>): Float = randomRange(range)

    /** Alias for [Rng.randomRange]. */
    public fun genRange(
        low: Double,
        highExclusive: Double,
    ): Double = randomRange(low, highExclusive)

    /** Alias for [Rng.randomRange]. */
    public fun genRange(
        low: Float,
        highExclusive: Float,
    ): Float = randomRange(low, highExclusive)

    /** Alias for [Rng.randomBool]. */
    public fun genBool(p: Double): Boolean = randomBool(p)

    /** Alias for [Rng.randomRatio]. */
    public fun genRatio(numerator: UInt, denominator: UInt): Boolean = randomRatio(numerator, denominator)
}

internal fun unsafe() {}

private class RngCoreAdapter(
    delegate: RngCore,
) : Rng,
    RngCore by delegate

/**
 * Blanket adapter lifting any [RngCore] instance to [Rng].
 */
public fun RngCore.asRng(): Rng =
    this as? Rng ?: RngCoreAdapter(this)

/**
 * Return a random value of reified type [T] via [StandardUniform].
 */
public inline fun <reified T> RngCore.random(): T {
    val r = asRng()
    return when (T::class) {
        Boolean::class -> StandardUniform.sampleBoolean(r) as T
        Byte::class -> StandardUniform.sampleByte(r) as T
        Short::class -> StandardUniform.sampleShort(r) as T
        Int::class -> StandardUniform.sampleInt(r) as T
        Long::class -> StandardUniform.sampleLong(r) as T
        UByte::class -> StandardUniform.sampleUByte(r) as T
        UShort::class -> StandardUniform.sampleUShort(r) as T
        UInt::class -> StandardUniform.sampleUInt(r) as T
        ULong::class -> StandardUniform.sampleULong(r) as T
        Float::class -> StandardUniform.sampleFloat(r) as T
        Double::class -> StandardUniform.sample(r) as T
        Char::class -> StandardUniform.sampleChar(r) as T
        else -> error("Unsupported StandardUniform target type: ${T::class}")
    }
}

/**
 * Return an iterator over [StandardUniform] variates of reified type [T].
 */
public inline fun <reified T> RngCore.randomIter(): Iter<T> =
    Distribution { rng -> rng.random<T>() }.sampleIter(asRng())

/**
 * Alias for [random].
 */
public inline fun <reified T> RngCore.gen(): T = random<T>()

public fun RngCore.randomRange(range: IntRange): Int = asRng().randomRange(range)

public fun RngCore.randomRange(range: LongRange): Long = asRng().randomRange(range)

public fun RngCore.randomRange(range: UIntProgression): UInt = asRng().randomRange(range)

public fun RngCore.randomRange(range: ULongProgression): ULong = asRng().randomRange(range)

public fun RngCore.genRange(range: UIntProgression): UInt = asRng().genRange(range)

public fun RngCore.genRange(range: ULongProgression): ULong = asRng().genRange(range)

public fun RngCore.randomRange(range: ClosedFloatingPointRange<Double>): Double = asRng().randomRange(range)

public fun RngCore.randomRange(range: ClosedFloatingPointRange<Float>): Float = asRng().randomRange(range)

public inline fun <reified T> RngCore.randomRange(range: OpenEndRange<T>): T where T : Number, T : Comparable<T> {
    require(!range.isEmpty()) { "cannot sample empty range" }
    val r = asRng()
    return when (T::class) {
        Double::class -> Uniform.sampleSingle(range.start as Double, range.endExclusive as Double, r).unwrap() as T
        Float::class -> Uniform.sampleSingle(range.start as Float, range.endExclusive as Float, r).unwrap() as T
        Int::class -> Uniform.sampleSingle(range.start as Int, range.endExclusive as Int, r).unwrap() as T
        Long::class -> Uniform.sampleSingle(range.start as Long, range.endExclusive as Long, r).unwrap() as T
        else -> error("Unsupported OpenEndRange target type: ${T::class}")
    }
}

public inline fun <reified T> RngCore.genRange(range: OpenEndRange<T>): T where T : Number, T : Comparable<T> =
    randomRange(range)

public fun RngCore.randomBool(p: Double = 0.5): Boolean = asRng().randomBool(p)

public fun RngCore.randomRatio(numerator: UInt, denominator: UInt): Boolean = asRng().randomRatio(numerator, denominator)

@OptIn(ExperimentalUnsignedTypes::class)
public fun RngCore.fill(dest: UByteArray) {
    if (dest.isNotEmpty()) {
        unsafe()
        val bytes = ByteArray(dest.size)
        fillBytes(bytes)
        for (i in dest.indices) {
            dest[i] = bytes[i].toUByte()
        }
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun RngCore.fill(dest: UShortArray) {
    if (dest.isNotEmpty()) {
        unsafe()
        val bytes = ByteArray(dest.size * UShort.SIZE_BYTES)
        fillBytes(bytes)
        for (i in dest.indices) {
            val base = i * UShort.SIZE_BYTES
            val lo = bytes[base].toInt() and 0xFF
            val hi = bytes[base + 1].toInt() and 0xFF
            dest[i] = (lo or (hi shl 8)).toUShort()
        }
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun RngCore.fill(dest: UIntArray) {
    if (dest.isNotEmpty()) {
        unsafe()
        val bytes = ByteArray(dest.size * UInt.SIZE_BYTES)
        fillBytes(bytes)
        for (i in dest.indices) {
            val base = i * UInt.SIZE_BYTES
            var word = 0u
            for (b in 0 until UInt.SIZE_BYTES) {
                word = word or ((bytes[base + b].toUInt() and 0xFFu) shl (b * 8))
            }
            dest[i] = word
        }
    }
}

@OptIn(ExperimentalUnsignedTypes::class)
public fun RngCore.fill(dest: ULongArray) {
    if (dest.isNotEmpty()) {
        unsafe()
        val bytes = ByteArray(dest.size * ULong.SIZE_BYTES)
        fillBytes(bytes)
        for (i in dest.indices) {
            val base = i * ULong.SIZE_BYTES
            var word = 0uL
            for (b in 0 until ULong.SIZE_BYTES) {
                word = word or ((bytes[base + b].toULong() and 0xFFuL) shl (b * 8))
            }
            dest[i] = word
        }
    }
}

/**
 * Standard Kotlin Random wrapper implementing [Rng].
 */
public class RandomRng(
    private val random: Random = Random.Default,
) : Rng {
    public constructor() : this(Random.Default)

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
    public constructor(initial: ULong) : this(initial, 0uL)
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
