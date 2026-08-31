// port-lint: source rand/src/seq/coin_flipper.rs
package io.github.kotlinmania.rand.seq

import io.github.kotlinmania.rand.RngCore

/**
 * Utility for flipping coins with exact probabilities using minimal random bits.
 */
public class CoinFlipper(
    public val rng: RngCore,
) {
    private var chunk: UInt = 0u
    private var chunkRemaining: UInt = 0u

    /**
     * Returns true with a probability of 1 / [d].
     * Uses an expected two bits of randomness.
     */
    public fun randomRatioOneOver(d: Int): Boolean {
        require(d > 0) { "denominator must be positive" }
        val leadingZeros = d.countLeadingZeroBits()
        val c = (Int.SIZE_BITS - 1 - leadingZeros).coerceAtMost(32)

        return if (flipCHeads(c.toUInt())) {
            val numerator = 1 shl c
            randomRatio(numerator, d)
        } else {
            false
        }
    }

    /**
     * Returns true with a probability of [initialNumerator] / [d].
     * Uses an expected two bits of randomness.
     */
    public fun randomRatio(initialNumerator: Int, d: Int): Boolean {
        var n = initialNumerator
        while (n < d) {
            val leadingN = n.countLeadingZeroBits()
            val leadingD = d.countLeadingZeroBits()
            val c = (leadingN - (leadingD + 1)).coerceIn(1, 32)

            if (flipCHeads(c.toUInt())) {
                val factor = 1 shl c
                n = (n.toLong() * factor.toLong()).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
            } else {
                if (c == 1) {
                    val nextN = (n + n) - d
                    if (nextN <= 0 || nextN > n) {
                        return false
                    }
                    n = nextN
                } else {
                    return false
                }
            }
        }
        return true
    }

    /**
     * If the next [count] bits of randomness all represent heads, consume them, return true.
     * Otherwise return false and consume the number of heads plus one.
     */
    private fun flipCHeads(count: UInt): Boolean {
        var c = count
        require(c <= 32u)
        while (true) {
            val zeros = chunk.countLeadingZeroBits().toUInt()

            if (zeros < c) {
                chunk = (chunk.toULong() shl (zeros.toInt() + 1)).toUInt()
                chunkRemaining = if (chunkRemaining > (zeros + 1u)) chunkRemaining - (zeros + 1u) else 0u
                return false
            } else {
                if (chunkRemaining >= c) {
                    chunkRemaining -= c
                    chunk = (chunk.toULong() shl c.toInt()).toUInt()
                    return true
                } else {
                    c -= chunkRemaining
                    chunk = rng.nextU32()
                    chunkRemaining = 32u
                }
            }
        }
    }
}
