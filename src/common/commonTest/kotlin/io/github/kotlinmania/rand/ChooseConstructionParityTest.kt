package io.github.kotlinmania.rand

import io.github.kotlinmania.rand.distr.Choose
import io.github.kotlinmania.rand.distr.Empty
import io.github.kotlinmania.rand.distr.Result
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame

class ChooseConstructionParityTest {
    private class NoClone

    private class Words : Rng {
        var draws32 = 0
        var draws64 = 0

        override fun nextU32(): UInt {
            draws32 += 1
            return UInt.MAX_VALUE
        }

        override fun nextU64(): ULong {
            draws64 += 1
            error("unexpected 64-bit draw")
        }

        override fun fillBytes(dest: ByteArray): Unit = error("unexpected byte draw")
    }

    @Test
    fun emptyListReturnsTypedError() {
        val result = Choose.new(emptyList<Int>())
        val error = assertIs<Result.Err<Empty>>(result).error
        assertEquals("Tried to create a `rand::distr::slice::Choose` with an empty slice", error.toString())
    }

    @Test
    fun emptyArrayReturnsTypedError() {
        val result = Choose.new(emptyArray<Int>())
        val error = assertIs<Result.Err<Empty>>(result).error
        assertEquals("Tried to create a `rand::distr::slice::Choose` with an empty slice", error.toString())
    }

    @Test
    fun payloadNeedsNoCloneAndReferenceIsRetained() {
        val payload = NoClone()
        val dist: Choose<NoClone> = Choose.new(listOf(payload)).unwrap()
        val rng = Words()
        assertEquals(1, dist.numChoices)
        assertSame(payload, dist.sample(rng))
        assertEquals(1, rng.draws32)
        assertEquals(0, rng.draws64)
    }
}
