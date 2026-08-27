// port-lint: tests rand/src/lib.rs
package io.github.kotlinmania.rand

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LibTest {
    @Test
    fun testRandVersion() {
        assertEquals("0.9.0", Rand.VERSION)
    }

    @Test
    fun testRandomBool() {
        assertFalse(randomBool(0.0))
        assertTrue(randomBool(1.0))
    }

    @Test
    fun testRandomRatio() {
        assertFalse(randomRatio(0u, 10u))
        assertTrue(randomRatio(10u, 10u))
    }

    @Test
    fun testRandomRange() {
        val intVal = randomRange(10..20)
        assertTrue(intVal in 10..20)

        val doubleVal = randomRange(1.5..2.5)
        assertTrue(doubleVal in 1.5..2.5)

        val floatVal = randomRange(3.0f..4.0f)
        assertTrue(floatVal in 3.0f..4.0f)
    }

    @Test
    fun testFill() {
        val buffer = ByteArray(16)
        fill(buffer)
        assertEquals(16, buffer.size)
    }

    @Test
    fun testPreludeRng() {
        val r = Prelude.rng()
        val b = r.randomBool(0.5)
        assertTrue(b || !b)
    }
}
