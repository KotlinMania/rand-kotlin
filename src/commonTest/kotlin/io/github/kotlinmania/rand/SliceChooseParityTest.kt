package io.github.kotlinmania.rand

import io.github.kotlinmania.rand.distr.Choose
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SliceChooseParityTest {
    private class Words(
        private val words: List<UInt>,
    ) : Rng {
        private var offset = 0
        var draws32 = 0
        var draws64 = 0

        override fun nextU32(): UInt {
            draws32++
            return words[offset++]
        }

        override fun nextU64(): ULong {
            draws64++
            return words[offset++].toULong()
        }

        override fun fillBytes(dest: ByteArray): Unit = error("unexpected byte draw")
    }

    private fun <T> check(dist: Choose<T>, words: List<UInt>, expected: List<T>) {
        val rng = Words(words)
        assertEquals(expected, expected.map { dist.sample(rng) })
        assertEquals(words.size, rng.draws32)
        assertEquals(0, rng.draws64)
    }

    @Test
    fun singletonDrawsU32() {
        val dist = Choose.fromList(listOf(9))
        assertEquals(1, dist.numChoices)
        check(dist, listOf(UInt.MAX_VALUE), listOf(9))
    }

    @Test
    fun powerOfTwoUsesHighBits() {
        val dist = Choose.fromList(listOf(0, 1, 2, 3))
        assertEquals(4, dist.numChoices)
        check(dist, listOf(0u, 0x40000000u, 0x80000000u, UInt.MAX_VALUE), listOf(0, 1, 2, 3))
    }

    @Test
    fun rejectedWordIsRetried() {
        check(Choose.fromList(listOf(0, 1, 2)), listOf(0u, UInt.MAX_VALUE), listOf(2))
    }

    @Test
    fun repeatedSamplesUseTheSameRange() {
        check(Choose.fromList(listOf(0, 1, 2)), listOf(0u, UInt.MAX_VALUE, 0x80000000u), listOf(2, 1))
    }

    @Test
    fun selectedNullIsAValue() {
        check(Choose.fromList(listOf(0, null, 2)), listOf(0x80000000u), listOf(null))
    }

    @Test
    fun arrayFactoryPreservesSelection() {
        check(Choose.fromArray(arrayOf("a", "b", "c", "d")), listOf(0x80000000u), listOf("c"))
    }

    @Test
    fun emptyIsRejected() {
        assertFailsWith<IllegalArgumentException> { Choose.fromList(emptyList<Int>()) }
    }
}
