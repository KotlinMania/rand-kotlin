// port-lint: source rngs/xoshiro256plusplus.rs
package io.github.kotlinmania.rand.rngs

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.SeedableRng

/**
 * A xoshiro256++ random number generator.
 *
 * The xoshiro256++ algorithm is not suitable for cryptographic purposes, but
 * is very fast and has excellent statistical properties.
 */
public class Xoshiro256PlusPlus(
    private val s: ULongArray,
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
        val v = nextU64()
        return (v shr 32).toUInt()
    }

    override fun nextU64(): ULong {
        val res = (s[0] + s[3]).rotateLeft(23) + s[0]

        val t = s[1] shl 17

        s[2] = s[2] xor s[0]
        s[3] = s[3] xor s[1]
        s[1] = s[1] xor s[2]
        s[0] = s[0] xor s[3]

        s[2] = s[2] xor t

        s[3] = s[3].rotateLeft(45)

        return res
    }

    override fun fillBytes(dest: ByteArray) {
        var i = 0
        while (i < dest.size) {
            val chunk = nextU64()
            for (b in 0 until 8) {
                if (i + b < dest.size) {
                    dest[i + b] = ((chunk shr (b * 8)) and 0xffuL).toByte()
                }
            }
            i += 8
        }
    }

    public companion object {
        private val PHI: ULong = 0x9e3779b97f4a7c15uL

        public fun fromSeed(seed: ByteArray): Xoshiro256PlusPlus {
            require(seed.size == 32) { "seed must be 32 bytes" }
            val state = ULongArray(4)
            for (i in 0 until 4) {
                val offset = i * 8
                var v = 0uL
                for (b in 0 until 8) {
                    v = v or ((seed[offset + b].toUByte().toULong()) shl (b * 8))
                }
                state[i] = v
            }
            if (state.all { it == 0uL }) {
                return seedFromU64(0uL)
            }
            return Xoshiro256PlusPlus(state)
        }

        public fun seedFromU64(seed: ULong): Xoshiro256PlusPlus = seedFromU64Internal(seed)

        private fun seedFromU64Internal(seed: ULong): Xoshiro256PlusPlus {
            var state = seed
            val s = ULongArray(4)
            for (i in 0 until 4) {
                state += PHI
                var z = state
                z = (z xor (z shr 30)) * 0xbf58476d1ce4e5b9uL
                z = (z xor (z shr 27)) * 0x94d049bb133111ebuL
                z = z xor (z shr 31)
                s[i] = z
            }
            return Xoshiro256PlusPlus(s)
        }
    }
}
