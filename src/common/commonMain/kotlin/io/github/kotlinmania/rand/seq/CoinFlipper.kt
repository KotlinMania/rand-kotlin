// port-lint: source seq/coin_flipper.rs
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
        return randomRatioOneOver(d.toULong())
    }

    internal fun randomRatioOneOver(d: ULong): Boolean {
        require(d > 0uL) { "denominator must be positive" }
        val c = (ULong.SIZE_BITS - 1 - d.countLeadingZeroBits()).coerceAtMost(32)
        return if (flipCHeads(c.toUInt())) {
            randomRatioWide(1uL shl c, d)
        } else {
            false
        }
    }

    /**
     * Returns true with a probability of [initialNumerator] / [d].
     * Uses an expected two bits of randomness.
     */
    public fun randomRatio(initialNumerator: Int, d: Int): Boolean {
        require(initialNumerator >= 0 && d >= 0) { "ratio terms must be non-negative" }
        return randomRatioWide(initialNumerator.toULong(), d.toULong())
    }

    private fun randomRatioWide(initialNumerator: ULong, d: ULong): Boolean {
        var n = initialNumerator
        while (n < d) {
            // Find a good count by comparing leading zeros. This gives the
            // highest possible count, or one less than that.
            val c = (n.countLeadingZeroBits() - (d.countLeadingZeroBits() + 1)).coerceIn(1, 32)
            if (flipCHeads(c.toUInt())) {
                // All heads. Saturate the product: overflow always means true.
                val factor = 1uL shl c
                n = if (n > ULong.MAX_VALUE / factor) ULong.MAX_VALUE else n * factor
            } else {
                // With one flip, calculate 2n - d using wrapping arithmetic.
                // With more than one flip, 2n < d and a tail means false.
                if (c == 1) {
                    val nextN = n + n - d
                    if (nextN == 0uL || nextN > n) return false
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
                chunk = chunk shl (zeros.toInt() + 1)
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
