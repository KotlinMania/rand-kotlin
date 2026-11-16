package io.github.kotlinmania.rand

import io.github.kotlinmania.rand.distr.Result
import io.github.kotlinmania.rand.distr.weighted.WeightError
import io.github.kotlinmania.rand.seq.chooseWeighted
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame

class WeightedChoiceParityTest {
    private class Words(
        private val words: List<ULong>,
        private val failure: Exception? = null,
    ) : Rng {
        var draws = 0

        override fun nextU32(): UInt = error("unexpected u32")

        override fun nextU64(): ULong {
            val index = draws++
            failure?.let { throw it }
            return words[index]
        }

        override fun fillBytes(dest: ByteArray): Unit = error("unexpected byte draw")
    }

    private fun adapters(size: Int): List<Pair<String, (Rng, (Int) -> Double) -> Any?>> =
        listOf(
            "list" to { rng, weight -> List(size) { it }.chooseWeighted(rng, weight) },
            "array" to { rng, weight -> Array(size) { it }.chooseWeighted(rng, weight) },
            "int" to { rng, weight -> IntArray(size) { it }.chooseWeighted(rng, weight) },
            "long" to { rng, weight -> LongArray(size) { it.toLong() }.chooseWeighted(rng) { weight(it.toInt()) } },
            "double" to { rng, weight -> DoubleArray(size) { it.toDouble() }.chooseWeighted(rng) { weight(it.toInt()) } },
        )

    private fun success(weights: List<Double>, word: ULong, expected: Int) {
        for ((name, choose) in adapters(weights.size)) {
            val reads = mutableListOf<Int>()
            val rng = Words(listOf(word))
            val result =
                assertIs<Result.Ok<*>>(
                    choose(rng) { index ->
                        reads.add(index)
                        weights[index]
                    },
                    name,
                )
            assertEquals(expected, (result.value as Number).toInt(), name)
            assertEquals(weights.indices.toList(), reads, name)
            assertEquals(1, rng.draws, name)
        }
    }

    private inline fun <reified E : WeightError> failure(weights: List<Double>, expectedReads: List<Int>, poison: Int? = null) {
        for ((name, choose) in adapters(weights.size)) {
            val reads = mutableListOf<Int>()
            val rng = Words(emptyList())
            val result =
                assertIs<Result.Err<WeightError>>(
                    choose(rng) { index ->
                        reads.add(index)
                        if (index == poison) error("visited suffix")
                        weights[index]
                    },
                    name,
                )
            assertIs<E>(result.error, name)
            assertEquals(expectedReads, reads, name)
            assertEquals(0, rng.draws, name)
        }
    }

    @Test
    fun validChoicesUseCanonicalBits() {
        success(listOf(1.0, 2.0), ULong.MAX_VALUE, 1)
        success(listOf(1.0, 2.0), 0x5555555555555555uL, 0)
    }

    @Test
    fun leadingZeroWeightIsSkipped() {
        success(listOf(0.0, 1.0), 0uL, 1)
    }

    @Test
    fun emptyIsError() {
        failure<WeightError.InvalidInput>(emptyList(), emptyList())
    }

    @Test
    fun invalidPrefixStopsCallback() {
        failure<WeightError.InvalidWeight>(listOf(-1.0, 1.0), listOf(0), 1)
    }

    @Test
    fun allZeroIsError() {
        failure<WeightError.InsufficientNonZero>(listOf(0.0, 0.0), listOf(0, 1))
    }

    @Test
    fun selectedNullIsOk() {
        for (array in listOf(false, true)) {
            val rng = Words(listOf(0uL))
            var calls = 0
            val weight: (String?) -> Double = { item ->
                calls++
                if (item == null) 1.0 else 0.0
            }
            val result: Any? =
                if (array) {
                    arrayOf("0", null, "2").chooseWeighted(rng, weight)
                } else {
                    listOf("0", null, "2").chooseWeighted(rng, weight)
                }
            assertNull(assertIs<Result.Ok<*>>(result).value)
            assertEquals(3, calls)
            assertEquals(1, rng.draws)
        }
    }

    @Test
    fun infinityThenInvalidStopsCallback() {
        failure<WeightError.InvalidWeight>(listOf(1.0, Double.POSITIVE_INFINITY, -1.0, 1.0), listOf(0, 1, 2), 3)
    }

    @Test
    fun nonfiniteSamplerPanicsBeforeRng() {
        for ((name, choose) in adapters(2)) {
            val rng = Words(listOf(0uL))
            val reads = mutableListOf<Int>()
            assertFailsWith<IllegalStateException>(name) {
                choose(rng) { index ->
                    reads.add(index)
                    if (index == 0) 1.0 else Double.POSITIVE_INFINITY
                }
            }
            assertEquals(listOf(0, 1), reads, name)
            assertEquals(0, rng.draws, name)
        }
    }

    @Test
    fun weightFailurePropagates() {
        for ((name, choose) in adapters(2)) {
            val marker = WeightError.InvalidWeight("weight failure")
            val rng = Words(emptyList())
            val reads = mutableListOf<Int>()
            assertSame(
                marker,
                assertFailsWith<WeightError.InvalidWeight>(name) {
                    choose(rng) { index ->
                        reads.add(index)
                        if (index == 1) throw marker
                        1.0
                    }
                },
            )
            assertEquals(listOf(0, 1), reads, name)
            assertEquals(0, rng.draws, name)
        }
    }

    @Test
    fun rngFailurePropagates() {
        for ((name, choose) in adapters(2)) {
            val marker = IllegalStateException("rng failure")
            val rng = Words(emptyList(), marker)
            val reads = mutableListOf<Int>()
            assertSame(
                marker,
                assertFailsWith<IllegalStateException>(name) {
                    choose(rng) { index ->
                        reads.add(index)
                        1.0
                    }
                },
            )
            assertEquals(listOf(0, 1), reads, name)
            assertEquals(1, rng.draws, name)
        }
    }
}
