// port-lint: source rand/src/rngs/small.rs
package io.github.kotlinmania.rand.rngs

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.SeedableRng

/**
 * A small-state, fast, non-crypto, non-portable PRNG.
 */
public class SmallRng(
    private val inner: Xoshiro256PlusPlus,
) : SeedableRng,
    Rng {
    override fun seedFromU64(seed: ULong) {
        inner.seedFromU64(seed)
    }

    override fun nextU32(): UInt = inner.nextU32()

    override fun nextU64(): ULong = inner.nextU64()

    override fun fillBytes(dest: ByteArray) {
        inner.fillBytes(dest)
    }

    public companion object {
        public fun fromSeed(seed: ByteArray): SmallRng = SmallRng(Xoshiro256PlusPlus.fromSeed(seed))

        public fun seedFromU64(state: ULong): SmallRng = SmallRng(Xoshiro256PlusPlus.seedFromU64(state))

        public fun fromRng(rng: Rng): SmallRng {
            val seed = ByteArray(32)
            rng.fillBytes(seed)
            return fromSeed(seed)
        }

        public fun fromOsRng(): SmallRng = fromRng(OsRng)
    }
}
