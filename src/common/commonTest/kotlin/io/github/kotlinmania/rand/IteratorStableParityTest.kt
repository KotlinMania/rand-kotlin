package io.github.kotlinmania.rand

import io.github.kotlinmania.rand.seq.chooseStable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class IteratorStableParityTest {
    private class Words(
        private val word: UInt = 0u,
    ) : Rng {
        var calls32 = 0
        var calls64 = 0

        override fun nextU32(): UInt {
            calls32++
            return word
        }

        override fun nextU64(): ULong {
            calls64++
            error("unexpected 64-bit draw")
        }

        override fun fillBytes(dest: ByteArray): Unit = error("unexpected byte draw")
    }

    private fun assertChoice(len: Int, word: UInt, expected: Int?, draws: Int) {
        val rng = Words(word)
        assertEquals(expected, (0 until len).iterator().chooseStable(rng))
        assertEquals(draws, rng.calls32)
        assertEquals(0, rng.calls64)
    }

    @Test
    fun selectionsAndDrawCountsMatchUpstreamAcrossIteratorLengths() {
        val lengths = listOf(0, 1, 2, 3, 7, 16, 33, 100, 1000)
        val headsDraws = listOf(0, 0, 1, 1, 1, 2, 5, 18, 281)
        val tailsDraws = listOf(0, 0, 1, 1, 1, 1, 1, 4, 32)
        val mixedDraws = listOf(0, 0, 1, 1, 1, 1, 2, 5, 50)
        val mixedResults = listOf(null, 0, 0, 2, 2, 7, 7, 7, 7)
        for ((index, len) in lengths.withIndex()) {
            assertChoice(len, 0u, if (len == 0) null else len - 1, headsDraws[index])
            assertChoice(len, UInt.MAX_VALUE, if (len == 0) null else 0, tailsDraws[index])
            assertChoice(len, 0x9e3779b9u, mixedResults[index], mixedDraws[index])
        }
    }

    @Test
    fun sequenceAndIterableAdaptersPreserveTheSameDraws() {
        val sequenceRng = Words(UInt.MAX_VALUE)
        val iterableRng = Words(UInt.MAX_VALUE)
        assertEquals(0, (0 until 100).asSequence().chooseStable(sequenceRng))
        assertEquals(0, (0 until 100).asIterable().chooseStable(iterableRng))
        assertEquals(4, sequenceRng.calls32)
        assertEquals(4, iterableRng.calls32)
        assertEquals(0, sequenceRng.calls64)
        assertEquals(0, iterableRng.calls64)
    }

    @Test
    fun nullableItemsDoNotEndSampling() {
        val rng = Words()
        assertEquals(3, listOf(null, 1, null, 3).iterator().chooseStable(rng))
        assertEquals(1, rng.calls32)
        assertEquals(0, rng.calls64)
    }

    @Test
    fun kotlinIteratorsProduceEveryItemWithoutHintSkipping() {
        var produced = 0
        val rng = Words(UInt.MAX_VALUE)
        val iter =
            (0 until 100)
                .asSequence()
                .map {
                    produced++
                    it
                }.iterator()
        assertEquals(0, iter.chooseStable(rng))
        assertEquals(100, produced)
        assertEquals(4, rng.calls32)
        assertEquals(0, rng.calls64)
    }

    @Test
    fun iteratorFailurePropagatesBeforeTheNextDraw() {
        val failure = IllegalStateException("iterator failure")
        val iter =
            sequence {
                yield(7)
                throw failure
            }.iterator()
        val rng = Words()
        assertSame(failure, assertFailsWith<IllegalStateException> { iter.chooseStable(rng) })
        assertEquals(0, rng.calls32)
        assertEquals(0, rng.calls64)
    }

    @Test
    fun firstExhaustionStopsSampling() {
        var probes = 0
        var reads = 0
        val iter =
            object : Iterator<Int> {
                override fun hasNext(): Boolean {
                    probes++
                    check(probes <= 2) { "probed after exhaustion" }
                    return reads == 0
                }

                override fun next(): Int {
                    if (reads != 0) throw NoSuchElementException()
                    reads++
                    return 7
                }
            }
        val rng = Words()
        assertEquals(7, iter.chooseStable(rng))
        assertEquals(2, probes)
        assertEquals(1, reads)
        assertEquals(0, rng.calls32)
        assertEquals(0, rng.calls64)
    }
}
