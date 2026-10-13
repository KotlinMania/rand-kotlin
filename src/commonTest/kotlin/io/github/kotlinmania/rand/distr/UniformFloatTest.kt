// port-lint: source distr/uniform_float.rs
package io.github.kotlinmania.rand.distr

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.StepRng
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class UniformFloatTest {
    private class Pcg32(
        private var state: ULong,
        private val inc: ULong = 1uL,
    ) : Rng {
        override fun nextU32(): UInt {
            val oldState = state
            state = oldState * 6364136223846793005uL + (inc or 1uL)
            val xorShifted = (((oldState shr 18) xor oldState) shr 27).toUInt()
            val rot = (oldState shr 59).toInt()
            return (xorShifted shr rot) or (xorShifted shl ((-rot) and 31))
        }

        override fun nextU64(): ULong {
            val hi = nextU32().toULong()
            val lo = nextU32().toULong()
            return (hi shl 32) or lo
        }

        override fun fillBytes(dest: ByteArray) {
            var i = 0
            while (i < dest.size) {
                val v = nextU32()
                for (b in 0 until 4) {
                    if (i < dest.size) {
                        dest[i++] = (v shr (b * 8)).toByte()
                    }
                }
            }
        }
    }

    private fun testRng(seed: ULong): Rng = Pcg32(seed, 1uL)

    private fun constRng(v: ULong): Rng = StepRng(v, 0uL)

    private fun range(low: Float, high: Float): Result<Float, Error> {
        val rng = testRng(253uL)
        return UniformFloat.sampleSingle(low, high, rng)
    }

    private fun range(low: Double, high: Double): Result<Double, Error> {
        val rng = testRng(253uL)
        return UniformFloat.sampleSingle(low, high, rng)
    }

    @Test
    fun testFloats() {
        val rng = testRng(252uL)
        val zeroRng = constRng(0uL)
        val maxRng = constRng(0xFFFF_FFFF_FFFF_FFFFuL)

        // Float (32-bit, bitsShifted = 9)
        val vFloat: List<Pair<Float, Float>> =
            listOf(
                0.0f to 100.0f,
                -1e35f to -1e25f,
                1e-35f to 1e-25f,
                -1e35f to 1e35f,
                Float.fromBits(0) to Float.fromBits(3),
                -Float.fromBits(10) to -Float.fromBits(1),
                -Float.fromBits(5) to 0.0f,
                -Float.fromBits(7) to -0.0f,
                0.1f * Float.MAX_VALUE to Float.MAX_VALUE,
                -Float.MAX_VALUE * 0.2f to Float.MAX_VALUE * 0.7f,
            )
        for ((low, high) in vFloat) {
            val myUniform = UniformFloat.new(low, high).unwrap()
            val myInclUniform = UniformFloat.newInclusive(low, high).unwrap()
            for (i in 0 until 100) {
                val v = myUniform.sample(rng)
                assertTrue(low <= v && v <= high)
                val vIncl = myInclUniform.sample(rng)
                assertTrue(low <= vIncl && vIncl <= high)
                val vSingle = UniformFloat.sampleSingle(low, high, rng).unwrap()
                assertTrue(low <= vSingle && vSingle <= high)
                val vSingleIncl = UniformFloat.sampleSingleInclusive(low, high, rng).unwrap()
                assertTrue(low <= vSingleIncl && vSingleIncl <= high)
            }
            assertEquals(low, UniformFloat.newInclusive(low, low).unwrap().sample(rng))
            assertEquals(low, myUniform.sample(zeroRng))
            assertEquals(low, myInclUniform.sample(zeroRng))
            assertEquals(low, UniformFloat.sampleSingle(low, high, zeroRng).unwrap())
            assertEquals(low, UniformFloat.sampleSingleInclusive(low, high, zeroRng).unwrap())
            assertTrue(myUniform.sample(maxRng) <= high)
            assertTrue(myInclUniform.sample(maxRng) <= high)
            assertTrue(UniformFloat.sampleSingleInclusive(low, high, maxRng).unwrap() <= high)

            if ((high - low) > 0.0001f) {
                val loweringMaxRng = StepRng(0xFFFF_FFFF_FFFF_FFFFuL, (-1L shl 9).toULong())
                assertTrue(UniformFloat.sampleSingle(low, high, loweringMaxRng).unwrap() <= high)
            }
        }

        // Double (64-bit, bitsShifted = 12)
        val vDouble: List<Pair<Double, Double>> =
            listOf(
                0.0 to 100.0,
                -1e35 to -1e25,
                1e-35 to 1e-25,
                -1e35 to 1e35,
                Double.fromBits(0L) to Double.fromBits(3L),
                -Double.fromBits(10L) to -Double.fromBits(1L),
                -Double.fromBits(5L) to 0.0,
                -Double.fromBits(7L) to -0.0,
                0.1 * Double.MAX_VALUE to Double.MAX_VALUE,
                -Double.MAX_VALUE * 0.2 to Double.MAX_VALUE * 0.7,
            )
        for ((low, high) in vDouble) {
            val myUniform = UniformFloat.new(low, high).unwrap()
            val myInclUniform = UniformFloat.newInclusive(low, high).unwrap()
            for (i in 0 until 100) {
                val v = myUniform.sample(rng)
                assertTrue(low <= v && v <= high)
                val vIncl = myInclUniform.sample(rng)
                assertTrue(low <= vIncl && vIncl <= high)
                val vSingle = UniformFloat.sampleSingle(low, high, rng).unwrap()
                assertTrue(low <= vSingle && vSingle <= high)
                val vSingleIncl = UniformFloat.sampleSingleInclusive(low, high, rng).unwrap()
                assertTrue(low <= vSingleIncl && vSingleIncl <= high)
            }
            assertEquals(low, UniformFloat.newInclusive(low, low).unwrap().sample(rng))
            assertEquals(low, myUniform.sample(zeroRng))
            assertEquals(low, myInclUniform.sample(zeroRng))
            assertEquals(low, UniformFloat.sampleSingle(low, high, zeroRng).unwrap())
            assertEquals(low, UniformFloat.sampleSingleInclusive(low, high, zeroRng).unwrap())
            assertTrue(myUniform.sample(maxRng) <= high)
            assertTrue(myInclUniform.sample(maxRng) <= high)
            assertTrue(UniformFloat.sampleSingleInclusive(low, high, maxRng).unwrap() <= high)

            if ((high - low) > 0.0001) {
                val loweringMaxRng = StepRng(0xFFFF_FFFF_FFFF_FFFFuL, (-1L shl 12).toULong())
                assertTrue(UniformFloat.sampleSingle(low, high, loweringMaxRng).unwrap() <= high)
            }
        }

        val fMax = Float.MAX_VALUE
        assertEquals(fMax, UniformFloat.newInclusive(fMax, fMax).unwrap().sample(rng))
        assertEquals(-fMax, UniformFloat.newInclusive(-fMax, -fMax).unwrap().sample(rng))

        val dMax = Double.MAX_VALUE
        assertEquals(dMax, UniformFloat.newInclusive(dMax, dMax).unwrap().sample(rng))
        assertEquals(-dMax, UniformFloat.newInclusive(-dMax, -dMax).unwrap().sample(rng))
    }

    @Test
    fun testFloatOverflow() {
        assertEquals(Result.Err(Error.NonFinite), Uniform.tryFromOpenEnd(-Double.MAX_VALUE..<Double.MAX_VALUE))
    }

    @Test
    fun testFloatOverflowSingle() {
        val rng = testRng(252uL)
        assertFails {
            rng.randomRange(-Double.MAX_VALUE..Double.MAX_VALUE)
        }
    }

    @Test
    fun testFloatAssertions() {
        val vFloat: List<Pair<Float, Float>> =
            listOf(
                Float.NaN to 0.0f,
                1.0f to Float.NaN,
                Float.NaN to Float.NaN,
                1.0f to 0.5f,
                Float.MAX_VALUE to -Float.MAX_VALUE,
                Float.POSITIVE_INFINITY to Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY to Float.NEGATIVE_INFINITY,
                Float.NEGATIVE_INFINITY to 5.0f,
                5.0f to Float.POSITIVE_INFINITY,
                Float.NaN to Float.POSITIVE_INFINITY,
                Float.NEGATIVE_INFINITY to Float.NaN,
                Float.NEGATIVE_INFINITY to Float.POSITIVE_INFINITY,
            )
        for ((low, high) in vFloat) {
            assertTrue(range(low, high).isErr)
            assertTrue(UniformFloat.new(low, high).isErr)
            assertTrue(UniformFloat.newInclusive(low, high).isErr)
            assertTrue(UniformFloat.new(low, low).isErr)
        }

        val vDouble: List<Pair<Double, Double>> =
            listOf(
                Double.NaN to 0.0,
                1.0 to Double.NaN,
                Double.NaN to Double.NaN,
                1.0 to 0.5,
                Double.MAX_VALUE to -Double.MAX_VALUE,
                Double.POSITIVE_INFINITY to Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY to Double.NEGATIVE_INFINITY,
                Double.NEGATIVE_INFINITY to 5.0,
                5.0 to Double.POSITIVE_INFINITY,
                Double.NaN to Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY to Double.NaN,
                Double.NEGATIVE_INFINITY to Double.POSITIVE_INFINITY,
            )
        for ((low, high) in vDouble) {
            assertTrue(range(low, high).isErr)
            assertTrue(UniformFloat.new(low, high).isErr)
            assertTrue(UniformFloat.newInclusive(low, high).isErr)
            assertTrue(UniformFloat.new(low, low).isErr)
        }
    }

    @Test
    fun testUniformFromStdRange() {
        val r = Uniform.tryFromOpenEnd(2.0..<7.0).unwrap()
        val s = r.sampler as UniformFloat<Double>
        assertEquals(2.0, s.low)
        assertEquals(5.0, s.scale)
    }

    @Test
    @Suppress("InvalidRange")
    fun testUniformFromStdRangeBadLimits() {
        assertTrue(Uniform.tryFromOpenEnd(100.0..<10.0).isErr)
        assertTrue(Uniform.tryFromOpenEnd(100.0..<100.0).isErr)
    }

    @Test
    fun testUniformFromStdRangeInclusive() {
        val r = Uniform.tryFrom(2.0..7.0).unwrap()
        val s = r.sampler as UniformFloat<Double>
        assertEquals(2.0, s.low)
        assertTrue(s.scale > 5.0)
        assertTrue(s.scale < 5.0 + 1e-14)
    }

    @Test
    @Suppress("InvalidRange")
    fun testUniformFromStdRangeInclusiveBadLimits() {
        assertTrue(Uniform.tryFrom(100.0..10.0).isErr)
        assertTrue(Uniform.tryFrom(100.0..99.0).isErr)
    }
}
