package io.github.kotlinmania.rand

import io.github.kotlinmania.rand.distr.UniformFloatOpen01
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Open01ParityTest {
    private class Word(
        private val value: ULong,
    ) : Rng {
        var draws32 = 0
        var draws64 = 0

        override fun nextU32(): UInt {
            draws32++
            return value.toUInt()
        }

        override fun nextU64(): ULong {
            draws64++
            return value
        }

        override fun fillBytes(dest: ByteArray): Unit = error("unexpected byte draw")
    }

    private fun checkFloat(word: UInt, bits: Int) {
        val rng = Word(word.toULong())
        val actual = UniformFloatOpen01.sampleFloat(rng)
        assertEquals(bits, actual.toBits(), "word $word")
        assertTrue(actual > 0.0f && actual < 1.0f)
        assertEquals(1, rng.draws32)
        assertEquals(0, rng.draws64)
    }

    private fun checkDouble(word: ULong, bits: Long) {
        val rng = Word(word)
        val actual = UniformFloatOpen01.sampleDouble(rng)
        assertEquals(bits, actual.toBits(), "word $word")
        assertTrue(actual > 0.0 && actual < 1.0)
        assertEquals(0, rng.draws32)
        assertEquals(1, rng.draws64)
    }

    @Test
    fun floatZeroWord() {
        checkFloat(0u, 0x33800000)
    }

    @Test
    fun floatMaxWord() {
        checkFloat(UInt.MAX_VALUE, 0x3f7fffff)
    }

    @Test
    fun floatFirstFractionStep() {
        checkFloat(512u, 0x34400000)
    }

    @Test
    fun floatDiscardedBitsAndMidpoint() {
        for (word in listOf(0u, 511u)) checkFloat(word, 0x33800000)
        for (word in listOf(512u, 1023u)) checkFloat(word, 0x34400000)
        for (word in listOf(0x80000000u, 0x800001ffu)) checkFloat(word, 0x3f000001)
    }

    @Test
    fun doubleEndpoints() {
        checkDouble(0uL, 0x3ca0000000000000L)
        checkDouble(ULong.MAX_VALUE, 0x3fefffffffffffffL)
    }

    @Test
    fun doubleDiscardedBitsAndMidpoint() {
        for (word in listOf(0uL, 4095uL)) checkDouble(word, 0x3ca0000000000000L)
        for (word in listOf(4096uL, 8191uL)) checkDouble(word, 0x3cb8000000000000L)
        for (word in listOf(0x8000000000000000uL, 0x8000000000000fffuL)) checkDouble(word, 0x3fe0000000000001L)
    }
}
