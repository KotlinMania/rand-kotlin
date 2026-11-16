package io.github.kotlinmania.rand

import io.github.kotlinmania.rand.distr.Result
import io.github.kotlinmania.rand.distr.weighted.WeightError
import io.github.kotlinmania.rand.distr.weighted.WeightedIndex
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertSame

class WeightedInputParityTest {
    private class Probe(
        private val values: List<Double>,
        private val failAt: Int? = null,
        val failure: Exception = IllegalStateException("element failure"),
    ) : AbstractList<Double>() {
        val reads = mutableListOf<Int>()

        override val size: Int get() = values.size

        override fun get(index: Int): Double {
            reads.add(index)
            if (index == failAt) throw failure
            return values[index]
        }
    }

    private fun invalid(values: List<Double>, failAt: Int, expected: List<Int>) {
        val probe = Probe(values, failAt)
        val result = assertIs<Result.Err<WeightError>>(WeightedIndex.new(probe))
        assertIs<WeightError.InvalidWeight>(result.error)
        assertEquals(expected, probe.reads)
        val throwing = Probe(values, failAt)
        assertFailsWith<WeightError.InvalidWeight> { WeightedIndex.create(throwing) }
        assertEquals(expected, throwing.reads)
    }

    @Test
    fun firstInvalidStopsInput() {
        invalid(listOf(-1.0, 4.0), 1, listOf(0))
    }

    @Test
    fun laterInvalidStopsInput() {
        invalid(listOf(1.0, -1.0, 4.0), 2, listOf(0, 1))
    }

    @Test
    fun nanStopsInput() {
        invalid(listOf(1.0, Double.NaN, 4.0), 2, listOf(0, 1))
    }

    @Test
    fun infinityThenInvalidStopsInput() {
        invalid(listOf(1.0, Double.POSITIVE_INFINITY, -1.0, 4.0), 3, listOf(0, 1, 2))
    }

    @Test
    fun validInputIsConsumedAndSampled() {
        val probe = Probe(listOf(1.0, 0.0, 2.0))
        val dist = WeightedIndex.new(probe).unwrap()
        assertEquals(listOf(0, 1, 2), probe.reads)
        assertEquals(listOf(1.0, 0.0, 2.0), dist.weights())
        assertEquals(3.0, dist.totalWeight())
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
        assertEquals(2, dist.sample(rng))
        assertEquals(1, draws)
    }

    @Test
    fun emptyInputIsInvalidWithoutElementReads() {
        val probe = Probe(emptyList())
        val result = assertIs<Result.Err<WeightError>>(WeightedIndex.new(probe))
        assertIs<WeightError.InvalidInput>(result.error)
        assertEquals(emptyList(), probe.reads)
    }

    @Test
    fun infiniteTotalReadsAllThenPanics() {
        val probe = Probe(listOf(Double.MAX_VALUE, Double.MAX_VALUE))
        assertFailsWith<IllegalStateException> { WeightedIndex.new(probe) }
        assertEquals(listOf(0, 1), probe.reads)
    }

    @Test
    fun elementFailurePropagates() {
        val probe = Probe(listOf(1.0, 4.0), 1)
        assertSame(probe.failure, assertFailsWith<IllegalStateException> { WeightedIndex.new(probe) })
        assertEquals(listOf(0, 1), probe.reads)
    }

    @Test
    fun weightErrorFromElementAccessPropagates() {
        val marker = WeightError.InvalidWeight("provider")
        val probe = Probe(listOf(1.0, 4.0), 1, marker)
        assertSame(marker, assertFailsWith<WeightError.InvalidWeight> { WeightedIndex.new(probe) })
        assertEquals(listOf(0, 1), probe.reads)
    }
}
