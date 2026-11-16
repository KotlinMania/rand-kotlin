package io.github.kotlinmania.rand

import io.github.kotlinmania.rand.distr.Alphabetic
import kotlin.test.Test
import kotlin.test.assertEquals

class AlphabeticParityTest {
    private class Words(
        private val words: List<UInt>,
    ) : Rng {
        var draws = 0

        override fun nextU32(): UInt = words[draws++]

        override fun nextU64(): ULong = error("unexpected u64 draw")

        override fun fillBytes(dest: ByteArray): Unit = error("unexpected byte draw")
    }

    private fun check(words: List<UInt>, expected: String) {
        val rng = Words(words)
        assertEquals(expected.map { it.code.toByte() }, expected.map { Alphabetic.sample(rng) })
        assertEquals(words.size, rng.draws)
    }

    @Test
    fun endpoints() {
        check(listOf(0u, UInt.MAX_VALUE), "Az")
    }

    @Test
    fun midpoints() {
        check(listOf(0x40000000u, 0x80000000u, 0xc0000000u), "Nan")
    }

    @Test
    fun correctionWithoutOverflow() {
        check(listOf(0x3b13b13bu, 0u), "L")
    }

    @Test
    fun correctionWithOverflow() {
        check(listOf(0x3b13b13bu, UInt.MAX_VALUE), "M")
    }

    @Test
    fun appendKeepsPrefix() {
        val rng = Words(listOf(0u, 0x80000000u, UInt.MAX_VALUE))
        val string = StringBuilder("π:")
        Alphabetic.appendString(rng, string, 3)
        assertEquals("π:Aaz", string.toString())
        assertEquals(3, rng.draws)
    }

    @Test
    fun sampledStringPreservesCorrectionDraws() {
        val rng = Words(listOf(0u, 0x3b13b13bu, UInt.MAX_VALUE, UInt.MAX_VALUE))
        assertEquals("AMz", Alphabetic.sampleString(rng, 3))
        assertEquals(4, rng.draws)
    }

    @Test
    fun zeroLengthDoesNotDraw() {
        val rng = Words(emptyList())
        val string = StringBuilder("π:")
        Alphabetic.appendString(rng, string, 0)
        assertEquals("π:", string.toString())
        assertEquals("", Alphabetic.sampleString(rng, 0))
        assertEquals(0, rng.draws)
    }
}
