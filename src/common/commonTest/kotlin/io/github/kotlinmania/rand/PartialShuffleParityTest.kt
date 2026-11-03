package io.github.kotlinmania.rand

import io.github.kotlinmania.rand.seq.partialShuffle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

class PartialShuffleParityTest {
    private class Words(
        private val word: UInt,
        private val failAt: Int?,
        private val failure: Exception,
    ) : Rng {
        var draws32 = 0
        var draws64 = 0

        private fun checkFailure() {
            if (draws32 + draws64 == failAt) throw failure
        }

        override fun nextU32(): UInt {
            draws32++
            checkFailure()
            return word
        }

        override fun nextU64(): ULong {
            draws64++
            checkFailure()
            return word.toULong()
        }

        override fun fillBytes(dest: ByteArray): Unit = error("unexpected byte draw")
    }

    private class Data(
        val name: String,
        val snapshot: () -> List<Int>,
        val partial: (Rng, Int) -> Pair<List<Int>, List<Int>>,
    )

    private fun adapters(size: Int): List<Data> {
        val list = MutableList(size) { it }
        val array = Array(size) { it }
        val ints = IntArray(size) { it }
        val longs = LongArray(size) { it.toLong() }
        return listOf(
            Data("list", { list.toList() }) { rng, amount -> list.partialShuffle(rng, amount) },
            Data("array", { array.toList() }) { rng, amount -> array.partialShuffle(rng, amount) },
            Data("int", { ints.toList() }) { rng, amount -> ints.partialShuffle(rng, amount) },
            Data("long", { longs.map { it.toInt() } }) { rng, amount ->
                val (a, b) = longs.partialShuffle(rng, amount)
                a.map { it.toInt() } to b.map { it.toInt() }
            },
        )
    }

    private fun check(
        size: Int,
        amount: Int,
        word: UInt,
        expected: Triple<List<Int>, List<Int>?, List<Int>?>,
        draws: Int,
        failAt: Int? = null,
    ) {
        for (data in adapters(size)) {
            val marker = IllegalStateException("rng failure")
            val rng = Words(word, failAt, marker)
            if (failAt == null) {
                val result = data.partial(rng, amount)
                assertEquals(expected.second, result.first, data.name)
                assertEquals(expected.third, result.second, data.name)
            } else {
                assertSame(marker, assertFailsWith<IllegalStateException>(data.name) { data.partial(rng, amount) })
            }
            assertEquals(expected.first, data.snapshot(), data.name)
            assertEquals(draws, rng.draws32, data.name)
            assertEquals(0, rng.draws64, data.name)
        }
    }

    @Test
    fun empty() {
        check(
            0,
            0,
            0u,
            Triple(
                emptyList(),
                emptyList(),
                emptyList(),
            ),
            0,
        )
    }

    @Test
    fun singletonNoop() {
        check(
            1,
            0,
            4294967295u,
            Triple(
                listOf(0),
                emptyList(),
                listOf(0),
            ),
            0,
        )
    }

    @Test
    fun singletonSelected() {
        check(
            1,
            1,
            4294967295u,
            Triple(
                listOf(0),
                listOf(0),
                emptyList(),
            ),
            0,
        )
    }

    @Test
    fun zeroAmount() {
        check(
            5,
            0,
            2654435769u,
            Triple(
                listOf(0, 1, 2, 3, 4),
                emptyList(),
                listOf(0, 1, 2, 3, 4),
            ),
            0,
        )
    }

    @Test
    fun oneSelected() {
        check(
            5,
            1,
            0u,
            Triple(
                listOf(4, 1, 2, 3, 0),
                listOf(0),
                listOf(4, 1, 2, 3),
            ),
            1,
        )
    }

    @Test
    fun partialZero() {
        check(
            5,
            3,
            0u,
            Triple(
                listOf(4, 1, 0, 2, 3),
                listOf(0, 2, 3),
                listOf(4, 1),
            ),
            1,
        )
    }

