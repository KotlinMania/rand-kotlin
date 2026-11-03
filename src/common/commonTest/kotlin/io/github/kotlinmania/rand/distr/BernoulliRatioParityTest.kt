package io.github.kotlinmania.rand.distr

import io.github.kotlinmania.rand.Rng
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BernoulliRatioParityTest {
    private class Words(
        val word: ULong,
        val fail: Boolean = false,
    ) : Rng {
        var draws = 0

        override fun nextU32(): UInt = error("unexpected 32-bit draw")

        override fun nextU64(): ULong {
            draws += 1
            check(!fail) { "rng failure" }
            return word
        }

        override fun fillBytes(dest: ByteArray): Unit = error("unexpected byte draw")
    }

    private data class Case(
        val numerator: UInt,
        val denominator: UInt,
        val probabilityBits: ULong,
        val values: List<Boolean>,
        val draws: List<Int>,
    )

    private fun distribution(numerator: UInt, denominator: UInt): Bernoulli {
        val result: Any = Bernoulli.fromRatio(numerator, denominator)
        return assertIs<Result.Ok<Bernoulli>>(result).value
    }

    private val words = listOf(0uL, 1uL, 4294967296uL, 4294967297uL, 6148914691236516863uL, 6148914691236516864uL, 6148914691236516865uL, 9223372036854775807uL, 9223372036854775808uL, 9223372036854775809uL, 12297829382473033727uL, 12297829382473033728uL, 12297829382473033729uL, 18446744069414584319uL, 18446744069414584320uL, 18446744069414584321uL, 18446744073709551615uL)

    private val cases =
        listOf(
            Case(0u, 1u, 0uL, listOf(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false), listOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)),
            Case(1u, 1u, 4607182418800017408uL, listOf(true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true), listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)),
            Case(1u, 2u, 4602678819172646912uL, listOf(true, true, true, true, true, true, true, true, false, false, false, false, false, false, false, false, false), listOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)),
            Case(1u, 3u, 4599676419421066581uL, listOf(true, true, true, true, true, false, false, false, false, false, false, false, false, false, false, false, false), listOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)),
            Case(2u, 3u, 4604180019048437077uL, listOf(true, true, true, true, true, true, true, true, true, true, true, false, false, false, false, false, false), listOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)),
            Case(1u, 4294967295u, 4463067230725210112uL, listOf(true, true, true, false, false, false, false, false, false, false, false, false, false, false, false, false, false), listOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)),
            Case(4294967294u, 4294967295u, 4607182418797920256uL, listOf(true, true, true, true, true, true, true, true, true, true, true, true, true, true, false, false, false), listOf(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)),
            Case(4294967295u, 4294967295u, 4607182418800017408uL, listOf(true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true), listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)),
        )

    private val invalid = listOf(0u to 0u, 1u to 0u, 2u to 1u, UInt.MAX_VALUE to UInt.MAX_VALUE - 1u)

    @Test
    fun invalidRatiosAreTypedErrors() {
        for ((numerator, denominator) in invalid) {
            val result: Any = Bernoulli.fromRatio(numerator, denominator)
            val error = assertIs<Result.Err<*>>(result).error
            assertEquals("InvalidProbability", assertIs<Enum<*>>(error).name)
            assertEquals("p is outside [0, 1] in Bernoulli distribution", error.toString())
        }
    }

    @Test
    fun directDistributionMatchesRustBitsAndDraws() {
        for (case in cases) {
            val dist = distribution(case.numerator, case.denominator)
            assertEquals(case.probabilityBits, dist.p().toBits().toULong())
            for ((index, word) in words.withIndex()) {
                val rng = Words(word)
                assertEquals(case.values[index], dist.sample(rng))
                assertEquals(case.draws[index], rng.draws)
            }
        }
    }

    @Test
    fun publicWrapperMatchesRustMatrixAndDraws() {
        for (case in cases) {
            for ((index, word) in words.withIndex()) {
                val rng = Words(word)
                assertEquals(case.values[index], rng.randomRatio(case.numerator, case.denominator))
                assertEquals(case.draws[index], rng.draws)
            }
        }
    }

    @Test
    fun publicInvalidRatioPanicsBeforeDraw() {
        for ((numerator, denominator) in invalid) {
            val rng = Words(0uL)
            val failure = assertFailsWith<IllegalStateException> { rng.randomRatio(numerator, denominator) }
            assertEquals("p=$numerator/$denominator is outside range [0.0, 1.0]", failure.message)
            assertEquals(0, rng.draws)
        }
    }

    @Test
    fun onlyProbabilityOneBypassesFailingRng() {
        val direct = Words(0uL, true)
        assertTrue(distribution(1u, 1u).sample(direct))
        assertEquals(0, direct.draws)
        val wrapped = Words(0uL, true)
        assertTrue(wrapped.randomRatio(UInt.MAX_VALUE, UInt.MAX_VALUE))
        assertEquals(0, wrapped.draws)
        val directFailure = assertFailsWith<IllegalStateException> { distribution(0u, 1u).sample(direct) }
        assertEquals("rng failure", directFailure.message)
        assertEquals(1, direct.draws)
        val wrappedFailure = assertFailsWith<IllegalStateException> { wrapped.randomRatio(0u, 1u) }
        assertEquals("rng failure", wrappedFailure.message)
        assertEquals(1, wrapped.draws)
    }
}
