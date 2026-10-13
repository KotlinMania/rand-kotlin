// port-lint: tests rngs/mod.rs
package io.github.kotlinmania.rand.rngs

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

class OsRngTest {
    @Test
    fun testOsRngGeneratesValues() {
        val u1 = OsRng.nextU32()
        val u2 = OsRng.nextU32()
        // Consecutive samples from OS RNG should not be identical under normal circumstances
        assertNotEquals(u1, u2)

        val l1 = OsRng.nextU64()
        val l2 = OsRng.nextU64()
        assertNotEquals(l1, l2)
    }

    @Test
    fun testOsRngFillBytes() {
        val dest1 = ByteArray(32)
        val dest2 = ByteArray(32)
        OsRng.fillBytes(dest1)
        OsRng.fillBytes(dest2)

        // Ensure buffer was filled and not all zeroes
        var allZeroes = true
        for (b in dest1) {
            if (b.toInt() != 0) {
                allZeroes = false
                break
            }
        }
        assertFalse(allZeroes, "OsRng buffer should not be all zeroes")
        assertFalse(dest1.contentEquals(dest2), "Consecutive OsRng buffers should differ")
    }

    @Test
    fun testStdRngFromOsRng() {
        val stdRng1 = StdRng.fromOsRng()
        val stdRng2 = StdRng.fromOsRng()
        val v1 = stdRng1.nextU64()
        val v2 = stdRng2.nextU64()
        assertNotEquals(v1, v2)
    }
}
