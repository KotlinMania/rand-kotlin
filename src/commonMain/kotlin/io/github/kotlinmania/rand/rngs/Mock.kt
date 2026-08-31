// port-lint: source rand/src/rngs/mock.rs
package io.github.kotlinmania.rand.rngs

import io.github.kotlinmania.rand.Rng

/**
 * A mock generator yielding very predictable output.
 *
 * Generates an arithmetic sequence over a [ULong] number, using wrapping arithmetic.
 */
public class StepRng(
    private var initial: ULong,
    private val increment: ULong = 0uL,
) : Rng {
    override fun nextU32(): UInt = nextU64().toUInt()

    override fun nextU64(): ULong {
        val result = initial
        initial += increment
        return result
    }

    override fun fillBytes(dest: ByteArray) {
        var i = 0
        while (i < dest.size) {
            val v = nextU64()
            for (j in 0 until 8) {
                if (i < dest.size) {
                    dest[i++] = ((v shr (j * 8)) and 0xFFuL).toByte()
                }
            }
        }
    }

    public companion object {
        public fun create(initial: ULong, increment: ULong): StepRng = StepRng(initial, increment)
    }
}
