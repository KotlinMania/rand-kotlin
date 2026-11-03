package io.github.kotlinmania.rand

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RngIntegerRangeParityTest {
    private class Words(
        private val words32: List<UInt> = emptyList(),
        private val words64: List<ULong> = emptyList(),
    ) : Rng {
        var calls32 = 0
            private set
        var calls64 = 0
            private set
        var fills = 0
            private set

        override fun nextU32(): UInt {
            val index = calls32++
            check(index < words32.size) { "unexpected 32-bit draw" }
            return words32[index]
        }

        override fun nextU64(): ULong {
            val index = calls64++
            check(index < words64.size) { "unexpected 64-bit draw" }
            return words64[index]
        }

        override fun fillBytes(dest: ByteArray) {
            fills++
            error("range sampling must use word draws")
        }
    }

    private fun <T> assertDraw(
        rng: Words,
        expected: T,
        calls32: Int,
        calls64: Int,
        sample: (Rng) -> T,
    ) {
        assertEquals(expected, sample(rng))
        assertEquals(calls32, rng.calls32)
        assertEquals(calls64, rng.calls64)
        assertEquals(0, rng.fills)
    }

    @Test
    fun intRangeUsesThe32BitUniformSampler() {
        assertDraw(Words(listOf(0x80000000u)), 0, 1, 0) { it.randomRange(-10..10) }
    }

    @Test
    fun singletonIntRangeStillConsumesOneWord() {
        assertDraw(Words(listOf(42u)), 7, 1, 0) { it.randomRange(7..7) }
    }

    @Test
    fun fullIntDomainPreservesAllWordBits() {
        assertDraw(Words(listOf(0x80000000u)), Int.MIN_VALUE, 1, 0) {
            it.randomRange(Int.MIN_VALUE..Int.MAX_VALUE)
        }
    }

    @Test
    fun intRangePreservesTheCanonicalCorrectionDraw() {
        assertDraw(Words(listOf(1u, UInt.MAX_VALUE)), 0, 2, 0) { it.randomRange(-1..Int.MAX_VALUE) }
    }

    @Test
    fun emptyIntRangePanicsBeforeUsingTheRng() {
        val rng = Words()
        assertFailsWith<IllegalArgumentException> { rng.randomRange(IntRange.EMPTY) }
        assertEquals(0, rng.calls32)
        assertEquals(0, rng.calls64)
        assertEquals(0, rng.fills)
    }

    @Test
    fun longRangeUsesThe64BitUniformSampler() {
        assertDraw(Words(words64 = listOf(0x8000000000000000uL)), 0L, 0, 1) {
            it.randomRange(-10L..10L)
        }
    }

    @Test
    fun unsignedIntRangeUsesThe32BitUniformSampler() {
        assertDraw(Words(listOf(0x80000000u)), 105u, 1, 0) { it.randomRange(100u..110u) }
    }

    @Test
    fun unsignedLongRangePreservesLargeEndpoints() {
        assertDraw(Words(words64 = listOf(0x8000000000000000uL)), ULong.MAX_VALUE - 5uL, 0, 1) {
            it.randomRange((ULong.MAX_VALUE - 10uL)..ULong.MAX_VALUE)
        }
    }

    @Test
    fun otherSingletonDomainsStillConsumeOneWord() {
        assertDraw(Words(words64 = listOf(42uL)), -7L, 0, 1) { it.randomRange(-7L..-7L) }
        assertDraw(Words(listOf(42u)), UInt.MAX_VALUE, 1, 0) { it.randomRange(UInt.MAX_VALUE..UInt.MAX_VALUE) }
        assertDraw(Words(words64 = listOf(42uL)), ULong.MAX_VALUE, 0, 1) {
            it.randomRange(ULong.MAX_VALUE..ULong.MAX_VALUE)
        }
    }

    @Test
    fun otherFullDomainsPreserveAllWordBits() {
        assertDraw(Words(words64 = listOf(0x8000000000000000uL)), Long.MIN_VALUE, 0, 1) {
            it.randomRange(Long.MIN_VALUE..Long.MAX_VALUE)
        }
        assertDraw(Words(listOf(0x80000000u)), 0x80000000u, 1, 0) { it.randomRange(0u..UInt.MAX_VALUE) }
        assertDraw(Words(words64 = listOf(0x8000000000000000uL)), 0x8000000000000000uL, 0, 1) {
            it.randomRange(0uL..ULong.MAX_VALUE)
        }
    }

    @Test
    fun inclusiveEndpointsRemainReachable() {
        assertDraw(Words(listOf(0u)), -10, 1, 0) { it.randomRange(-10..10) }
        assertDraw(Words(listOf(UInt.MAX_VALUE)), 10, 1, 0) { it.randomRange(-10..10) }
        assertDraw(Words(words64 = listOf(0uL)), -10L, 0, 1) { it.randomRange(-10L..10L) }
        assertDraw(Words(words64 = listOf(ULong.MAX_VALUE)), 10L, 0, 1) { it.randomRange(-10L..10L) }
        assertDraw(Words(listOf(0u)), 100u, 1, 0) { it.randomRange(100u..110u) }
        assertDraw(Words(listOf(UInt.MAX_VALUE)), 110u, 1, 0) { it.randomRange(100u..110u) }
        assertDraw(Words(words64 = listOf(0uL)), 100uL, 0, 1) { it.randomRange(100uL..110uL) }
        assertDraw(Words(words64 = listOf(ULong.MAX_VALUE)), 110uL, 0, 1) { it.randomRange(100uL..110uL) }
    }

    @Test
    fun otherDomainsPreserveTheCanonicalCorrectionDraw() {
        assertDraw(Words(words64 = listOf(1uL, ULong.MAX_VALUE)), 0L, 0, 2) {
            it.randomRange(-1L..Long.MAX_VALUE)
        }
        assertDraw(Words(listOf(1u, UInt.MAX_VALUE)), 1u, 2, 0) { it.randomRange(0u..0x80000000u) }
        assertDraw(Words(words64 = listOf(1uL, ULong.MAX_VALUE)), 1uL, 0, 2) {
            it.randomRange(0uL..0x8000000000000000uL)
        }
        assertDraw(Words(listOf(1u, 0u)), 0u, 2, 0) { it.randomRange(0u..0x80000000u) }
        assertDraw(Words(words64 = listOf(1uL, 0uL)), 0uL, 0, 2) {
            it.randomRange(0uL..0x8000000000000000uL)
        }
    }

    @Test
    fun otherEmptyDomainsPanicBeforeUsingTheRng() {
        val rng = Words()
        assertFailsWith<IllegalArgumentException> { rng.randomRange(LongRange.EMPTY) }
        assertFailsWith<IllegalArgumentException> { rng.randomRange(UIntRange.EMPTY) }
        assertFailsWith<IllegalArgumentException> { rng.randomRange(ULongRange.EMPTY) }
        assertEquals(0, rng.calls32)
        assertEquals(0, rng.calls64)
        assertEquals(0, rng.fills)
    }
}
