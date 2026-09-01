// port-lint: tests distr/uniform.rs
package io.github.kotlinmania.rand.distr

import io.github.kotlinmania.rand.rngs.SmallRng
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UniformTest {
    @Test
    fun testUniformInt() {
        val rng = SmallRng.seedFromU64(100uL)
        val distr = Uniform.int(10, 20)
        for (i in 0 until 100) {
            val v = distr.sample(rng)
            assertTrue(v in 10 until 20, "Expected in [10, 20), got $v")
        }
    }

    @Test
    fun testUniformIntInclusive() {
        val rng = SmallRng.seedFromU64(101uL)
        val distr = Uniform.intInclusive(1, 6)
        val seen = mutableSetOf<Int>()
        for (i in 0 until 500) {
            val v = distr.sample(rng)
            assertTrue(v in 1..6, "Expected in [1, 6], got $v")
            seen.add(v)
        }
        assertEquals(6, seen.size)
    }

    @Test
    fun testUniformDouble() {
        val rng = SmallRng.seedFromU64(102uL)
        val distr = Uniform.double(-5.0, 5.0)
        for (i in 0 until 100) {
            val v = distr.sample(rng)
            assertTrue(v >= -5.0 && v < 5.0, "Expected in [-5.0, 5.0), got $v")
        }
    }

    @Test
    fun testUniformLong() {
        val rng = SmallRng.seedFromU64(103uL)
        val distr = Uniform.long(1000L, 2000L)
        for (i in 0 until 100) {
            val v = distr.sample(rng)
            assertTrue(v in 1000L until 2000L, "Expected in [1000, 2000), got $v")
        }
    }
}
