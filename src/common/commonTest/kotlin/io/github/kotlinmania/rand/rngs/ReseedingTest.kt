// port-lint: tests rngs/reseeding.rs
package io.github.kotlinmania.rand.rngs

import io.github.kotlinmania.rand.StepRng
import io.github.kotlinmania.rand.fill
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ReseedingTest {
    @Test
    fun testReseeding() {
        val zero = StepRng(0uL, 0uL)
        val thresh = 1uL
        val reseeding = ReseedingRng.new(thresh, zero)

        val buf0 = UIntArray(32)
        val buf1 = UIntArray(32)
        reseeding.fill(buf0)
        reseeding.fill(buf1)
        val seq0 = buf0.copyOf()
        val seq1 = buf1.copyOf()
        repeat(10) {
            reseeding.fill(buf0)
            reseeding.fill(buf1)
            assertContentEquals(seq0, buf0)
            assertContentEquals(seq1, buf1)
        }
    }

    @Test
    fun testCloneReseeding() {
        val zero = StepRng(0uL, 0uL)
        val rng1 = ReseedingRng.new(32uL * 4uL, zero)

        val first: UInt = rng1.nextU32()
        repeat(10) {
            rng1.nextU32()
        }

        val rng2 = rng1.clone()
        assertEquals(first, rng2.nextU32())
    }
}
