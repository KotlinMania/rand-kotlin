// port-lint: tests distr/bernoulli.rs
package io.github.kotlinmania.rand.distr

import io.github.kotlinmania.rand.StepRng
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BernoulliTest {
    @Test
    fun testTrivial() {
        val r = StepRng(1uL, 1uL)
        val alwaysFalse = Bernoulli.create(0.0)
        val alwaysTrue = Bernoulli.create(1.0)
        for (i in 0 until 5) {
            assertFalse(r.sample(alwaysFalse))
            assertTrue(r.sample(alwaysTrue))
            assertFalse(alwaysFalse.sample(r))
            assertTrue(alwaysTrue.sample(r))
        }
    }

    @Test
    fun testBernoulliRatio() {
        val b = Bernoulli.fromRatio(2u, 3u)
        val p = b.p()
        assertTrue(abs(p - (2.0 / 3.0)) < 1e-4)
    }

    @Test
    fun testBernoulliCanBeCompared() {
        assertEquals(Bernoulli.create(1.0), Bernoulli.create(1.0))
    }
}
