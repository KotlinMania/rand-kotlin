package io.github.kotlinmania.rand

import io.github.kotlinmania.rand.distr.Result
import io.github.kotlinmania.rand.distr.weighted.WeightError
import io.github.kotlinmania.rand.distr.weighted.WeightUpdate
import io.github.kotlinmania.rand.distr.weighted.WeightedIndex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class WeightedSamplingParityTest {
    private class Words(
        vararg words: ULong,
    ) : Rng {
        private val words = words.toList()
        var draws = 0

        override fun nextU32(): UInt = error("unexpected u32 draw")

        override fun nextU64(): ULong = words[draws++]

        override fun fillBytes(dest: ByteArray): Unit = error("unexpected byte draw")
    }

    private fun choices(weights: DoubleArray, words: List<ULong>, expected: List<Int>) {
        val dist = WeightedIndex.create(weights)
        val rng = Words(*words.toULongArray())
        assertEquals(expected, words.map { dist.sample(rng) })
        assertEquals(words.size, rng.draws)
    }

    @Test
    fun maximalWordNormalWeight() {
        choices(doubleArrayOf(1.0, 0.0), listOf(ULong.MAX_VALUE), listOf(0))
    }

    @Test
    fun lowTwelveBitsAreDiscarded() {
        choices(doubleArrayOf(1.0, 2.0), listOf(0x5555555555555000uL, 0x5555555555555fffuL), listOf(0, 0))
    }

    @Test
    fun zeroWeightBoundaries() {
        choices(doubleArrayOf(0.0, 2.0, 0.0, 3.0, 0.0), listOf(0uL, 0x8000000000000000uL, ULong.MAX_VALUE), listOf(1, 3, 3))
    }

    @Test
    fun successfulUpdateRefreshesSampler() {
        val dist = WeightedIndex.create(doubleArrayOf(1.0, 1.0))
        val rng = Words(0xc000000000000000uL, 0xc000000000000000uL)
        assertEquals(1, dist.sample(rng))
        assertIs<Result.Ok<Unit>>(dist.updateWeights(listOf(WeightUpdate(0, 3.0))))
        assertEquals(listOf(3.0, 1.0), dist.weights())
        assertEquals(4.0, dist.totalWeight())
        assertEquals(1, dist.sample(rng))
        assertEquals(2, rng.draws)
    }

    @Test
    fun rejectedUpdatePreservesSampler() {
        val dist = WeightedIndex.create(doubleArrayOf(1.0, 2.0))
        val failure = assertIs<Result.Err<WeightError>>(dist.updateWeights(listOf(WeightUpdate(0, -1.0))))
        assertIs<WeightError.InvalidWeight>(failure.error)
        assertEquals(listOf(1.0, 2.0), dist.weights())
        assertEquals(3.0, dist.totalWeight())
        val rng = Words(0x5555555555555555uL)
        assertEquals(0, dist.sample(rng))
        assertEquals(1, rng.draws)
    }

    @Test
    fun subnormalRoundingIsNotClamped() {
        choices(doubleArrayOf(Double.fromBits(1), 0.0), listOf(ULong.MAX_VALUE), listOf(1))
    }
}
