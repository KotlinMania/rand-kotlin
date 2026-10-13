// port-lint: source rngs/mod.rs
package io.github.kotlinmania.rand.rngs

import io.github.kotlinmania.getrandom.GetrandomResult
import io.github.kotlinmania.getrandom.getrandom
import io.github.kotlinmania.rand.CryptoRng
import io.github.kotlinmania.rand.Rng

/**
 * A random number generator that retrieves randomness from the operating system.
 */
public object OsRng : CryptoRng, Rng {
    override fun nextU32(): UInt {
        val buf = ByteArray(4)
        fillBytes(buf)
        return (buf[0].toUByte().toUInt()) or
            ((buf[1].toUByte().toUInt()) shl 8) or
            ((buf[2].toUByte().toUInt()) shl 16) or
            ((buf[3].toUByte().toUInt()) shl 24)
    }

    override fun nextU64(): ULong {
        val buf = ByteArray(8)
        fillBytes(buf)
        var result = 0uL
        for (i in 0 until 8) {
            result = result or ((buf[i].toUByte().toULong()) shl (i * 8))
        }
        return result
    }

    override fun fillBytes(dest: ByteArray) {
        if (dest.isEmpty()) return
        when (val res = getrandom(dest)) {
            is GetrandomResult.Ok -> Unit
            is GetrandomResult.Failure ->
                error("OsRng failed to obtain OS randomness: ${res.errorMessage ?: "unknown error"}")
        }
    }
}
