// port-lint: tests rng.rs
package io.github.kotlinmania.rand

import io.github.kotlinmania.rand.distr.StandardUniform
import io.github.kotlinmania.rand.distr.UniformInt
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalUnsignedTypes::class)
class RngTest {
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

    private fun constRng(x: ULong): StepRng = StepRng(x, 0uL)

    private fun rng(seed: ULong): Rng = Pcg32(seed)

    @Test
    fun testFillBytesDefault() {
        val r = constRng(0x11_22_33_44_55_66_77_88uL)

        // Check every remainder mod 8, both in small and big vectors.
        val lengths = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 80, 81, 82, 83, 84, 85, 86, 87)
        for (n in lengths) {
            val v = ByteArray(n)
            r.fillBytes(v)

            for (i in v.indices) {
                val byte = v[i]
                if (byte == 0.toByte()) {
                    error("byte $i of $n is zero")
                }
            }
        }
    }

    @Test
    fun testFill() {
        val x = 9041086907909331047uL
        val rng = constRng(x)

        // Convert to byte sequence and back to u64 in portable little-endian order.
        val u64Array = ULongArray(2)
        rng.fill(u64Array)
        assertContentEquals(ulongArrayOf(x, x), u64Array)
        assertEquals(x, rng.nextU64())

        // Convert to bytes then u32 in little-endian order.
        val u32Array = UIntArray(2)
        rng.fill(u32Array)
        assertContentEquals(uintArrayOf(x.toUInt(), (x shr 32).toUInt()), u32Array)
        assertEquals(x.toUInt(), rng.nextU32())

        // Check equivalence using custom Fill wrapper.
        val wrapped = UIntArray(2)
        val fillTarget = Fill { targetRng -> targetRng.fill(wrapped) }
        rng.fill(fillTarget)
        assertEquals(u32Array[0], wrapped[0])
        assertEquals(u32Array[1], wrapped[1])

        // Check equivalence for generated floats.
        val floatArray = FloatArray(2)
        rng.fill(floatArray)
        val arr2 = floatArrayOf(rng.random<Float>(), rng.random<Float>())
        assertContentEquals(arr2, floatArray)
    }

    @Test
    fun testFillEmpty() {
        var draws = 0
        val countingRng =
            object : Rng {
                override fun nextU32(): UInt {
                    draws++
                    return 1u
                }

                override fun nextU64(): ULong {
                    draws++
                    return 1uL
                }

                override fun fillBytes(dest: ByteArray) {
                    draws++
                }
            }
        val emptyU32 = UIntArray(0)
        val emptyI32 = IntArray(0)
        countingRng.fill(emptyU32)
        countingRng.fill(emptyI32)
        assertEquals(0, draws)

        val seeded = rng(1uL)
        seeded.fill(emptyU32)
        seeded.fill(emptyI32)
    }

    @Test
    fun testRandomRangeInt() {
        val r = rng(101uL)
        repeat(1000) {
            val a = r.randomRange(-4711 until 17)
            assertTrue(a in -4711 until 17)

            val aByte: Byte = UniformInt.sampleSingle((-3).toByte(), 42.toByte(), r).unwrap()
            assertTrue(aByte in -3 until 42)

            val aUShort: UShort = UniformInt.sampleSingle(10u.toUShort(), 99u.toUShort(), r).unwrap()
            assertTrue(aUShort in 10u..98u)

            val aInt: Int = r.randomRange(-100 until 2000)
            assertTrue(aInt in -100 until 2000)

            val aUInt: UInt = r.randomRange(12u..24u)
            assertTrue(aUInt in 12u..24u)

            assertEquals(0u, r.randomRange(0u until 1u))
            assertEquals(-12L, r.randomRange(-12L until -11L))
            assertEquals(3_000_000, r.randomRange(3_000_000 until 3_000_001))
        }
    }

    @Test
    fun testRandomRangeFloat() {
        val r = rng(101uL)
        repeat(1000) {
            val a = r.randomRange(-4.5..<1.7)
            assertTrue(a >= -4.5 && a < 1.7)
            val aIncl = r.randomRange(-1.1..-0.3)
            assertTrue(aIncl in -1.1..-0.3)

            assertEquals(0.0f, r.randomRange(0.0f..0.0f))
            assertEquals(-11.0, r.randomRange(-11.0..-11.0))
            assertEquals(3_000_000.0, r.randomRange(3_000_000.0..3_000_000.0))
        }
    }

    @Test
    fun testRandomRangePanicInt() {
        val r = rng(102uL)
        @Suppress("EmptyRange")
        assertFailsWith<IllegalArgumentException> {
            r.randomRange(5..-2)
        }
    }

    @Test
    fun testRandomRangePanicUsize() {
        val r = rng(103uL)
        @Suppress("EmptyRange")
        assertFailsWith<IllegalArgumentException> {
            r.randomRange(5u..2u)
        }
    }

    @Test
    fun testRandomBool() {
        val r = rng(105uL)
        repeat(5) {
            assertFalse(r.randomBool(0.0))
            assertTrue(r.randomBool(1.0))
        }
    }

    @Test
    fun testRngMutRef() {
        fun useRng(r: Rng) {
            r.nextU32()
        }

        val r = rng(109uL)
        useRng(r)
    }

    @Test
    fun testRngTraitObject() {
        val underlying = rng(109uL)
        val r: RngCore = underlying
        r.nextU32()
        r.random<Int>()
        assertEquals(0, r.randomRange(0 until 1))
        val c: UByte = StandardUniform.sampleUByte(r.asRng())
        assertTrue(c <= UByte.MAX_VALUE)
    }

    private class BoxedRngCore(
        delegate: RngCore,
    ) : RngCore by delegate

    @Test
    fun testRngBoxedTrait() {
        val r: RngCore = BoxedRngCore(rng(110uL))
        r.nextU32()
        r.random<Int>()
        assertEquals(0, r.randomRange(0 until 1))
        val c: UByte = r.random<UByte>()
        assertTrue(c <= UByte.MAX_VALUE)
    }

    @Test
    fun testGenRatioAverage() {
        val num = 3u
        val denom = 10u
        val n = 100_000u

        var sum = 0u
        val r = rng(111uL)
        repeat(n.toInt()) {
            if (r.randomRatio(num, denom)) {
                sum += 1u
            }
        }
        val expected = (num * n) / denom
        assertTrue(abs((sum - expected).toInt()) < 500)
    }

    @Test
    fun testRandomIterAndGenAliases() {
        val r = rng(202uL)
        val ints = ArrayList<Int>(5)
        val iter = r.randomIter<Int>()
        repeat(5) {
            ints.add(iter.next())
        }
        assertEquals(5, ints.size)

        val aliases = rng(202uL)
        repeat(5) { idx ->
            assertEquals(ints[idx], aliases.gen<Int>())
        }
        assertEquals(7, aliases.genRange(7..7))
        assertFalse(aliases.genBool(0.0))
        assertTrue(aliases.genBool(1.0))
        assertFalse(aliases.genRatio(0u, 5u))
        assertTrue(aliases.genRatio(5u, 5u))
    }

    @Test
    fun testStepRngIncrement() {
        val stepRng = StepRng(10uL, 5uL)
        assertEquals(10uL, stepRng.nextU64())
        assertEquals(15uL, stepRng.nextU64())
        assertEquals(20uL, stepRng.nextU64())
    }

    @Test
    fun testStepRngFillBytes() {
        val stepRng = StepRng(0x0102030405060708uL, 0uL)
        val buffer = ByteArray(8)
        stepRng.fillBytes(buffer)
        assertEquals(0x08.toByte(), buffer[0])
        assertEquals(0x07.toByte(), buffer[1])
        assertEquals(0x06.toByte(), buffer[2])
        assertEquals(0x05.toByte(), buffer[3])
    }

    @Test
    fun testRandomRangeInclusive() {
        val randomRng = RandomRng()
        val num = randomRng.randomRange(5..5)
        assertEquals(5, num)

        val sample = randomRng.randomRange(10..20)
        assertTrue(sample in 10..20)
    }

    @Test
    fun testRandomBoolProbabilities() {
        val randomRng = RandomRng()
        assertFalse(randomRng.randomBool(0.0))
        assertTrue(randomRng.randomBool(1.0))
    }
}
