// port-lint: source seq/increasing_uniform.rs
package io.github.kotlinmania.rand.seq

import io.github.kotlinmania.rand.Rng

/**
 * Similar to a Uniform distribution, but after returning a number in the range [0, n],
 * n is increased by 1.
 */
public class IncreasingUniform(
    public val rng: Rng,
    initialN: UInt,
) {
    private var n: UInt = initialN
    private var chunk: UInt = 0u
    private var chunkRemaining: Int = if (initialN == 0u) 1 else 0

    /**
     * Returns a number in `[0, n]` and increments `n` by 1.
     * Generates new random bits as needed.
     */
    public fun nextIndex(): Int {
        val nextN = n + 1u

        val nextChunkRemaining =
            if (chunkRemaining > 0) {
                chunkRemaining - 1
            } else {
                val (bound, remaining) = calculateBoundU32(nextN)
                chunk = (rng.nextU64() % bound.toULong()).toUInt()
                remaining - 1
            }

        val result =
            if (nextChunkRemaining == 0) {
                chunk.toInt()
            } else {
                val r = (chunk % nextN).toInt()
                chunk /= nextN
                r
            }

        chunkRemaining = nextChunkRemaining
        n = nextN
        return result
    }

    public companion object {
        private val RESULT2: Pair<UInt, Int> = calculateInner(2u)

        /**
         * Calculates bound and count such that bound = m * (m+1) * ... * (m + remaining - 1).
         */
        public fun calculateBoundU32(m: UInt): Pair<UInt, Int> {
            require(m > 0u)
            if (m == 2u) {
                return RESULT2
            }
            return calculateInner(m)
        }

        private fun calculateInner(m: UInt): Pair<UInt, Int> {
            var product: ULong = m.toULong()
            var current: ULong = m.toULong() + 1uL

            while (true) {
                val p = product * current
                if (p <= UInt.MAX_VALUE.toULong()) {
                    product = p
                    current += 1uL
                } else {
                    val count = (current - m.toULong()).toInt()
                    return Pair(product.toUInt(), count)
                }
            }
        }
    }
}
