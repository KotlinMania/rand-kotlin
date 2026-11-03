package io.github.kotlinmania.rand

import io.github.kotlinmania.rand.seq.shuffle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class ShuffleParityTest {
    private class Words(
        private val word: UInt,
        private val failure: Exception? = null,
    ) : Rng {
        var draws32 = 0
        var draws64 = 0

        override fun nextU32(): UInt {
            draws32++
            failure?.let { throw it }
            return word
        }

        override fun nextU64(): ULong {
            draws64++
            failure?.let { throw it }
            return word.toULong()
        }

        override fun fillBytes(dest: ByteArray): Unit = error("unexpected byte draw")
    }

    private class Data(
        val name: String,
        val snapshot: () -> List<Int>,
        val shuffle: (Rng) -> Unit,
    )

    private fun adapters(size: Int): List<Data> {
        val list = MutableList(size) { it }
        val array = Array(size) { it }
        val ints = IntArray(size) { it }
        val longs = LongArray(size) { it.toLong() }
        val bytes = ByteArray(size) { it.toByte() }
        val shorts = ShortArray(size) { it.toShort() }
        val floats = FloatArray(size) { it.toFloat() }
        val doubles = DoubleArray(size) { it.toDouble() }
        return listOf(
            Data("list", { list.toList() }) { list.shuffle(it) },
            Data("array", { array.toList() }) { array.shuffle(it) },
            Data("int", { ints.toList() }) { ints.shuffle(it) },
            Data("long", { longs.map { it.toInt() } }) { longs.shuffle(it) },
            Data("byte", { bytes.map { it.toInt() } }) { bytes.shuffle(it) },
            Data("short", { shorts.map { it.toInt() } }) { shorts.shuffle(it) },
            Data("float", { floats.map { it.toInt() } }) { floats.shuffle(it) },
            Data("double", { doubles.map { it.toInt() } }) { doubles.shuffle(it) },
        )
    }

    private fun check(size: Int, word: UInt, expected: List<Int>, draws: Int) {
        for (data in adapters(size)) {
            val rng = Words(word)
            data.shuffle(rng)
            assertEquals(expected, data.snapshot(), data.name)
            assertEquals(draws, rng.draws32, data.name)
            assertEquals(0, rng.draws64, data.name)
        }
    }

    @Test
    fun empty() {
        check(0, 0u, emptyList(), 0)
    }

    @Test
    fun singleton() {
        check(1, 4294967295u, listOf(0), 0)
    }

    @Test
    fun twoZero() {
        check(2, 0u, listOf(1, 0), 1)
    }

    @Test
    fun factorialChunk() {
        check(12, 4294967295u, listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11), 1)
    }

    @Test
    fun refillBoundary() {
        check(13, 4294967295u, listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), 2)
    }

    @Test
    fun zeroAcrossChunks() {
        check(25, 0u, listOf(24, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23), 3)
    }

    @Test
    fun mixedAcrossChunks() {
        check(25, 2654435769u, listOf(2, 24, 3, 15, 10, 9, 16, 22, 12, 0, 4, 18, 13, 21, 14, 5, 6, 23, 8, 19, 7, 17, 11, 1, 20), 5)
    }

    @Test
    fun midpointAcrossChunks() {
        check(25, 2147483648u, listOf(24, 0, 1, 2, 3, 4, 11, 6, 7, 18, 9, 5, 10, 12, 13, 14, 15, 8, 17, 16, 19, 20, 21, 22, 23), 3)
    }

    @Test
    fun floatPayloadBitsArePreserved() {
        val floats = intArrayOf(0x80000000u.toInt(), 0x7fc01234, 0x7f800000, 0xff800000u.toInt()).map(Float::fromBits).toFloatArray()
        val floatRng = Words(0u)
        floats.shuffle(floatRng)
        assertEquals(listOf(0xff800000u.toInt(), 0x80000000u.toInt(), 0x7fc01234, 0x7f800000), floats.map { it.toRawBits() })
        assertEquals(1, floatRng.draws32)
        assertEquals(0, floatRng.draws64)
        val doubles =
            longArrayOf(0x8000000000000000uL.toLong(), 0x7ff8000000001234L, 0x7ff0000000000000L, 0xfff0000000000000uL.toLong())
                .map(Double::fromBits)
                .toDoubleArray()
        val doubleRng = Words(0u)
        doubles.shuffle(doubleRng)
        assertEquals(
            listOf(0xfff0000000000000uL.toLong(), 0x8000000000000000uL.toLong(), 0x7ff8000000001234L, 0x7ff0000000000000L),
            doubles.map { it.toRawBits() },
        )
        assertEquals(1, doubleRng.draws32)
        assertEquals(0, doubleRng.draws64)
    }

    @Test
    fun rngFailurePreservesInputAndPropagates() {
        for (data in adapters(4)) {
            val marker = IllegalStateException("rng failure")
            val rng = Words(0u, marker)
            assertSame(marker, assertFailsWith<IllegalStateException>(data.name) { data.shuffle(rng) })
            assertEquals(listOf(0, 1, 2, 3), data.snapshot(), data.name)
            assertEquals(1, rng.draws32, data.name)
            assertEquals(0, rng.draws64, data.name)
        }
    }
}
