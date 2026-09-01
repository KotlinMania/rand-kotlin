// port-lint: source rngs/mod.rs
package io.github.kotlinmania.rand.rngs

import io.github.kotlinmania.rand.CryptoRng
import io.github.kotlinmania.rand.Rng
import kotlin.random.Random

/**
 * A random number generator that retrieves randomness from the operating system.
 */
public object OsRng : CryptoRng, Rng {
    override fun nextU32(): UInt = Random.Default.nextInt().toUInt()

    override fun nextU64(): ULong = Random.Default.nextLong().toULong()

    override fun fillBytes(dest: ByteArray) {
        Random.Default.nextBytes(dest)
    }
}