    @Test
    fun partialMax() {
        check(
            5,
            3,
            4294967295u,
            Triple(
                listOf(0, 1, 2, 3, 4),
                listOf(2, 3, 4),
                listOf(0, 1),
            ),
            1,
        )
    }

    @Test
    fun highStart() {
        check(
            25,
            3,
            2654435769u,
            Triple(
                listOf(0, 24, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 22, 15, 16, 17, 18, 19, 20, 23, 14, 21, 1),
                listOf(14, 21, 1),
                listOf(0, 24, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 22, 15, 16, 17, 18, 19, 20, 23),
            ),
            1,
        )
    }

    @Test
    fun crossesChunks() {
        check(
            25,
            13,
            2654435769u,
            Triple(
                listOf(0, 24, 2, 15, 4, 5, 16, 22, 12, 9, 10, 18, 13, 21, 14, 3, 6, 23, 11, 19, 1, 17, 7, 8, 20),
                listOf(13, 21, 14, 3, 6, 23, 11, 19, 1, 17, 7, 8, 20),
                listOf(0, 24, 2, 15, 4, 5, 16, 22, 12, 9, 10, 18),
            ),
            4,
        )
    }

    @Test
    fun oversize() {
        check(
            5,
            8,
            0u,
            Triple(
                listOf(4, 0, 1, 2, 3),
                listOf(4, 0, 1, 2, 3),
                emptyList(),
            ),
            1,
        )
    }

    @Test
    fun largestAmount() {
        check(
            5,
            2147483647,
            4294967295u,
            Triple(
                listOf(0, 1, 2, 3, 4),
                listOf(0, 1, 2, 3, 4),
                emptyList(),
            ),
            1,
        )
    }

    @Test
    fun firstDrawFailure() {
        check(
            5,
            3,
            0u,
            Triple(
                listOf(0, 1, 2, 3, 4),
                null,
                null,
            ),
            1,
            1,
        )
    }

    @Test
    fun refillFailure() {
        check(
            25,
            24,
            0u,
            Triple(
                listOf(11, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24),
                null,
                null,
            ),
            2,
            2,
        )
    }

    @Test
    fun rawPayloadSnapshots() {
        val marker = IllegalStateException("unexpected failure")
        val floats = intArrayOf(0x80000000u.toInt(), 0x7fc01234, 0x7f800000, 0xff800000u.toInt()).map(Float::fromBits).toTypedArray()
        val floatRng = Words(0u, null, marker)
        val (floatSelected, floatRemainder) = floats.partialShuffle(floatRng, 2)
        assertEquals(listOf(0x80000000u.toInt(), 0x7f800000), floatSelected.map { it.toRawBits() })
        assertEquals(listOf(0xff800000u.toInt(), 0x7fc01234), floatRemainder.map { it.toRawBits() })
        assertEquals(listOf(0xff800000u.toInt(), 0x7fc01234, 0x80000000u.toInt(), 0x7f800000), floats.map { it.toRawBits() })
        assertEquals(1, floatRng.draws32)
        assertEquals(0, floatRng.draws64)
        val doubles =
            longArrayOf(0x8000000000000000uL.toLong(), 0x7ff8000000001234L, 0x7ff0000000000000L, 0xfff0000000000000uL.toLong())
                .map(Double::fromBits)
                .toTypedArray()
        val doubleRng = Words(0u, null, marker)
        val (doubleSelected, doubleRemainder) = doubles.partialShuffle(doubleRng, 2)
        assertEquals(listOf(0x8000000000000000uL.toLong(), 0x7ff0000000000000L), doubleSelected.map { it.toRawBits() })
        assertEquals(listOf(0xfff0000000000000uL.toLong(), 0x7ff8000000001234L), doubleRemainder.map { it.toRawBits() })
        assertEquals(
            listOf(0xfff0000000000000uL.toLong(), 0x7ff8000000001234L, 0x8000000000000000uL.toLong(), 0x7ff0000000000000L),
            doubles.map { it.toRawBits() },
        )
        assertEquals(1, doubleRng.draws32)
        assertEquals(0, doubleRng.draws64)
    }
}
