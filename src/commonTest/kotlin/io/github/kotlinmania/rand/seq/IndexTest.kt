// port-lint: tests seq/index.rs
package io.github.kotlinmania.rand.seq

import io.github.kotlinmania.rand.rngs.StepRng
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IndexTest {
    @Test
    fun testSampleBoundaries() {
        val r = StepRng(404uL, 1uL)

        assertEquals(0, sampleInplace(r, 0, 0).len())
        assertEquals(0, sampleInplace(r, 1, 0).len())
        assertEquals(listOf(0), sampleInplace(r, 1, 1).intoVec())

        assertEquals(0, sampleRejection(r, 1, 0).len())

        assertEquals(0, sampleFloyd(r, 0, 0).len())
        assertEquals(0, sampleFloyd(r, 1, 0).len())
        assertEquals(listOf(0), sampleFloyd(r, 1, 1).intoVec())
    }

    @Test
    fun testSampleAlg() {
        val r = StepRng(420uL, 7uL)

        val length = 100
        val amount = 50
        val v1 = sample(r, length, amount)
        assertTrue(v1.all { it in 0 until length })
        assertEquals(50, v1.size)
    }

    @Test
    fun testSampleWeighted() {
        val r = StepRng(423uL, 3uL)
        for (amount in listOf(0, 5, 9)) {
            val len = 10
            val v = sampleWeighted(r, len, amount) { it.toDouble() }
            assertEquals(amount, v.size)
            for (idx in v) {
                assertTrue(idx in 0 until len)
            }
        }
    }
}
