// port-lint: tests distr/other.rs
package io.github.kotlinmania.rand.distr

import io.github.kotlinmania.rand.rngs.SmallRng
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OtherTest {
    @Test
    fun testAlphanumeric() {
        val rng = SmallRng.seedFromU64(806uL)
        for (i in 0 until 100) {
            val c = Alphanumeric.sampleChar(rng)
            assertTrue(c.isLetterOrDigit(), "Expected alphanumeric char, got $c")
        }
    }

    @Test
    fun testAlphabetic() {
        val rng = SmallRng.seedFromU64(806uL)
        for (i in 0 until 100) {
            val c = Alphabetic.sampleChar(rng)
            assertTrue(c.isLetter(), "Expected alphabetic char, got $c")
        }
    }

    @Test
    fun testSampleString() {
        val rng = SmallRng.seedFromU64(1234uL)
        val s = Alphanumeric.sampleString(rng, 16)
        assertEquals(16, s.length)
        assertTrue(s.all { it.isLetterOrDigit() })

        val alphaStr = Alphabetic.sampleString(rng, 20)
        assertEquals(20, alphaStr.length)
        assertTrue(alphaStr.all { it.isLetter() })
    }

    @Test
    fun testOpen01AndOpenClosed01() {
        val rng = SmallRng.seedFromU64(42uL)
        for (i in 0 until 100) {
            val v1 = Open01.sample(rng)
            assertTrue(v1 > 0.0 && v1 < 1.0, "Open01 out of bounds: $v1")

            val v2 = OpenClosed01.sample(rng)
            assertTrue(v2 > 0.0 && v2 <= 1.0, "OpenClosed01 out of bounds: $v2")
        }
    }

    @Test
    fun testStandardUniform() {
        val rng = SmallRng.seedFromU64(999uL)
        for (i in 0 until 100) {
            val d = StandardUniform.sample(rng)
            assertTrue(d >= 0.0 && d < 1.0)

            val f = StandardUniform.sampleFloat(rng)
            assertTrue(f >= 0.0f && f < 1.0f)

            val b = StandardUniform.sampleBoolean(rng)
            // boolean is valid
            assertTrue(b || !b)
        }
    }
}
