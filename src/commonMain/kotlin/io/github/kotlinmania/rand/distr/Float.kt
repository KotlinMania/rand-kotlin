// port-lint: source distr/float.rs
package io.github.kotlinmania.rand.distr

import io.github.kotlinmania.rand.Rng

/**
 * The difference between 1.0 and the next representable [Float] value.
 */
internal val FLOAT_EPSILON: Float = Float.fromBits(0x3400_0000)

/**
 * The difference between 1.0 and the next representable [Double] value.
 */
internal val DOUBLE_EPSILON: Double = Double.fromBits(0x3CB0_0000_0000_0000L)

/**
 * Hidden interface used by distribution implementations to combine a fraction
 * and a constant exponent into a float.
 *
 * Only the least significant bits of [intoFloatWithExponent] may be set:
 * 23 for [Float] and 52 for [Double]. The resulting value will fall in a range
 * that depends on the exponent. As an example, the range with exponent 0 is
 * [2^0, 2^1), which is [1, 2).
 */
public interface IntoFloat {
    /**
     * Combine the fraction bits in [this] with a constant [exponent] to produce a float.
     */
    public fun intoFloatWithExponent(exponent: Int): Double
}

/**
 * Implementation of [IntoFloat] for [UInt] producing [Float].
 */
public class UIntIntoFloat(
    private val value: UInt,
) : IntoFloat {
    override fun intoFloatWithExponent(exponent: Int): Double {
        val fractionBits = 23
        val exponentBias = 127
        val exponentBits: UInt = ((exponentBias + exponent).toUInt()) shl fractionBits
        return Float.fromBits((value or exponentBits).toInt()).toDouble()
    }
}

/**
 * Implementation of [IntoFloat] for [ULong] producing [Double].
 */
public class ULongIntoFloat(
    private val value: ULong,
) : IntoFloat {
    override fun intoFloatWithExponent(exponent: Int): Double {
        val fractionBits = 52
        val exponentBias = 1023
        val exponentBits: ULong = ((exponentBias + exponent).toULong()) shl fractionBits
        return Double.fromBits((value or exponentBits).toLong())
    }
}

/**
 * Sample floating point numbers uniformly in the half-open interval `[0, 1)`.
 *
 * All values that can be generated are of the form `n * ε/2`. For [Float]
 * the 24 most significant random bits of a [UInt] are used and for [Double]
 * the 53 most significant bits of a [ULong] are used. The conversion uses the
 * multiplicative method.
 *
 * This is the float-specific companion to [StandardUniform].
 */
public object UniformFloat01 {
    /**
     * Sample a [Float] uniformly from `[0, 1)`.
     */
    public fun sampleFloat(rng: Rng): Float {
        val floatSize = 32
        val precision = 24
        val scale = 1.0f / ((1u shl precision).toFloat())
        val value: UInt = rng.nextU32()
        val shifted = value shr (floatSize - precision)
        return scale * shifted.toFloat()
    }

    /**
     * Sample a [Double] uniformly from `[0, 1)`.
     */
    public fun sampleDouble(rng: Rng): Double {
        val floatSize = 64
        val precision = 53
        val scale = 1.0 / ((1uL shl precision).toDouble())
        val value: ULong = rng.nextU64()
        val shifted = value shr (floatSize - precision)
        return scale * shifted.toDouble()
    }
}

/**
 * Sample floating point numbers uniformly in the half-open interval `(0, 1]`,
 * i.e. including 1 but not 0.
 *
 * All values that can be generated are of the form `n * ε/2`. For [Float]
 * the 24 most significant random bits of a [UInt] are used and for [Double]
 * the 53 most significant bits of a [ULong] are used. The conversion uses the
 * multiplicative method.
 */
public object UniformFloatOpenClosed01 {
    /**
     * Sample a [Float] uniformly from `(0, 1]`.
     */
    public fun sampleFloat(rng: Rng): Float {
        val floatSize = 32
        val precision = 24
        val scale = 1.0f / ((1u shl precision).toFloat())
        val value: UInt = rng.nextU32()
        val shifted = value shr (floatSize - precision)
        // Add 1 to shift up; will not overflow because of right-shift.
        return scale * (shifted + 1u).toFloat()
    }

    /**
     * Sample a [Double] uniformly from `(0, 1]`.
     */
    public fun sampleDouble(rng: Rng): Double {
        val floatSize = 64
        val precision = 53
        val scale = 1.0 / ((1uL shl precision).toDouble())
        val value: ULong = rng.nextU64()
        val shifted = value shr (floatSize - precision)
        return scale * (shifted + 1uL).toDouble()
    }
}

/**
 * Sample floating point numbers uniformly in the open interval `(0, 1)`,
 * i.e. not including either endpoint.
 *
 * All values that can be generated are of the form `n * ε + ε/2`. For [Float]
 * the 23 most significant random bits of a [UInt] are used, for [Double] 52 from
 * a [ULong]. The conversion uses a bit-manipulation method.
 */
public object UniformFloatOpen01 {
    /**
     * Sample a [Float] uniformly from `(0, 1)`.
     */
    public fun sampleFloat(rng: Rng): Float {
        val floatSize = 32
        val fractionBits = 23
        val value: UInt = rng.nextU32()
        val fraction = value shr (floatSize - fractionBits)
        return Float.fromBits(UIntIntoFloat(fraction).intoFloatWithExponent(0).toInt()) - (1.0f - FLOAT_EPSILON / 2.0f)
    }

    /**
     * Sample a [Double] uniformly from `(0, 1)`.
     */
    public fun sampleDouble(rng: Rng): Double {
        val floatSize = 64
        val fractionBits = 52
        val value: ULong = rng.nextU64()
        val fraction = value shr (floatSize - fractionBits)
        return ULongIntoFloat(fraction).intoFloatWithExponent(0) - (1.0 - DOUBLE_EPSILON / 2.0)
    }
}
