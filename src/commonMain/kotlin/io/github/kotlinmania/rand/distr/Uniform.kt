// port-lint: source rand/src/distr/uniform.rs
package io.github.kotlinmania.rand.distr

import io.github.kotlinmania.rand.Rng

/**
 * A distribution uniformly sampling values within a given range.
 */
public class Uniform<T>(
    private val sampler: (Rng) -> T,
) : Distribution<T> {
    override fun sample(rng: Rng): T = sampler(rng)

    public companion object {
        public fun int(low: Int, highExclusive: Int): Uniform<Int> {
            require(low < highExclusive) { "Uniform.int requires low < highExclusive (got $low >= $highExclusive)" }
            val range = (highExclusive.toLong() - low.toLong()).toULong()
            return Uniform { rng ->
                val offset = (rng.nextU64() % range).toLong()
                (low.toLong() + offset).toInt()
            }
        }

        public fun intInclusive(low: Int, highInclusive: Int): Uniform<Int> {
            require(low <= highInclusive) { "Uniform.intInclusive requires low <= highInclusive" }
            val range = (highInclusive.toLong() - low.toLong() + 1L).toULong()
            return Uniform { rng ->
                val offset = (rng.nextU64() % range).toLong()
                (low.toLong() + offset).toInt()
            }
        }

        public fun long(low: Long, highExclusive: Long): Uniform<Long> {
            require(low < highExclusive) { "Uniform.long requires low < highExclusive" }
            val range = (highExclusive.toULong() - low.toULong())
            return Uniform { rng ->
                val offset = rng.nextU64() % range
                (low.toULong() + offset).toLong()
            }
        }

        public fun longInclusive(low: Long, highInclusive: Long): Uniform<Long> {
            require(low <= highInclusive) { "Uniform.longInclusive requires low <= highInclusive" }
            val range = (highInclusive.toULong() - low.toULong() + 1uL)
            return Uniform { rng ->
                if (range == 0uL) { // Full range
                    rng.nextU64().toLong()
                } else {
                    val offset = rng.nextU64() % range
                    (low.toULong() + offset).toLong()
                }
            }
        }

        public fun double(low: Double, highExclusive: Double): Uniform<Double> {
            require(low < highExclusive) { "Uniform.double requires low < highExclusive" }
            val scale = highExclusive - low
            return Uniform { rng ->
                val u = StandardUniform.sample(rng)
                low + u * scale
            }
        }

        public fun doubleInclusive(low: Double, highInclusive: Double): Uniform<Double> {
            require(low <= highInclusive) { "Uniform.doubleInclusive requires low <= highInclusive" }
            val scale = highInclusive - low
            return Uniform { rng ->
                val u = StandardUniform.sample(rng)
                low + u * scale
            }
        }

        public fun float(low: Float, highExclusive: Float): Uniform<Float> {
            require(low < highExclusive) { "Uniform.float requires low < highExclusive" }
            val scale = highExclusive - low
            return Uniform { rng ->
                val u = StandardUniform.sampleFloat(rng)
                low + u * scale
            }
        }

        public fun floatInclusive(low: Float, highInclusive: Float): Uniform<Float> {
            require(low <= highInclusive) { "Uniform.floatInclusive requires low <= highInclusive" }
            val scale = highInclusive - low
            return Uniform { rng ->
                val u = StandardUniform.sampleFloat(rng)
                low + u * scale
            }
        }

        public fun byte(low: Byte, highExclusive: Byte): Uniform<Byte> =
            int(low.toInt(), highExclusive.toInt()).map { it.toByte() }.let { Uniform { rng -> it.sample(rng) } }

        public fun byteInclusive(low: Byte, highInclusive: Byte): Uniform<Byte> =
            intInclusive(low.toInt(), highInclusive.toInt()).map { it.toByte() }.let { Uniform { rng -> it.sample(rng) } }

        public fun short(low: Short, highExclusive: Short): Uniform<Short> =
            int(low.toInt(), highExclusive.toInt()).map { it.toShort() }.let { Uniform { rng -> it.sample(rng) } }

        public fun shortInclusive(low: Short, highInclusive: Short): Uniform<Short> =
            intInclusive(low.toInt(), highInclusive.toInt()).map { it.toShort() }.let { Uniform { rng -> it.sample(rng) } }

        public fun uInt(low: UInt, highExclusive: UInt): Uniform<UInt> {
            require(low < highExclusive) { "Uniform.uInt requires low < highExclusive" }
            val range = highExclusive - low
            return Uniform { rng -> low + (rng.nextU32() % range) }
        }

        public fun uIntInclusive(low: UInt, highInclusive: UInt): Uniform<UInt> {
            require(low <= highInclusive) { "Uniform.uIntInclusive requires low <= highInclusive" }
            val range = highInclusive - low + 1u
            return Uniform { rng ->
                if (range == 0u) rng.nextU32() else low + (rng.nextU32() % range)
            }
        }

        public fun uLong(low: ULong, highExclusive: ULong): Uniform<ULong> {
            require(low < highExclusive) { "Uniform.uLong requires low < highExclusive" }
            val range = highExclusive - low
            return Uniform { rng -> low + (rng.nextU64() % range) }
        }

        public fun uLongInclusive(low: ULong, highInclusive: ULong): Uniform<ULong> {
            require(low <= highInclusive) { "Uniform.uLongInclusive requires low <= highInclusive" }
            val range = highInclusive - low + 1uL
            return Uniform { rng ->
                if (range == 0uL) rng.nextU64() else low + (rng.nextU64() % range)
            }
        }

        public fun char(low: Char, highExclusive: Char): Uniform<Char> =
            int(low.code, highExclusive.code).map { it.toChar() }.let { Uniform { rng -> it.sample(rng) } }

        public fun charInclusive(low: Char, highInclusive: Char): Uniform<Char> =
            intInclusive(low.code, highInclusive.code).map { it.toChar() }.let { Uniform { rng -> it.sample(rng) } }
    }
}
