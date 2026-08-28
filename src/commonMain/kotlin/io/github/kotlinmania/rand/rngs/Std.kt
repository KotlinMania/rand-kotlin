// port-lint: source rngs/std.rs
package io.github.kotlinmania.rand.rngs

import io.github.kotlinmania.rand.CryptoRng
import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.RngCore
import io.github.kotlinmania.rand.SeedableRng
import kotlin.math.min

/**
 * The standard cryptographic RNG, based on ChaCha12.
 */
public class StdRng private constructor(
    private val key: UIntArray,
) : CryptoRng, SeedableRng, Rng {
    private var counter: ULong = 0uL
    private val buffer = ULongArray(8)
    private var bufferIndex: Int = 8

    init {
        require(key.size == 8) { "key must contain 8 32-bit words" }
    }

    override fun seedFromU64(seed: ULong) {
        val seedBytes = ByteArray(32)
        for (i in 0 until 8) {
            seedBytes[i] = ((seed shr (i * 8)) and 0xffuL).toByte()
        }
        val reseeded = fromSeed(seedBytes)
        for (i in 0 until 8) {
            key[i] = reseeded.key[i]
        }
        counter = 0uL
        bufferIndex = 8
    }

    override fun nextU32(): UInt {
        val v = nextU64()
        return (v shr 32).toUInt()
    }

    override fun nextU64(): ULong {
        if (bufferIndex >= 8) {
            refill()
        }
        return buffer[bufferIndex++]
    }

    override fun fillBytes(dest: ByteArray) {
        var offset = 0
        while (offset < dest.size) {
            val chunk = nextU64()
            for (b in 0 until 8) {
                if (offset + b < dest.size) {
                    dest[offset + b] = ((chunk shr (b * 8)) and 0xffuL).toByte()
                }
            }
            offset += 8
        }
    }

    private fun refill() {
        val state = UIntArray(16)
        // Constants: "expand 32-byte k"
        state[0] = 0x61707865u
        state[1] = 0x3320646eu
        state[2] = 0x79622d32u
        state[3] = 0x6b206574u

        for (i in 0 until 8) {
            state[4 + i] = key[i]
        }

        state[12] = counter.toUInt()
        state[13] = (counter shr 32).toUInt()
        state[14] = 0u
        state[15] = 0u
        counter++

        val working = state.copyOf()
        // ChaCha12 has 12 rounds (6 double rounds)
        for (round in 0 until 6) {
            quarterRound(working, 0, 4, 8, 12)
            quarterRound(working, 1, 5, 9, 13)
            quarterRound(working, 2, 6, 10, 14)
            quarterRound(working, 3, 7, 11, 15)

            quarterRound(working, 0, 5, 10, 15)
            quarterRound(working, 1, 6, 11, 12)
            quarterRound(working, 2, 7, 8, 13)
            quarterRound(working, 3, 4, 9, 14)
        }

        for (i in 0 until 16) {
            working[i] = working[i] + state[i]
        }

        for (i in 0 until 8) {
            val low = working[i * 2].toULong()
            val high = working[i * 2 + 1].toULong()
            buffer[i] = low or (high shl 32)
        }
        bufferIndex = 0
    }

    private fun quarterRound(st: UIntArray, a: Int, b: Int, c: Int, d: Int) {
        st[a] = st[a] + st[b]; st[d] = (st[d] xor st[a]).rotateLeft(16)
        st[c] = st[c] + st[d]; st[b] = (st[b] xor st[c]).rotateLeft(12)
        st[a] = st[a] + st[b]; st[d] = (st[d] xor st[a]).rotateLeft(8)
        st[c] = st[c] + st[d]; st[b] = (st[b] xor st[c]).rotateLeft(7)
    }

    public companion object {
        public fun fromSeed(seed: ByteArray): StdRng {
            require(seed.size == 32) { "seed must be 32 bytes" }
            val key = UIntArray(8)
            for (i in 0 until 8) {
                val off = i * 4
                key[i] = (seed[off].toUByte().toUInt()) or
                    ((seed[off + 1].toUByte().toUInt()) shl 8) or
                    ((seed[off + 2].toUByte().toUInt()) shl 16) or
                    ((seed[off + 3].toUByte().toUInt()) shl 24)
            }
            return StdRng(key)
        }

        public fun seedFromU64(seed: ULong): StdRng {
            val rng = Xoshiro256PlusPlus.seedFromU64(seed)
            val seedBytes = ByteArray(32)
            rng.fillBytes(seedBytes)
            return fromSeed(seedBytes)
        }

        public fun fromRng(rng: Rng): StdRng {
            val seedBytes = ByteArray(32)
            rng.fillBytes(seedBytes)
            return fromSeed(seedBytes)
        }

        public fun fromOsRng(): StdRng = fromRng(OsRng)
    }
}
