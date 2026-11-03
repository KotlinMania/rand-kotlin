package io.github.kotlinmania.rand

import io.github.kotlinmania.rand.distr.Result
import io.github.kotlinmania.rand.distr.weighted.WeightError
import io.github.kotlinmania.rand.distr.weighted.WeightedIndex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class WeightedErrorParityTest {
    private fun assertSamplerPanic(weights: DoubleArray) {
        assertFailsWith<IllegalStateException> { WeightedIndex.create(weights) }
        assertFailsWith<IllegalStateException> { WeightedIndex.create(weights.toList()) }
        assertFailsWith<IllegalStateException> { WeightedIndex.new(weights) }
        assertFailsWith<IllegalStateException> { WeightedIndex.new(weights.toList()) }
    }

    private fun assertInvalidWeight(weights: DoubleArray) {
        val result = assertIs<Result.Err<WeightError>>(WeightedIndex.new(weights))
        assertIs<WeightError.InvalidWeight>(result.error)
        assertFailsWith<WeightError.InvalidWeight> { WeightedIndex.create(weights) }
    }

    @Test
    fun finiteOverflowPanics() {
        assertSamplerPanic(doubleArrayOf(Double.MAX_VALUE, Double.MAX_VALUE))
    }

    @Test
    fun positiveInfinityPanics() {
        assertSamplerPanic(doubleArrayOf(1.0, Double.POSITIVE_INFINITY))
    }

    @Test
    fun invalidAfterInfinityIsInvalidWeight() {
        assertInvalidWeight(doubleArrayOf(1.0, Double.POSITIVE_INFINITY, -1.0))
    }

    @Test
    fun invalidAfterOverflowIsInvalidWeight() {
        assertInvalidWeight(doubleArrayOf(Double.MAX_VALUE, Double.MAX_VALUE, Double.NaN))
    }

    @Test
    fun negativeInfinityIsInvalidWeight() {
        assertInvalidWeight(doubleArrayOf(1.0, Double.NEGATIVE_INFINITY))
    }

    @Test
    fun zeroAndEmptyErrorsArePreserved() {
        val zero = assertIs<Result.Err<WeightError>>(WeightedIndex.new(doubleArrayOf(0.0, 0.0)))
        assertIs<WeightError.InsufficientNonZero>(zero.error)
        val empty = assertIs<Result.Err<WeightError>>(WeightedIndex.new(doubleArrayOf()))
        assertIs<WeightError.InvalidInput>(empty.error)
    }

    @Test
    fun finiteLargeWeightsSample() {
        var draws = 0
        val rng =
            object : Rng {
                override fun nextU32(): UInt = error("unexpected u32 draw")

                override fun nextU64(): ULong {
                    draws++
                    return ULong.MAX_VALUE
                }

                override fun fillBytes(dest: ByteArray): Unit = error("unexpected byte draw")
            }
        val dist = WeightedIndex.create(doubleArrayOf(Double.MAX_VALUE / 4.0, Double.MAX_VALUE / 4.0))
        assertEquals(Double.MAX_VALUE / 2.0, dist.totalWeight())
        assertEquals(1, dist.sample(rng))
        assertEquals(1, draws)
    }
}
