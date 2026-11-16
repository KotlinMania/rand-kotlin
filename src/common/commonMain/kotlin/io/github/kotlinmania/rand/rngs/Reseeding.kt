// port-lint: source rngs/reseeding.rs
package io.github.kotlinmania.rand.rngs

import io.github.kotlinmania.rand.CryptoRng
import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.RngCore

/**
 * A wrapper around any PRNG that adds the ability to reseed it.
 *
 * `ReseedingRng` reseeds the underlying PRNG in the following cases:
 * - On a manual call to [reseed].
 * - After [clone], the clone will be reseeded on first use.
 * - After the PRNG has generated a configurable number of random bytes.
 */
public class ReseedingRng<R : Rng, Rsdr : RngCore>(
    threshold: ULong,
    private val reseeder: Rsdr,
    private val seedToRng: (ByteArray) -> R,
    private val seedLength: Int = 32,
) : CryptoRng,
    Rng {
    private val threshold: Long =
        if (threshold == 0uL || threshold > Long.MAX_VALUE.toULong()) {
            Long.MAX_VALUE
        } else {
            threshold.toLong()
        }

    private var bytesUntilReseed: Long = this.threshold
    private var inner: R = seedToRng(ByteArray(seedLength).also { reseeder.fillBytes(it) })
    private val buffer: UIntArray = UIntArray(BUFFER_SIZE)
    private var bufferIndex: Int = BUFFER_SIZE

    /**
     * Immediately reseed the generator.
     *
     * This discards any remaining random data in the cache.
     */
    public fun reseed() {
        bufferIndex = BUFFER_SIZE
        val seedBytes = ByteArray(seedLength)
        reseeder.fillBytes(seedBytes)
        inner = seedToRng(seedBytes)
        bytesUntilReseed = threshold
    }

    override fun nextU32(): UInt {
        if (bufferIndex >= BUFFER_SIZE) {
            refill()
        }
        return buffer[bufferIndex++]
    }

    override fun nextU64(): ULong {
        val low = nextU32().toULong()
        val high = nextU32().toULong()
        return low or (high shl 32)
    }

    override fun fillBytes(dest: ByteArray) {
        var offset = 0
        while (offset < dest.size) {
            val word = nextU32()
            for (b in 0 until 4) {
                if (offset + b < dest.size) {
                    dest[offset + b] = ((word shr (b * 8)) and 0xffu).toByte()
                }
            }
            offset += 4
        }
    }

    /**
     * Creates a clone of this [ReseedingRng].
     *
     * The clone will be reseeded on first use.
     */
    public fun clone(): ReseedingRng<R, Rsdr> {
        val cloned =
            ReseedingRng(
                threshold = if (threshold == Long.MAX_VALUE) 0uL else threshold.toULong(),
                reseeder = reseeder,
                seedToRng = seedToRng,
                seedLength = seedLength,
            )
        cloned.bytesUntilReseed = 0L
        return cloned
    }

    private fun refill() {
        if (bytesUntilReseed <= 0L) {
            reseed()
            bytesUntilReseed = threshold - BLOCK_BYTES
        } else {
            bytesUntilReseed -= BLOCK_BYTES
        }
        for (i in 0 until BUFFER_SIZE) {
            buffer[i] = inner.nextU32()
        }
        bufferIndex = 0
    }

    public companion object {
        private const val BUFFER_SIZE: Int = 64
        private const val BLOCK_BYTES: Long = 64L * 4L

        /**
         * Create a new `ReseedingRng` wrapping [StdRng] with a specified reseeder.
         */
        public fun <Rsdr : RngCore> new(
            threshold: ULong,
            reseeder: Rsdr,
        ): ReseedingRng<StdRng, Rsdr> =
            ReseedingRng(
                threshold = threshold,
                reseeder = reseeder,
                seedToRng = { seed -> StdRng.fromSeed(seed) },
                seedLength = 32,
            )

        /**
         * Create a new `ReseedingRng` wrapping [StdRng] with [OsRng] as reseeder.
         */
        public fun std(
            threshold: ULong = 64uL * 1024uL,
            reseeder: OsRng = OsRng,
        ): ReseedingRng<StdRng, OsRng> = new(threshold, reseeder)
    }
}
