package io.github.kotlinmania.rand

import io.github.kotlinmania.rand.seq.chooseMultiple
import io.github.kotlinmania.rand.seq.chooseMultipleFill
import io.github.kotlinmania.rand.seq.sampleIteratorIndex
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame

class IteratorReservoirParityTest {
    private data class Item(
        val value: Int,
    )

    private class Words(
        private val words: List<UInt> = emptyList(),
        private val words64: List<ULong> = emptyList(),
    ) : Rng {
        var calls32 = 0
        var calls64 = 0

        override fun nextU32(): UInt = words[calls32++]

        override fun nextU64(): ULong = words64[calls64++]

        override fun fillBytes(dest: ByteArray): Unit = error("unexpected byte draw")
    }

    @Test
    fun replacementsPreserveReservoirOrderAndWordDraws() {
        val rng = Words(listOf(0u, UInt.MAX_VALUE, 0u))
        assertEquals(listOf(4, 1), (0..4).iterator().chooseMultiple(rng, 2))
        assertEquals(3, rng.calls32)
        assertEquals(0, rng.calls64)
    }

    @Test
    fun shortInputNeedsNoRandomness() {
        val rng = Words()
        assertEquals(listOf(0, 1, 2), (0..2).iterator().chooseMultiple(rng, 5))
        assertEquals(0, rng.calls32)
        assertEquals(0, rng.calls64)
    }

    @Test
    fun exactInputNeedsNoRandomness() {
        assertEquals(listOf(0, 1, 2), (0..2).iterator().chooseMultiple(Words(), 3))
    }

    @Test
    fun zeroCapacityStillConsumesInputAndSamplesEachItem() {
        val rng = Words(listOf(0u, 0u, 0u))
        val iter = (0..2).iterator()
        assertEquals(emptyList(), iter.chooseMultiple(rng, 0))
        assertFalse(iter.hasNext())
        assertEquals(3, rng.calls32)
        assertEquals(0, rng.calls64)
    }

    @Test
    fun nullableItemsRemainItems() {
        assertEquals(listOf(null, 1), listOf(null, 1).iterator().chooseMultiple(Words(), 3))
    }

    @Test
    fun fillUsesTheSameReplacements() {
        val rng = Words(listOf(0u, UInt.MAX_VALUE, 0u))
        val buf = arrayOf(Item(-1), Item(-1))
        assertEquals(2, List(5, ::Item).iterator().chooseMultipleFill(rng, buf))
        assertContentEquals(arrayOf(Item(4), Item(1)), buf)
        assertEquals(3, rng.calls32)
        assertEquals(0, rng.calls64)
    }

    @Test
    fun shortFillLeavesTheTailUntouched() {
        val buf = Array(5) { Item(-1) }
        assertEquals(3, List(3, ::Item).iterator().chooseMultipleFill(Words(), buf))
        assertContentEquals(arrayOf(Item(0), Item(1), Item(2), Item(-1), Item(-1)), buf)
    }

    @Test
    fun zeroCapacityFillConsumesInputAndSamplesEachItem() {
        val rng = Words(listOf(0u, 0u, 0u))
        val iter = List(3, ::Item).iterator()
        assertEquals(0, iter.chooseMultipleFill(rng, emptyArray<Item>()))
        assertFalse(iter.hasNext())
        assertEquals(3, rng.calls32)
        assertEquals(0, rng.calls64)
    }

    @Test
    fun nullableFillItemsRemainItems() {
        val buf = arrayOf<Item?>(Item(-1), Item(-1), Item(-1))
        assertEquals(2, listOf(null, Item(1)).iterator().chooseMultipleFill(Words(), buf))
        assertContentEquals(arrayOf(null, Item(1), Item(-1)), buf)
    }

    private class ExhaustionProbe<T>(
        private val single: T,
    ) : Iterator<T> {
        var probes = 0
        var reads = 0

        override fun hasNext(): Boolean {
            probes++
            check(probes <= 2) { "probed after exhaustion" }
            return reads == 0
        }

        override fun next(): T {
            if (reads != 0) throw NoSuchElementException()
            reads++
            return single
        }
    }

    @Test
    fun bothOperationsStopAtFirstExhaustion() {
        val iter = ExhaustionProbe(7)
        assertEquals(listOf(7), iter.chooseMultiple(Words(), 3))
        assertEquals(2, iter.probes)
        assertEquals(1, iter.reads)
        val fillIter = ExhaustionProbe(Item(7))
        val buf = Array(3) { Item(-1) }
        assertEquals(1, fillIter.chooseMultipleFill(Words(), buf))
        assertContentEquals(arrayOf(Item(7), Item(-1), Item(-1)), buf)
        assertEquals(2, fillIter.probes)
        assertEquals(1, fillIter.reads)
    }

    @Test
    fun exclusiveUpperBoundSelectsTheUpstreamWordWidth() {
        val small = Words(listOf(0u))
        assertEquals(0uL, sampleIteratorIndex(small, UInt.MAX_VALUE.toULong()))
        assertEquals(1, small.calls32)
        assertEquals(0, small.calls64)
        for (upper in listOf(UInt.MAX_VALUE.toULong() + 1uL, ULong.MAX_VALUE)) {
            val large = Words(words64 = listOf(0uL))
            assertEquals(0uL, sampleIteratorIndex(large, upper))
            assertEquals(0, large.calls32)
            assertEquals(1, large.calls64)
        }
    }

    @Test
    fun emptyInputsLeaveTheBufferUntouched() {
        val rng = Words()
        assertEquals(emptyList(), emptyList<Int>().iterator().chooseMultiple(rng, 2))
        val buf = arrayOf(Item(1), Item(2))
        assertEquals(0, emptyList<Item>().iterator().chooseMultipleFill(rng, buf))
        assertContentEquals(arrayOf(Item(1), Item(2)), buf)
        assertEquals(0, rng.calls32)
        assertEquals(0, rng.calls64)
    }

    @Test
    fun negativeAmountIsRejectedBeforeReadingOrDrawing() {
        val iter = ExhaustionProbe(7)
        val rng = Words()
        assertFailsWith<IllegalArgumentException> { iter.chooseMultiple(rng, -1) }
        assertEquals(0, iter.probes)
        assertEquals(0, rng.calls32)
        assertEquals(0, rng.calls64)
    }

    @Test
    fun iteratorFailurePropagatesWithTheFilledPrefixIntact() {
        val failure = IllegalStateException("iterator failure")
        val iter = (0..2).asSequence().map { if (it == 2) throw failure else Item(it) }.iterator()
        val rng = Words()
        val buf = Array(3) { Item(-1) }
        assertSame(failure, assertFailsWith<IllegalStateException> { iter.chooseMultipleFill(rng, buf) })
        assertContentEquals(arrayOf(Item(0), Item(1), Item(-1)), buf)
        assertEquals(0, rng.calls32)
        assertEquals(0, rng.calls64)
    }
}
