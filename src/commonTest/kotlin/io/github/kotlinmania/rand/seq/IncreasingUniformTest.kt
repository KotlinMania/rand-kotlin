// port-lint: tests seq/increasing_uniform.rs
package io.github.kotlinmania.rand.seq

import io.github.kotlinmania.rand.rngs.StepRng
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncreasingUniformTest {
    @Test
    fun testIncreasingUniform() {
        val rng = StepRng(100uL, 5uL)
        val roller = IncreasingUniform(rng, 0u)

        val idx0 = roller.nextIndex()
        assertEquals(0, idx0)

        for (i in 1..10) {
            val idx = roller.nextIndex()
            assertTrue(idx in 0..i)
        }
    }
}
