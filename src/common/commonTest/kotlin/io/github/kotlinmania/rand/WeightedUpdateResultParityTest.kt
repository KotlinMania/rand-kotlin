package io.github.kotlinmania.rand

import io.github.kotlinmania.rand.distr.Result
import io.github.kotlinmania.rand.distr.weighted.WeightError
import io.github.kotlinmania.rand.distr.weighted.WeightUpdate
import io.github.kotlinmania.rand.distr.weighted.WeightedIndex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WeightedUpdateResultParityTest {
    private fun sampleLast(dist: WeightedIndex) {
        var draws = 0
        val rng =
            object : Rng {
                override fun nextU32(): UInt = error("unexpected u32")

                override fun nextU64(): ULong {
                    draws++
                    return ULong.MAX_VALUE
                }

                override fun fillBytes(dest: ByteArray): Unit = error("unexpected byte draw")
            }
        assertEquals(1, dist.sample(rng))
        assertEquals(1, draws)
    }

    private fun unchanged(dist: WeightedIndex) {
        assertEquals(listOf(1.0, 2.0), dist.weights())
        assertEquals(3.0, dist.totalWeight())
        sampleLast(dist)
    }

    private inline fun <reified E : WeightError> rejected(updates: List<WeightUpdate>) {
        val dist = WeightedIndex.create(doubleArrayOf(1.0, 2.0))
        val result = assertIs<Result.Err<WeightError>>(dist.updateWeights(updates))
        assertIs<E>(result.error)
        unchanged(dist)
        val wrapped = assertIs<Result.Err<WeightError>>(WeightedIndex.updateWeightsResult(dist, updates))
        assertIs<E>(wrapped.error)
        unchanged(dist)
    }

    @Test
    fun emptyUpdateReturnsOk() {
        val dist = WeightedIndex.create(doubleArrayOf(1.0, 2.0))
        assertIs<Result.Ok<Unit>>(dist.updateWeights(emptyList()))
        unchanged(dist)
    }

    @Test
    fun successfulUpdateReturnsOk() {
        val dist = WeightedIndex.create(doubleArrayOf(1.0, 2.0))
        assertIs<Result.Ok<Unit>>(dist.updateWeights(listOf(WeightUpdate(0, 3.0))))
        assertEquals(listOf(3.0, 2.0), dist.weights())
        assertEquals(5.0, dist.totalWeight())
        sampleLast(dist)
    }

    @Test
    fun negativeWeightReturnsError() {
        rejected<WeightError.InvalidWeight>(listOf(WeightUpdate(0, -1.0)))
    }

    @Test
    fun invalidWeightPrecedesOutOfBounds() {
        rejected<WeightError.InvalidWeight>(listOf(WeightUpdate(8, -1.0)))
    }

    @Test
    fun duplicateIndexPrecedesInvalidWeight() {
        rejected<WeightError.InvalidInput>(listOf(WeightUpdate(0, 3.0), WeightUpdate(0, -1.0)))
    }

    @Test
    fun outOfBoundsReturnsError() {
        rejected<WeightError.InvalidInput>(listOf(WeightUpdate(8, 1.0)))
    }

    @Test
    fun zeroTotalReturnsError() {
        rejected<WeightError.InsufficientNonZero>(listOf(WeightUpdate(0, 0.0), WeightUpdate(1, 0.0)))
    }

    @Test
    fun nonfiniteSamplerFailureStillPanics() {
        val dist = WeightedIndex.create(doubleArrayOf(1.0, 2.0))
        assertFailsWith<IllegalStateException> { dist.updateWeights(listOf(WeightUpdate(0, Double.POSITIVE_INFINITY))) }
    }

    @Test
    fun nanTotalAfterPriorPanicIsNotInsufficientNonzero() {
        val dist = WeightedIndex.create(doubleArrayOf(1.0, 2.0))
        assertFailsWith<IllegalStateException> { dist.updateWeights(listOf(WeightUpdate(0, Double.POSITIVE_INFINITY))) }
        assertFailsWith<IllegalStateException> { dist.updateWeights(listOf(WeightUpdate(0, 1.0))) }
        assertTrue(dist.totalWeight().isNaN())
    }
}
