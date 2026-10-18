// port-lint: tests distr/uniform_int.rs
package io.github.kotlinmania.rand.distr

import io.github.kotlinmania.rand.Rng
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UniformIntTest {
    /**
     * Deterministic PCG32 generator matching upstream `crate::test::rng(seed)`.
     */
    private class Pcg32(
        seed: ULong,
        stream: ULong = 11634580027462260723uL,
    ) : Rng {
        private val inc: ULong = (stream shl 1) or 1uL
        private var state: ULong = 0uL

        init {
            step()
            state += seed
            step()
        }

        private fun step() {
            state = state * 6364136223846793005uL + inc
        }

        override fun nextU32(): UInt {
            val old = state
            step()
            val xsh = (((old shr 18) xor old) shr 27).toUInt()
            val rot = (old shr 59).toInt()
            return (xsh shr rot) or (xsh shl ((-rot) and 31))
        }

        override fun nextU64(): ULong {
            val lo = nextU32().toULong()
            val hi = nextU32().toULong()
            return (hi shl 32) or lo
        }

        override fun fillBytes(dest: ByteArray) {
            var i = 0
            while (i < dest.size) {
                val u = nextU32()
                val rem = dest.size - i
                dest[i] = u.toByte()
                if (rem > 1) dest[i + 1] = (u shr 8).toByte()
                if (rem > 2) dest[i + 2] = (u shr 16).toByte()
                if (rem > 3) dest[i + 3] = (u shr 24).toByte()
                i += 4
            }
        }
    }

    private fun testRng(seed: ULong): Rng = Pcg32(seed)

    @Test
    fun testUniformBadLimitsEqualInt() {
        assertEquals(Err(Error.EmptyRange), Uniform.new(10, 10))
        assertEquals(Err(Error.EmptyRange), UniformInt.new(10, 10))
    }

    @Test
    fun testUniformGoodLimitsEqualInt() {
        val rng = testRng(804uL)
        val dist = Uniform.newInclusive(10, 10).unwrap()
        repeat(20) {
            assertEquals(10, rng.sample(dist))
        }
    }

    @Test
    fun testUniformBadLimitsFlippedInt() {
        assertEquals(Err(Error.EmptyRange), Uniform.new(10, 5))
        assertEquals(Err(Error.EmptyRange), UniformInt.new(10, 5))
    }

    @Test
    fun testIntegers() {
        val rng = testRng(251uL)

        // Int
        val intRanges = listOf(0 to 10, 10 to 127, Int.MIN_VALUE to Int.MAX_VALUE)
        for ((low, high) in intRanges) {
            if (low < high) {
                val dist = UniformInt.new(low, high).unwrap()
                repeat(100) {
                    val v = dist.sample(rng)
                    assertTrue(v in low until high)
                }
                repeat(100) {
                    val v = UniformInt.sampleSingle(low, high, rng).unwrap()
                    assertTrue(v in low until high)
                }
            }
            val distInc = UniformInt.newInclusive(low, high).unwrap()
            repeat(100) {
                val v = distInc.sample(rng)
                assertTrue(v in low..high)
            }
            repeat(100) {
                val v = UniformInt.sampleSingleInclusive(low, high, rng).unwrap()
                assertTrue(v in low..high)
            }
        }

        // Long
        val longRanges = listOf(0L to 10L, 10L to 127L, Long.MIN_VALUE to Long.MAX_VALUE)
        for ((low, high) in longRanges) {
            if (low < high) {
                val dist = UniformInt.new(low, high).unwrap()
                repeat(100) {
                    val v = dist.sample(rng)
                    assertTrue(v in low until high)
                }
                repeat(100) {
                    val v = UniformInt.sampleSingle(low, high, rng).unwrap()
                    assertTrue(v in low until high)
                }
            }
            val distInc = UniformInt.newInclusive(low, high).unwrap()
            repeat(100) {
                val v = distInc.sample(rng)
                assertTrue(v in low..high)
            }
            repeat(100) {
                val v = UniformInt.sampleSingleInclusive(low, high, rng).unwrap()
                assertTrue(v in low..high)
            }
        }

        // UInt
        val uintRanges = listOf(0u to 10u, 10u to 127u, UInt.MIN_VALUE to UInt.MAX_VALUE)
        for ((low, high) in uintRanges) {
            if (low < high) {
                val dist = UniformInt.new(low, high).unwrap()
                repeat(100) {
                    val v = dist.sample(rng)
                    assertTrue(v in low until high)
                }
                repeat(100) {
                    val v = UniformInt.sampleSingle(low, high, rng).unwrap()
                    assertTrue(v in low until high)
                }
            }
            val distInc = UniformInt.newInclusive(low, high).unwrap()
            repeat(100) {
                val v = distInc.sample(rng)
                assertTrue(v in low..high)
            }
            repeat(100) {
                val v = UniformInt.sampleSingleInclusive(low, high, rng).unwrap()
                assertTrue(v in low..high)
            }
        }

        // ULong
        val ulongRanges = listOf(0uL to 10uL, 10uL to 127uL, ULong.MIN_VALUE to ULong.MAX_VALUE)
        for ((low, high) in ulongRanges) {
            if (low < high) {
                val dist = UniformInt.new(low, high).unwrap()
                repeat(100) {
                    val v = dist.sample(rng)
                    assertTrue(v in low until high)
                }
                repeat(100) {
                    val v = UniformInt.sampleSingle(low, high, rng).unwrap()
                    assertTrue(v in low until high)
                }
            }
            val distInc = UniformInt.newInclusive(low, high).unwrap()
            repeat(100) {
                val v = distInc.sample(rng)
                assertTrue(v in low..high)
            }
            repeat(100) {
                val v = UniformInt.sampleSingleInclusive(low, high, rng).unwrap()
                assertTrue(v in low..high)
            }
        }

        // Byte
        val byteRanges = listOf(0.toByte() to 10.toByte(), 10.toByte() to 127.toByte(), Byte.MIN_VALUE to Byte.MAX_VALUE)
        for ((low, high) in byteRanges) {
            if (low < high) {
                val dist = UniformInt.new(low, high).unwrap()
                repeat(100) {
                    val v = dist.sample(rng)
                    assertTrue(v in low until high)
                }
                repeat(100) {
                    val v = UniformInt.sampleSingle(low, high, rng).unwrap()
                    assertTrue(v in low until high)
                }
            }
            val distInc = UniformInt.newInclusive(low, high).unwrap()
            repeat(100) {
                val v = distInc.sample(rng)
                assertTrue(v in low..high)
            }
            repeat(100) {
                val v = UniformInt.sampleSingleInclusive(low, high, rng).unwrap()
                assertTrue(v in low..high)
            }
        }

        // Short
        val shortRanges = listOf(0.toShort() to 10.toShort(), 10.toShort() to 127.toShort(), Short.MIN_VALUE to Short.MAX_VALUE)
        for ((low, high) in shortRanges) {
            if (low < high) {
                val dist = UniformInt.new(low, high).unwrap()
                repeat(100) {
                    val v = dist.sample(rng)
                    assertTrue(v in low until high)
                }
                repeat(100) {
                    val v = UniformInt.sampleSingle(low, high, rng).unwrap()
                    assertTrue(v in low until high)
                }
            }
            val distInc = UniformInt.newInclusive(low, high).unwrap()
            repeat(100) {
                val v = distInc.sample(rng)
                assertTrue(v in low..high)
            }
            repeat(100) {
                val v = UniformInt.sampleSingleInclusive(low, high, rng).unwrap()
                assertTrue(v in low..high)
            }
        }
    }

    @Test
    fun testUniformFromStdRange() {
        val r = UniformInt.new(2u, 7u).unwrap()
        assertEquals(2u, r.low)
        assertEquals(5u, r.range)
    }

    @Test
    fun testUniformFromStdRangeBadLimits() {
        val low = 100
        val high10 = 10
        val high100 = 100
        assertTrue(Uniform.tryFromOpenEndInt(low..<high10).isErr)
        assertTrue(Uniform.tryFromOpenEndInt(low..<high100).isErr)
    }

    @Test
    fun testUniformFromStdRangeInclusive() {
        val r = UniformInt.newInclusive(2u, 6u).unwrap()
        assertEquals(2u, r.low)
        assertEquals(5u, r.range)
    }

    @Test
    fun testUniformFromStdRangeInclusiveBadLimits() {
        val low = 100
        val high10 = 10
        val high99 = 99
        assertTrue(Uniform.tryFrom(low..high10).isErr)
        assertTrue(Uniform.tryFrom(low..high99).isErr)
    }

    fun testSamples(
        lb: Byte,
        ub: Byte,
        ubExcl: Byte,
        expected: ByteArray,
    ) {
        val rng1 = testRng(897uL)
        val buf = ByteArray(6)
        for (i in 0 until 3) {
            buf[i] = UniformInt.sampleSingleInclusive(lb, ub, rng1).unwrap()
        }
        val distr = UniformInt.newInclusive(lb, ub).unwrap()
        for (i in 3 until 6) {
            buf[i] = distr.sample(rng1)
        }
        assertEquals(expected.toList(), buf.toList())

        val rng2 = testRng(897uL)
        for (i in 0 until 3) {
            buf[i] = UniformInt.sampleSingle(lb, ubExcl, rng2).unwrap()
        }
        val distrExcl = UniformInt.new(lb, ubExcl).unwrap()
        for (i in 3 until 6) {
            buf[i] = distrExcl.sample(rng2)
        }
        assertEquals(expected.toList(), buf.toList())
    }

    fun testSamples(
        lb: Short,
        ub: Short,
        ubExcl: Short,
        expected: ShortArray,
    ) {
        val rng1 = testRng(897uL)
        val buf = ShortArray(6)
        for (i in 0 until 3) {
            buf[i] = UniformInt.sampleSingleInclusive(lb, ub, rng1).unwrap()
        }
        val distr = UniformInt.newInclusive(lb, ub).unwrap()
        for (i in 3 until 6) {
            buf[i] = distr.sample(rng1)
        }
        assertEquals(expected.toList(), buf.toList())

        val rng2 = testRng(897uL)
        for (i in 0 until 3) {
            buf[i] = UniformInt.sampleSingle(lb, ubExcl, rng2).unwrap()
        }
        val distrExcl = UniformInt.new(lb, ubExcl).unwrap()
        for (i in 3 until 6) {
            buf[i] = distrExcl.sample(rng2)
        }
        assertEquals(expected.toList(), buf.toList())
    }

    fun testSamples(
        lb: Int,
        ub: Int,
        ubExcl: Int,
        expected: IntArray,
    ) {
        val rng1 = testRng(897uL)
        val buf = IntArray(6)
        for (i in 0 until 3) {
            buf[i] = UniformInt.sampleSingleInclusive(lb, ub, rng1).unwrap()
        }
        val distr = UniformInt.newInclusive(lb, ub).unwrap()
        for (i in 3 until 6) {
            buf[i] = distr.sample(rng1)
        }
        assertEquals(expected.toList(), buf.toList())

        val rng2 = testRng(897uL)
        for (i in 0 until 3) {
            buf[i] = UniformInt.sampleSingle(lb, ubExcl, rng2).unwrap()
        }
        val distrExcl = UniformInt.new(lb, ubExcl).unwrap()
        for (i in 3 until 6) {
            buf[i] = distrExcl.sample(rng2)
        }
        assertEquals(expected.toList(), buf.toList())
    }

    fun testSamples(
        lb: Long,
        ub: Long,
        ubExcl: Long,
        expected: LongArray,
    ) {
        val rng1 = testRng(897uL)
        val buf = LongArray(6)
        for (i in 0 until 3) {
            buf[i] = UniformInt.sampleSingleInclusive(lb, ub, rng1).unwrap()
        }
        val distr = UniformInt.newInclusive(lb, ub).unwrap()
        for (i in 3 until 6) {
            buf[i] = distr.sample(rng1)
        }
        assertEquals(expected.toList(), buf.toList())

        val rng2 = testRng(897uL)
        for (i in 0 until 3) {
            buf[i] = UniformInt.sampleSingle(lb, ubExcl, rng2).unwrap()
        }
        val distrExcl = UniformInt.new(lb, ubExcl).unwrap()
        for (i in 3 until 6) {
            buf[i] = distrExcl.sample(rng2)
        }
        assertEquals(expected.toList(), buf.toList())
    }

    fun testSamples(
        lb: UByte,
        ub: UByte,
        ubExcl: UByte,
        expected: UByteArray,
    ) {
        val rng1 = testRng(897uL)
        val buf = UByteArray(6)
        for (i in 0 until 3) {
            buf[i] = UniformInt.sampleSingleInclusive(lb, ub, rng1).unwrap()
        }
        val distr = UniformInt.newInclusive(lb, ub).unwrap()
        for (i in 3 until 6) {
            buf[i] = distr.sample(rng1)
        }
        assertEquals(expected.toList(), buf.toList())

        val rng2 = testRng(897uL)
        for (i in 0 until 3) {
            buf[i] = UniformInt.sampleSingle(lb, ubExcl, rng2).unwrap()
        }
        val distrExcl = UniformInt.new(lb, ubExcl).unwrap()
        for (i in 3 until 6) {
            buf[i] = distrExcl.sample(rng2)
        }
        assertEquals(expected.toList(), buf.toList())
    }

    fun testSamples(
        lb: UShort,
        ub: UShort,
        ubExcl: UShort,
        expected: UShortArray,
    ) {
        val rng1 = testRng(897uL)
        val buf = UShortArray(6)
        for (i in 0 until 3) {
            buf[i] = UniformInt.sampleSingleInclusive(lb, ub, rng1).unwrap()
        }
        val distr = UniformInt.newInclusive(lb, ub).unwrap()
        for (i in 3 until 6) {
            buf[i] = distr.sample(rng1)
        }
        assertEquals(expected.toList(), buf.toList())

        val rng2 = testRng(897uL)
        for (i in 0 until 3) {
            buf[i] = UniformInt.sampleSingle(lb, ubExcl, rng2).unwrap()
        }
        val distrExcl = UniformInt.new(lb, ubExcl).unwrap()
        for (i in 3 until 6) {
            buf[i] = distrExcl.sample(rng2)
        }
        assertEquals(expected.toList(), buf.toList())
    }

    fun testSamples(
        lb: UInt,
        ub: UInt,
        ubExcl: UInt,
        expected: UIntArray,
    ) {
        val rng1 = testRng(897uL)
        val buf = UIntArray(6)
        for (i in 0 until 3) {
            buf[i] = UniformInt.sampleSingleInclusive(lb, ub, rng1).unwrap()
        }
        val distr = UniformInt.newInclusive(lb, ub).unwrap()
        for (i in 3 until 6) {
            buf[i] = distr.sample(rng1)
        }
        assertEquals(expected.toList(), buf.toList())

        val rng2 = testRng(897uL)
        for (i in 0 until 3) {
            buf[i] = UniformInt.sampleSingle(lb, ubExcl, rng2).unwrap()
        }
        val distrExcl = UniformInt.new(lb, ubExcl).unwrap()
        for (i in 3 until 6) {
            buf[i] = distrExcl.sample(rng2)
        }
        assertEquals(expected.toList(), buf.toList())
    }

    fun testSamples(
        lb: ULong,
        ub: ULong,
        ubExcl: ULong,
        expected: ULongArray,
    ) {
        val rng1 = testRng(897uL)
        val buf = ULongArray(6)
        for (i in 0 until 3) {
            buf[i] = UniformInt.sampleSingleInclusive(lb, ub, rng1).unwrap()
        }
        val distr = UniformInt.newInclusive(lb, ub).unwrap()
        for (i in 3 until 6) {
            buf[i] = distr.sample(rng1)
        }
        assertEquals(expected.toList(), buf.toList())

        val rng2 = testRng(897uL)
        for (i in 0 until 3) {
            buf[i] = UniformInt.sampleSingle(lb, ubExcl, rng2).unwrap()
        }
        val distrExcl = UniformInt.new(lb, ubExcl).unwrap()
        for (i in 3 until 6) {
            buf[i] = distrExcl.sample(rng2)
        }
        assertEquals(expected.toList(), buf.toList())
    }

    fun testSamplesUsize(
        lb: ULong,
        ub: ULong,
        ubExcl: ULong,
        expected: ULongArray,
    ) {
        val rng1 = testRng(897uL)
        val buf = ULongArray(6)
        for (i in 0 until 3) {
            buf[i] = UniformUsize.sampleSingleInclusive(lb, ub, rng1).unwrap()
        }
        val distr = UniformUsize.newInclusive(lb, ub).unwrap()
        for (i in 3 until 6) {
            buf[i] = distr.sample(rng1)
        }
        assertEquals(expected.toList(), buf.toList())

        val rng2 = testRng(897uL)
        for (i in 0 until 3) {
            buf[i] = UniformUsize.sampleSingle(lb, ubExcl, rng2).unwrap()
        }
        val distrExcl = UniformUsize.new(lb, ubExcl).unwrap()
        for (i in 3 until 6) {
            buf[i] = distrExcl.sample(rng2)
        }
        assertEquals(expected.toList(), buf.toList())
    }

    @Test
    fun valueStability() {
        testSamples((-105).toByte(), 111.toByte(), 112.toByte(), byteArrayOf(-99, -48, 107, 72, -19, 56))
        testSamples(2.toShort(), 1352.toShort(), 1353.toShort(), shortArrayOf(43, 361, 1325, 1109, 539, 1005))
        testSamples(
            -313853,
            13513,
            13514,
            intArrayOf(-303803, -226673, 6912, -45605, -183505, -70668),
        )
        testSamples(
            131521L,
            6542165L,
            6542166L,
            longArrayOf(1838724L, 5384489L, 4893692L, 3712948L, 3951509L, 4094926L),
        )
        testSamples(11u.toUByte(), 218u.toUByte(), 219u.toUByte(), ubyteArrayOf(17u, 66u, 214u, 181u, 93u, 165u))
        testSamples(11u.toUShort(), 218u.toUShort(), 219u.toUShort(), ushortArrayOf(17u, 66u, 214u, 181u, 93u, 165u))
        testSamples(11u, 218u, 219u, uintArrayOf(17u, 66u, 214u, 181u, 93u, 165u))
        testSamples(11uL, 218uL, 219uL, ulongArrayOf(66uL, 181uL, 165uL, 127uL, 134uL, 139uL))
        testSamplesUsize(11uL, 218uL, 219uL, ulongArrayOf(17uL, 66uL, 214uL, 181uL, 93uL, 165uL))
    }

    @Test
    fun testUniformUsizeEmptyRange() {
        assertEquals(Err(Error.EmptyRange), UniformUsize.new(10uL, 10uL))
        assertTrue(UniformUsize.new(10uL, 11uL).isOk)

        assertEquals(Err(Error.EmptyRange), UniformUsize.newInclusive(10uL, 9uL))
        assertTrue(UniformUsize.newInclusive(10uL, 10uL).isOk)
    }

    @Test
    fun testUniformUsizeConstructors() {
        assertEquals(
            Ok(
                UniformUsize(
                    low = UInt.MAX_VALUE.toULong(),
                    range = 1uL,
                    thresh = 0uL,
                    mode64 = false,
                ),
            ),
            UniformUsize.newInclusive(UInt.MAX_VALUE.toULong(), UInt.MAX_VALUE.toULong()),
        )
        assertEquals(
            Ok(
                UniformUsize(
                    low = 0uL,
                    range = 0uL,
                    thresh = 0uL,
                    mode64 = false,
                ),
            ),
            UniformUsize.newInclusive(0uL, UInt.MAX_VALUE.toULong()),
        )
        assertEquals(
            Ok(
                UniformUsize(
                    low = 0uL,
                    range = UInt.MAX_VALUE.toULong() + 2uL,
                    thresh = 1uL,
                    mode64 = true,
                ),
            ),
            UniformUsize.newInclusive(0uL, UInt.MAX_VALUE.toULong() + 1uL),
        )
        assertEquals(
            Ok(
                UniformUsize(
                    low = UInt.MAX_VALUE.toULong(),
                    range = ULong.MAX_VALUE - UInt.MAX_VALUE.toULong() + 1uL,
                    thresh = UInt.MAX_VALUE.toULong(),
                    mode64 = true,
                ),
            ),
            UniformUsize.newInclusive(UInt.MAX_VALUE.toULong(), ULong.MAX_VALUE),
        )
    }

    @Test
    fun testUniformUsizeDeserialization() {
        val original = UniformUsize.newInclusive(10uL, 100uL).unwrap()
        assertEquals(10uL, original.low)
        assertEquals(91uL, original.range)
        assertEquals(74uL, original.thresh)
        assertEquals(false, original.mode64)
    }

    @Test
    fun testUniformUsizeDeserializationFrom32bit() {
        val original = UniformUsize.newInclusive(10uL, 100uL).unwrap()
        assertEquals(10uL, original.low)
        assertEquals(91uL, original.range)
        assertEquals(74uL, original.thresh)
    }

    @Test
    fun testUniformUsizeDeserialization64bit() {
        val original = UniformUsize.newInclusive(1uL, ULong.MAX_VALUE - 1uL).unwrap()
        assertTrue(original.mode64)
    }
}
