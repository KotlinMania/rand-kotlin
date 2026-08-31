// port-lint: source rand/src/distr/other.rs
package io.github.kotlinmania.rand.distr

import io.github.kotlinmania.rand.Rng

/**
 * Sample a byte or char, uniformly distributed over ASCII letters and numbers:
 * a-z, A-Z and 0-9.
 */
public object Alphanumeric : Distribution<Byte>, SampleString {
    private val RANGE: UInt = 62u
    private val GEN_ASCII_STR_CHARSET: ByteArray =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".encodeToByteArray()

    override fun sample(rng: Rng): Byte {
        while (true) {
            val varBits = rng.nextU32() shr (32 - 6)
            if (varBits < RANGE) {
                return GEN_ASCII_STR_CHARSET[varBits.toInt()]
            }
        }
    }

    public fun sampleChar(rng: Rng): Char = sample(rng).toInt().toChar()

    override fun appendString(rng: Rng, string: StringBuilder, len: Int) {
        for (i in 0 until len) {
            string.append(sampleChar(rng))
        }
    }
}

/**
 * Sample a byte or char, uniformly distributed over letters:
 * a-z and A-Z.
 */
public object Alphabetic : Distribution<Byte>, SampleString {
    private val RANGE: UInt = 52u

    override fun sample(rng: Rng): Byte {
        val offset = (rng.nextU32() % RANGE).toByte() + 'A'.code.toByte()
        val isLower = if (offset > 'Z'.code.toByte()) ('a'.code - 'Z'.code - 1).toByte() else 0.toByte()
        return (offset + isLower).toByte()
    }

    public fun sampleChar(rng: Rng): Char = sample(rng).toInt().toChar()

    override fun appendString(rng: Rng, string: StringBuilder, len: Int) {
        for (i in 0 until len) {
            string.append(sampleChar(rng))
        }
    }
}

/**
 * A distribution to sample floating point numbers uniformly in the open
 * interval `(0, 1)`, i.e. not including either endpoint.
 */
public object Open01 : Distribution<Double> {
    override fun sample(rng: Rng): Double {
        val fraction = (rng.nextU64() shr 12) or 1uL
        return fraction.toDouble() / (1L shl 52).toDouble()
    }

    public fun sampleFloat(rng: Rng): Float {
        val fraction = (rng.nextU32() shr 9) or 1u
        return fraction.toFloat() / (1 shl 23).toFloat()
    }
}

/**
 * A distribution to sample floating point numbers uniformly in the half-open
 * interval `(0, 1]`, i.e. including 1 but not 0.
 */
public object OpenClosed01 : Distribution<Double> {
    override fun sample(rng: Rng): Double {
        val fraction = (rng.nextU64() shr 11) + 1uL
        return fraction.toDouble() * (1.0 / (1L shl 53).toDouble())
    }

    public fun sampleFloat(rng: Rng): Float {
        val fraction = (rng.nextU32() shr 8) + 1u
        return fraction.toFloat() * (1.0f / (1 shl 24).toFloat())
    }
}

/**
 * Standard uniform distribution across standard types.
 */
public object StandardUniform : Distribution<Double>, SampleString {
    override fun sample(rng: Rng): Double {
        val fraction = rng.nextU64() shr 11
        return fraction.toDouble() * (1.0 / (1L shl 53).toDouble())
    }

    public fun sampleFloat(rng: Rng): Float {
        val fraction = rng.nextU32() shr 8
        return fraction.toFloat() * (1.0f / (1 shl 24).toFloat())
    }

    public fun sampleBoolean(rng: Rng): Boolean = (rng.nextU32().toInt()) < 0

    public fun sampleByte(rng: Rng): Byte = rng.nextU32().toByte()

    public fun sampleShort(rng: Rng): Short = rng.nextU32().toShort()

    public fun sampleInt(rng: Rng): Int = rng.nextU32().toInt()

    public fun sampleLong(rng: Rng): Long = rng.nextU64().toLong()

    public fun sampleUByte(rng: Rng): UByte = rng.nextU32().toUByte()

    public fun sampleUShort(rng: Rng): UShort = rng.nextU32().toUShort()

    public fun sampleUInt(rng: Rng): UInt = rng.nextU32()

    public fun sampleULong(rng: Rng): ULong = rng.nextU64()

    public fun sampleChar(rng: Rng): Char {
        // Valid char interval [0, 0xD800) or (0xDFFF, 0x110000)
        val gapSize = 0xDFFF - 0xD800 + 1
        val range = 0x110000 - gapSize
        var n = (rng.nextU32() % range.toUInt()).toInt() + gapSize
        if (n <= 0xDFFF) {
            n -= gapSize
        }
        return n.toChar()
    }

    override fun appendString(rng: Rng, string: StringBuilder, len: Int) {
        for (i in 0 until len) {
            string.append(sampleChar(rng))
        }
    }
}
