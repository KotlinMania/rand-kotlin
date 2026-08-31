// port-lint: tests rand/src/distr/weighted/weighted_index.rs
package io.github.kotlinmania.rand.distr.weighted

import io.github.kotlinmania.rand.rngs.StepRng
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class WeightedIndexTest {
    @Test
    fun testWeightedIndexBasic() {
        val weights = doubleArrayOf(1.0, 2.0, 3.0, 0.0, 5.0)
        val dist = WeightedIndex.create(weights)

        assertEquals(11.0, dist.totalWeight())
        assertEquals(5, dist.size)

        val rng = StepRng(1000uL, 200uL)
        val sample = dist.sample(rng)
        assertTrue(sample in 0 until 5)
    }

    @Test
    fun testUpdateWeights() {
        val weights = doubleArrayOf(10.0, 2.0, 3.0, 4.0)
        val dist = WeightedIndex.create(weights)

        dist.updateWeights(listOf(WeightUpdate(1, 100.0), WeightUpdate(2, 4.0)))
        assertEquals(118.0, dist.totalWeight())
    }

    @Test
    fun testInvalidWeights() {
        assertFailsWith<WeightError.InvalidInput> {
            WeightedIndex.create(doubleArrayOf())
        }
        assertFailsWith<WeightError.InsufficientNonZero> {
            WeightedIndex.create(doubleArrayOf(0.0))
        }
        assertFailsWith<WeightError.InvalidWeight> {
            WeightedIndex.create(doubleArrayOf(10.0, -1.0, 30.0))
        }
    }
}
