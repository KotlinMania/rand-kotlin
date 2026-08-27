// port-lint: tests rand/src/distr/distribution.rs
package io.github.kotlinmania.rand.distr

import io.github.kotlinmania.rand.StepRng
import kotlin.test.Test
import kotlin.test.assertTrue

class DistributionTest {
    @Test
    fun testDistributionsIter() {
        val rng = StepRng(100uL, 10uL)
        val distr = Distribution { r -> (r.nextU32().toFloat() % 100f) / 100f }
        val iter = distr.sampleIter(rng)
        var sum = 0f
        for (i in 0 until 100) {
            sum += iter.next()
        }
        assertTrue(sum > 0f)
    }

    @Test
    fun testDistributionsMap() {
        val dist = Distribution { r -> (r.nextU32() % 6u).toInt() }.map { it + 15 }
        val rng = StepRng(10uL, 1uL)
        val v = dist.sample(rng)
        assertTrue(v in 15..20)
    }
}
