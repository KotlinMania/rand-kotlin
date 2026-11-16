// port-lint: source rngs/xoshiro128plusplus.rs
package io.github.kotlinmania.rand.rngs

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.SeedableRng

/**
 * A xoshiro128++ random number generator.
 *
 * The xoshiro128++ algorithm is not suitable for cryptographic purposes, but
 * is very fast and has excellent statistical properties.
 */
public class Xoshiro128PlusPlus(
    private val s: UIntArray,
) : SeedableRng,
    Rng {
    init {
        require(s.size == 4) { "state must contain 4 elements" }
    }

    override fun seedFromU64(seed: ULong) {
        val seeded = seedFromU64Internal(seed)
        for (i in 0 until 4) {
            s[i] = seeded.s[i]
        }
    }

    override fun nextU32(): UInt {
        val res = (s[0] + s[3]).rotateLeft(7) + s[0]

        val t = s[1] shl 9

        s[2] = s[2] xor s[0]
        s[3] = s[3] xor s[1]
        s[1] = s[1] xor s[2]
        s[0] = s[0] xor s[3]

        s[2] = s[2] xor t

        s[3] = s[3].rotateLeft(11)

        return res
    }

    override fun nextU64(): ULong {
        val x0 = nextU32().toULong()
        val x1 = nextU32().toULong()
        return (x1 shl 32) or x0
    }

    override fun fillBytes(dest: ByteArray) {
        var i = 0
        while (i < dest.size) {
            val chunk = nextU32()
            for (b in 0 until 4) {
                if (i + b < dest.size) {
                    dest[i + b] = ((chunk shr (b * 8)) and 0xffu).toByte()
                }
            }
            i += 4
        }
    }

    public companion object {
        private val PHI: ULong = 0x9e3779b97f4a7c15uL

        public fun fromSeed(seed: ByteArray): Xoshiro128PlusPlus {
            require(seed.size == 16) { "seed must be 16 bytes" }
            val state = UIntArray(4)
            for (i in 0 until 4) {
                val offset = i * 4
                var v = 0u
                for (b in 0 until 4) {
                    v = v or ((seed[offset + b].toUByte().toUInt()) shl (b * 8))
                }
                state[i] = v
            }
            if (state.all { it == 0u }) {
                return seedFromU64(0uL)
            }
            return Xoshiro128PlusPlus(state)
        }

        public fun seedFromU64(seed: ULong): Xoshiro128PlusPlus = seedFromU64Internal(seed)

        private fun seedFromU64Internal(seed: ULong): Xoshiro128PlusPlus {
            var state = seed
            val s = UIntArray(4)
            for (chunk in 0 until 2) {
                state += PHI
                var z = state
                z = (z xor (z shr 30)) * 0xbf58476d1ce4e5b9uL
                z = (z xor (z shr 27)) * 0x94d049bb133111ebuL
                z = z xor (z shr 31)
                s[chunk * 2] = z.toUInt()
                s[chunk * 2 + 1] = (z shr 32).toUInt()
            }
            return Xoshiro128PlusPlus(s)
        }
    }
}
