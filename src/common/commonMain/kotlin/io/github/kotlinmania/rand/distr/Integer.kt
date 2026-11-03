// port-lint: source distr/integer.rs
package io.github.kotlinmania.rand.distr

import io.github.kotlinmania.rand.Rng

/**
 * The implementations of the [StandardUniform] distribution for integer types.
 *
 * This file provides sampling functions for all standard integer types (signed and unsigned)
 * via [StandardUniform]. Each integer type samples by taking the appropriate number of
 * random bits from the RNG.
 */
public object IntegerSampling {
    /**
     * Sample a [Byte] uniformly from the full range of [Byte].
     *
     * Uses the least significant 8 bits of a [UInt].
     */
    public fun sampleByte(rng: Rng): Byte = rng.nextU32().toByte()

    /**
     * Sample a [Short] uniformly from the full range of [Short].
     *
     * Uses the least significant 16 bits of a [UInt].
     */
    public fun sampleShort(rng: Rng): Short = rng.nextU32().toShort()

    /**
     * Sample an [Int] uniformly from the full range of [Int].
     *
     * Uses a full [UInt] from the RNG.
     */
    public fun sampleInt(rng: Rng): Int = rng.nextU32().toInt()

    /**
     * Sample a [Long] uniformly from the full range of [Long].
     *
     * Uses a full [ULong] from the RNG.
     */
    public fun sampleLong(rng: Rng): Long = rng.nextU64().toLong()

    /**
     * Sample a [UByte] uniformly from the full range of [UByte].
     *
     * Uses the least significant 8 bits of a [UInt].
     */
    public fun sampleUByte(rng: Rng): UByte = rng.nextU32().toUByte()

    /**
     * Sample a [UShort] uniformly from the full range of [UShort].
     *
     * Uses the least significant 16 bits of a [UInt].
     */
    public fun sampleUShort(rng: Rng): UShort = rng.nextU32().toUShort()

    /**
     * Sample a [UInt] uniformly from the full range of [UInt].
     *
     * Uses a full [UInt] from the RNG.
     */
    public fun sampleUInt(rng: Rng): UInt = rng.nextU32()

    /**
     * Sample a [ULong] uniformly from the full range of [ULong].
     *
     * Uses a full [ULong] from the RNG.
     */
    public fun sampleULong(rng: Rng): ULong = rng.nextU64()

    /**
     * Sample a [UByteArray] of the given [length], filling each element uniformly.
     */
    public fun sampleUByteArray(rng: Rng, length: Int): UByteArray {
        val arr = UByteArray(length)
        for (i in 0 until length) {
            arr[i] = rng.nextU32().toUByte()
        }
        return arr
    }

    /**
     * Sample a [UIntArray] of the given [length], filling each element uniformly.
     */
    public fun sampleUIntArray(rng: Rng, length: Int): UIntArray {
        val arr = UIntArray(length)
        for (i in 0 until length) {
            arr[i] = rng.nextU32()
        }
        return arr
    }

    /**
     * Sample a [ULongArray] of the given [length], filling each element uniformly.
     */
    public fun sampleULongArray(rng: Rng, length: Int): ULongArray {
        val arr = ULongArray(length)
        for (i in 0 until length) {
            arr[i] = rng.nextU64()
        }
        return arr
    }

    /**
     * Sample a [ByteArray] of the given [length], filling each element uniformly.
     */
    public fun sampleByteArray(rng: Rng, length: Int): ByteArray {
        val arr = ByteArray(length)
        rng.fillBytes(arr)
        return arr
    }
}
