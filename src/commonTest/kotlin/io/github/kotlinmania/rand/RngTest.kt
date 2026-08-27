// port-lint: tests rand/src/rng.rs
package io.github.kotlinmania.rand

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RngTest {
    @Test
    fun testStepRngIncrement() {
        val stepRng = StepRng(10uL, 5uL)
        assertEquals(10uL, stepRng.nextU64())
        assertEquals(15uL, stepRng.nextU64())
        assertEquals(20uL, stepRng.nextU64())
    }

    @Test
    fun testStepRngFillBytes() {
        val stepRng = StepRng(0x0102030405060708uL, 0uL)
        val buffer = ByteArray(8)
        stepRng.fillBytes(buffer)
        assertEquals(0x08.toByte(), buffer[0])
        assertEquals(0x07.toByte(), buffer[1])
        assertEquals(0x06.toByte(), buffer[2])
        assertEquals(0x05.toByte(), buffer[3])
    }

    @Test
    fun testRandomRangeInclusive() {
        val rng = RandomRng()
        val num = rng.randomRange(5..5)
        assertEquals(5, num)

        val sample = rng.randomRange(10..20)
        assertTrue(sample in 10..20)
    }

    @Test
    fun testRandomBoolProbabilities() {
        val rng = RandomRng()
        assertFalse(rng.randomBool(0.0))
        assertTrue(rng.randomBool(1.0))
    }
}
