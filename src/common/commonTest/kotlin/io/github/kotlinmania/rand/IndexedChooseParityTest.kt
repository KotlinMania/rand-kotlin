package io.github.kotlinmania.rand

import io.github.kotlinmania.rand.seq.choose
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class IndexedChooseParityTest {
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

    private fun adapters(size: Int, words: List<UInt>, expected: Int?) {
        val samplers: List<Pair<String, (Rng) -> Int?>> =
            listOf(
                "list" to { rng -> List(size) { it }.choose(rng) },
                "array" to { rng -> Array(size) { it }.choose(rng) },
                "int" to { rng -> IntArray(size) { it }.choose(rng) },
                "long" to { rng -> LongArray(size) { it.toLong() }.choose(rng)?.toInt() },
                "byte" to { rng -> ByteArray(size) { it.toByte() }.choose(rng)?.toInt() },
                "short" to { rng -> ShortArray(size) { it.toShort() }.choose(rng)?.toInt() },
                "float" to { rng -> FloatArray(size) { it.toFloat() }.choose(rng)?.toInt() },
                "double" to { rng -> DoubleArray(size) { it.toDouble() }.choose(rng)?.toInt() },
            )
        for ((name, sample) in samplers) {
            val rng = Words(words)
            assertEquals(expected, sample(rng), name)
            assertEquals(words.size, rng.draws32, name)
            assertEquals(0, rng.draws64, name)
        }
    }

    @Test
    fun emptyDoesNotDraw() {
        adapters(0, emptyList(), null)
    }

    @Test
    fun singletonDrawsU32() {
        adapters(1, listOf(UInt.MAX_VALUE), 0)
    }

    @Test
    fun highBitsSelectTheIndex() {
        adapters(4, listOf(0x80000000u), 2)
    }

    @Test
    fun correctionWithoutOverflow() {
        adapters(52, listOf(0x3b13b13bu, 0u), 11)
    }

    @Test
    fun correctionWithOverflow() {
        adapters(52, listOf(0x3b13b13bu, UInt.MAX_VALUE), 12)
    }

    @Test
    fun maximalWord() {
        adapters(3, listOf(UInt.MAX_VALUE), 2)
    }

    @Test
    fun selectedNullIsAValue() {
        val rng = Words(listOf(0x80000000u))
        assertNull(listOf(0, null, 2).choose(rng))
        assertEquals(1, rng.draws32)
        assertEquals(0, rng.draws64)
    }
}
