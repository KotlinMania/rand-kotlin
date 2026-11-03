// port-lint: tests rngs/thread.rs
package io.github.kotlinmania.rand.rngs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ThreadRngTest {
    @Test
    fun testThreadRng() {
        val r = rng()
        r.nextU32()
        assertEquals(0, r.randomRange(0..0))
    }

    @Test
    fun testDebugOutput() {
        assertEquals("ThreadRng { .. }", rng().toString())
    }

    @Test
    fun testThreadRngReseed() {
        val r = ThreadRng.default()
        val prev = r.nextU64()
        r.reseed()
        val next = r.nextU64()
        assertTrue(prev != 0uL || next != 0uL)
    }
}
