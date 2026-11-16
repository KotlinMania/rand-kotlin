// port-lint: source distr/uniform_other.rs
package io.github.kotlinmania.rand.distr

import io.github.kotlinmania.rand.Rng
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

// / UTF-16 surrogate range start
internal const val CHAR_SURROGATE_START: UInt = 0xD800u

// / UTF-16 surrogate range size
internal const val CHAR_SURROGATE_LEN: UInt = 0x800u

/**
 * Convert a [Char] to a compressed [UInt], removing the surrogate gap.
 */
internal fun charToCompU32(c: Char): UInt {
    val v = c.code.toUInt()
    return if (v >= CHAR_SURROGATE_START) v - CHAR_SURROGATE_LEN else v
}

/**
 * The back-end implementing [UniformSampler] for [Char].
 *
 * This differs from integer range sampling since the range `0xD800..=0xDFFF`
 * is used for surrogate pairs in UCS and UTF-16, and consequently are not
 * valid Unicode code points. This sampler avoids sampling values in that range.
 */
public class UniformChar internal constructor(
    internal val sampler: UniformInt<UInt>,
) : UniformSampler<Char> {
    override fun sample(rng: Rng): Char {
        var x = sampler.sample(rng)
        if (x >= CHAR_SURROGATE_START) {
            x += CHAR_SURROGATE_LEN
        }
        return x.toInt().toChar()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UniformChar) return false
        return sampler == other.sampler
    }

    override fun hashCode(): Int = sampler.hashCode()

    override fun toString(): String = "UniformChar(sampler=$sampler)"

    public companion object {
        public fun new(low: Char, high: Char): Result<UniformChar, Error> {
            val lowComp = charToCompU32(low)
            val highComp = charToCompU32(high)
            return UniformInt.new(lowComp, highComp).map { UniformChar(it) }
        }

        public fun newInclusive(low: Char, high: Char): Result<UniformChar, Error> {
            val lowComp = charToCompU32(low)
            val highComp = charToCompU32(high)
            return UniformInt.newInclusive(lowComp, highComp).map { UniformChar(it) }
        }

        public fun sampleSingle(low: Char, high: Char, rng: Rng): Result<Char, Error> {
            val lowComp = charToCompU32(low)
            val highComp = charToCompU32(high)
            return UniformInt.sampleSingle(lowComp, highComp, rng).map { comp ->
                var x = comp
                if (x >= CHAR_SURROGATE_START) {
                    x += CHAR_SURROGATE_LEN
                }
                x.toInt().toChar()
            }
        }

        public fun sampleSingleInclusive(low: Char, high: Char, rng: Rng): Result<Char, Error> {
            val lowComp = charToCompU32(low)
            val highComp = charToCompU32(high)
            return UniformInt.sampleSingleInclusive(lowComp, highComp, rng).map { comp ->
                var x = comp
                if (x >= CHAR_SURROGATE_START) {
                    x += CHAR_SURROGATE_LEN
                }
                x.toInt().toChar()
            }
        }
    }
}

/**
 * Checked multiplication for [ULong], returning null on overflow.
 */
internal fun ULong.checkedMul(other: ULong): ULong? {
    val result = this * other
    if (other != 0uL && result / other != this) return null
    return result
}

/**
 * Checked addition for [ULong], returning null on overflow.
 */
internal fun ULong.checkedAdd(other: ULong): ULong? {
    val result = this + other
    if (result < this) return null
    return result
}

/**
 * The mode of [UniformDuration], determining how nanoseconds and seconds are sampled.
 */
internal sealed interface UniformDurationMode {
    data class Small(
        val secs: ULong,
        val nanos: Uniform<UInt>,
    ) : UniformDurationMode

    data class Medium(
        val nanos: Uniform<ULong>,
    ) : UniformDurationMode

    data class Large(
        val maxSecs: ULong,
        val maxNanos: UInt,
        val secs: Uniform<ULong>,
    ) : UniformDurationMode
}

/**
 * The back-end implementing [UniformSampler] for [Duration].
 *
 * Unless you are implementing [UniformSampler] for your own type, this type
 * should not be used directly, use [Uniform] instead.
 */
public class UniformDuration internal constructor(
    private val mode: UniformDurationMode,
    private val offset: UInt,
) : UniformSampler<Duration> {
    override fun sample(rng: Rng): Duration {
        when (mode) {
            is UniformDurationMode.Small -> {
                val n = mode.nanos.sample(rng)
                return mode.secs.toLong().seconds + n.toInt().nanoseconds
            }
            is UniformDurationMode.Medium -> {
                val nanos = mode.nanos.sample(rng)
                return (nanos / 1_000_000_000uL).toLong().seconds + (nanos % 1_000_000_000uL).toInt().nanoseconds
            }
            is UniformDurationMode.Large -> {
                val nanoRange = Uniform.new(0u, 1_000_000_000u).unwrap()
                while (true) {
                    val s = mode.secs.sample(rng)
                    val n = nanoRange.sample(rng)
                    if (!(s == mode.maxSecs && n > mode.maxNanos)) {
                        val sum = n + offset
                        return s.toLong().seconds + sum.toInt().nanoseconds
                    }
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UniformDuration) return false
        return mode == other.mode && offset == other.offset
    }

    override fun hashCode(): Int = 31 * mode.hashCode() + offset.hashCode()

    override fun toString(): String = "UniformDuration(mode=$mode, offset=$offset)"

    public companion object {
        public fun new(low: Duration, high: Duration): Result<UniformDuration, Error> {
            if (!(low < high)) return Err(Error.EmptyRange)
            return newInclusive(low, high - 1.nanoseconds)
        }

        public fun newInclusive(low: Duration, high: Duration): Result<UniformDuration, Error> {
            if (!(low <= high)) return Err(Error.EmptyRange)

            val lowNanos = low.inWholeNanoseconds
            val highNanos = high.inWholeNanoseconds

            val lowS = (lowNanos / 1_000_000_000).toULong()
            val lowN = (lowNanos % 1_000_000_000).toUInt()
            var highS = (highNanos / 1_000_000_000).toULong()
            var highN = (highNanos % 1_000_000_000).toUInt()

            if (highN < lowN) {
                highS -= 1uL
                highN += 1_000_000_000u
            }

            val mode: UniformDurationMode =
                if (lowS == highS) {
                    UniformDurationMode.Small(
                        secs = lowS,
                        nanos = Uniform.newInclusive(lowN, highN).unwrap(),
                    )
                } else {
                    val max =
                        highS.checkedMul(1_000_000_000uL)?.let { n ->
                            n.checkedAdd(highN.toULong())
                        }
                    if (max != null) {
                        val lowerBound = lowS * 1_000_000_000uL + lowN.toULong()
                        UniformDurationMode.Medium(
                            nanos = Uniform.newInclusive(lowerBound, max).unwrap(),
                        )
                    } else {
                        val maxNanos = highN - lowN
                        UniformDurationMode.Large(
                            maxSecs = highS,
                            maxNanos = maxNanos,
                            secs = Uniform.newInclusive(lowS, highS).unwrap(),
                        )
                    }
                }
            return Ok(UniformDuration(mode, lowN))
        }

        public fun sampleSingle(low: Duration, high: Duration, rng: Rng): Result<Duration, Error> {
            val distr = new(low, high).unwrap()
            return Ok(distr.sample(rng))
        }

        public fun sampleSingleInclusive(low: Duration, high: Duration, rng: Rng): Result<Duration, Error> {
            val distr = newInclusive(low, high).unwrap()
            return Ok(distr.sample(rng))
        }
    }
}
