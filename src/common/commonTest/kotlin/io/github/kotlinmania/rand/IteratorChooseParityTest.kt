package io.github.kotlinmania.rand

import io.github.kotlinmania.rand.seq.CoinFlipper
import io.github.kotlinmania.rand.seq.choose
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

class IteratorChooseParityTest {
    private class Words(
        private val word: UInt,
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
        assertEquals(expected, (0 until len).iterator().choose(rng))
        assertEquals(draws, rng.calls32)
        assertEquals(0, rng.calls64)
    }

    @Test
    fun emptyAndSingletonInputsDoNotDraw() {
        assertChoice(0, 0u, null, 0)
        assertChoice(1, 0u, 0, 0)
    }

    @Test
    fun headsSelectTheLastElementAndRefillChunks() {
        for ((len, draws) in listOf(2 to 1, 3 to 1, 7 to 1, 16 to 2, 33 to 5, 100 to 18, 1000 to 281)) {
            assertChoice(len, 0u, len - 1, draws)
        }
    }

    @Test
    fun tailsKeepTheFirstElementAndReuseChunkBits() {
        for ((len, draws) in listOf(2 to 1, 3 to 1, 7 to 1, 16 to 1, 33 to 1, 100 to 4, 1000 to 32)) {
            assertChoice(len, UInt.MAX_VALUE, 0, draws)
        }
    }

    @Test
    fun mixedBitsMatchTheUpstreamSelectionsAndDrawCounts() {
        val cases =
            listOf(
                Triple(2, 0, 1),
                Triple(3, 2, 1),
                Triple(7, 2, 1),
                Triple(16, 7, 1),
                Triple(33, 7, 2),
                Triple(100, 7, 5),
                Triple(1000, 7, 50),
            )
        for ((len, result, draws) in cases) {
            assertChoice(len, 0x9e3779b9u, result, draws)
        }
    }

    @Test
    fun largeDenominatorsUseTheSame32BitCoinChunks() {
        val denominators =
            listOf(Int.MAX_VALUE.toULong(), 1uL shl 31, UInt.MAX_VALUE.toULong(), 1uL shl 32, ULong.MAX_VALUE)
        for (d in denominators) {
            val rng = Words(UInt.MAX_VALUE)
            assertFalse(CoinFlipper(rng).randomRatioOneOver(d))
            assertEquals(1, rng.calls32)
            assertEquals(0, rng.calls64)
        }
    }

    @Test
    fun nullableItemsDoNotEndIteration() {
        val rng = Words(0u)
        assertEquals(3, listOf(null, 1, null, 3).iterator().choose(rng))
        assertEquals(1, rng.calls32)
        assertEquals(0, rng.calls64)
    }

    @Test
    fun iteratorFailurePropagatesAfterANullItem() {
        val failure = IllegalStateException("iterator failure")
        val iter =
            sequence<Int?> {
                yield(null)
                throw failure
            }.iterator()
        val rng = Words(0u)
        assertSame(failure, assertFailsWith<IllegalStateException> { iter.choose(rng) })
        assertEquals(0, rng.calls32)
        assertEquals(0, rng.calls64)
    }

    @Test
    fun headsPreserveUnsignedNumeratorsBeyondTheSignedIntRange() {
        for (d in listOf(Int.MAX_VALUE.toULong(), 1uL shl 31, UInt.MAX_VALUE.toULong())) {
            val rng = Words(0u)
            assertTrue(CoinFlipper(rng).randomRatioOneOver(d))
            assertEquals(1, rng.calls32)
            assertEquals(0, rng.calls64)
        }
    }

    @Test
    fun wrappingTailConsumptionPreservesTheUpstreamChunkState() {
        val rng = Words(1u)
        val coin = CoinFlipper(rng)
        repeat(2) { assertFalse(coin.randomRatioOneOver(ULong.MAX_VALUE)) }
        assertEquals(1, rng.calls32)
        assertEquals(0, rng.calls64)
    }
}
