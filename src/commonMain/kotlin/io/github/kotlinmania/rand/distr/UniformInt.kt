// port-lint: source distr/uniform_int.rs
package io.github.kotlinmania.rand.distr

import io.github.kotlinmania.rand.Rng

/**
 * The back-end implementing [UniformSampler] for integer types.
 *
 * Unless you are implementing [UniformSampler] for your own type, this type
 * should not be used directly, use [Uniform] instead.
 */
public class UniformInt<X>(
    public val low: X,
    public val range: X,
    public val thresh: X,
    private val sampleFn: (Rng) -> X,
) : UniformSampler<X> {
    override fun sample(rng: Rng): X = sampleFn(rng)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UniformInt<*>) return false
        return low == other.low && range == other.range && thresh == other.thresh
    }

    override fun hashCode(): Int {
        var result = low?.hashCode() ?: 0
        result = 31 * result + (range?.hashCode() ?: 0)
        result = 31 * result + (thresh?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "UniformInt(low=$low, range=$range, thresh=$thresh)"

    public companion object {
        // Int
        public fun new(low: Int, high: Int): Result<UniformInt<Int>, Error> {
            if (low >= high) return Err(Error.EmptyRange)
            return newInclusive(low, high - 1)
        }

        public fun newInclusive(low: Int, high: Int): Result<UniformInt<Int>, Error> {
            if (low > high) return Err(Error.EmptyRange)
            val range = high.toUInt() - low.toUInt() + 1u
            val thresh = if (range > 0u) (0u - range) % range else 0u
            return Ok(
                UniformInt(
                    low = low,
                    range = range.toInt(),
                    thresh = thresh.toInt(),
                    sampleFn = { rng ->
                        val r = range
                        if (r == 0u) {
                            rng.nextU32().toInt()
                        } else {
                            val t = thresh
                            var hi: UInt
                            while (true) {
                                val (h, lo) = rng.nextU32().wmul(r)
                                if (lo >= t) {
                                    hi = h
                                    break
                                }
                            }
                            low + hi.toInt()
                        }
                    },
                ),
            )
        }

        public fun sampleSingle(low: Int, high: Int, rng: Rng): Result<Int, Error> {
            if (low >= high) return Err(Error.EmptyRange)
            return sampleSingleInclusive(low, high - 1, rng)
        }

        public fun sampleSingleInclusive(low: Int, high: Int, rng: Rng): Result<Int, Error> {
            if (low > high) return Err(Error.EmptyRange)
            val range = high.toUInt() - low.toUInt() + 1u
            if (range == 0u) return Ok(rng.nextU32().toInt())
            val (result, loOrder) = rng.nextU32().wmul(range)
            var finalResult = result
            if (loOrder > (0u - range)) {
                val (newHiOrder, _) = rng.nextU32().wmul(range)
                val isOverflow = loOrder > (UInt.MAX_VALUE - newHiOrder)
                if (isOverflow) finalResult += 1u
            }
            return Ok(low + finalResult.toInt())
        }

        // UInt
        public fun new(low: UInt, high: UInt): Result<UniformInt<UInt>, Error> {
            if (low >= high) return Err(Error.EmptyRange)
            return newInclusive(low, high - 1u)
        }

        public fun newInclusive(low: UInt, high: UInt): Result<UniformInt<UInt>, Error> {
            if (low > high) return Err(Error.EmptyRange)
            val range = high - low + 1u
            val thresh = if (range > 0u) (0u - range) % range else 0u
            return Ok(
                UniformInt(
                    low = low,
                    range = range,
                    thresh = thresh,
                    sampleFn = { rng ->
                        if (range == 0u) {
                            rng.nextU32()
                        } else {
                            var hi: UInt
                            while (true) {
                                val (h, lo) = rng.nextU32().wmul(range)
                                if (lo >= thresh) {
                                    hi = h
                                    break
                                }
                            }
                            low + hi
                        }
                    },
                ),
            )
        }

        public fun sampleSingle(low: UInt, high: UInt, rng: Rng): Result<UInt, Error> {
            if (low >= high) return Err(Error.EmptyRange)
            return sampleSingleInclusive(low, high - 1u, rng)
        }

        public fun sampleSingleInclusive(low: UInt, high: UInt, rng: Rng): Result<UInt, Error> {
            if (low > high) return Err(Error.EmptyRange)
            val range = high - low + 1u
            if (range == 0u) return Ok(rng.nextU32())
            val (result, loOrder) = rng.nextU32().wmul(range)
            var finalResult = result
            if (loOrder > (0u - range)) {
                val (newHiOrder, _) = rng.nextU32().wmul(range)
                val isOverflow = loOrder > (UInt.MAX_VALUE - newHiOrder)
                if (isOverflow) finalResult += 1u
            }
            return Ok(low + finalResult)
        }

        // Long
        public fun new(low: Long, high: Long): Result<UniformInt<Long>, Error> {
            if (low >= high) return Err(Error.EmptyRange)
            return newInclusive(low, high - 1L)
        }

        public fun newInclusive(low: Long, high: Long): Result<UniformInt<Long>, Error> {
            if (low > high) return Err(Error.EmptyRange)
            val range = high.toULong() - low.toULong() + 1uL
            val thresh = if (range > 0uL) (0uL - range) % range else 0uL
            return Ok(
                UniformInt(
                    low = low,
                    range = range.toLong(),
                    thresh = thresh.toLong(),
                    sampleFn = { rng ->
                        val r = range
                        if (r == 0uL) {
                            rng.nextU64().toLong()
                        } else {
                            val t = thresh
                            var hi: ULong
                            while (true) {
                                val (h, lo) = rng.nextU64().wmul(r)
                                if (lo >= t) {
                                    hi = h
                                    break
                                }
                            }
                            low + hi.toLong()
                        }
                    },
                ),
            )
        }

        public fun sampleSingle(low: Long, high: Long, rng: Rng): Result<Long, Error> {
            if (low >= high) return Err(Error.EmptyRange)
            return sampleSingleInclusive(low, high - 1L, rng)
        }

        public fun sampleSingleInclusive(low: Long, high: Long, rng: Rng): Result<Long, Error> {
            if (low > high) return Err(Error.EmptyRange)
            val range = high.toULong() - low.toULong() + 1uL
            if (range == 0uL) return Ok(rng.nextU64().toLong())
            val (result, loOrder) = rng.nextU64().wmul(range)
            var finalResult = result
            if (loOrder > (0uL - range)) {
                val (newHiOrder, _) = rng.nextU64().wmul(range)
                val isOverflow = loOrder > (ULong.MAX_VALUE - newHiOrder)
                if (isOverflow) finalResult += 1uL
            }
            return Ok(low + finalResult.toLong())
        }

        // ULong
        public fun new(low: ULong, high: ULong): Result<UniformInt<ULong>, Error> {
            if (low >= high) return Err(Error.EmptyRange)
            return newInclusive(low, high - 1uL)
        }

        public fun newInclusive(low: ULong, high: ULong): Result<UniformInt<ULong>, Error> {
            if (low > high) return Err(Error.EmptyRange)
            val range = high - low + 1uL
            val thresh = if (range > 0uL) (0uL - range) % range else 0uL
            return Ok(
                UniformInt(
                    low = low,
                    range = range,
                    thresh = thresh,
                    sampleFn = { rng ->
                        if (range == 0uL) {
                            rng.nextU64()
                        } else {
                            var hi: ULong
                            while (true) {
                                val (h, lo) = rng.nextU64().wmul(range)
                                if (lo >= thresh) {
                                    hi = h
                                    break
                                }
                            }
                            low + hi
                        }
                    },
                ),
            )
        }

        public fun sampleSingle(low: ULong, high: ULong, rng: Rng): Result<ULong, Error> {
            if (low >= high) return Err(Error.EmptyRange)
            return sampleSingleInclusive(low, high - 1uL, rng)
        }

        public fun sampleSingleInclusive(low: ULong, high: ULong, rng: Rng): Result<ULong, Error> {
            if (low > high) return Err(Error.EmptyRange)
            val range = high - low + 1uL
            if (range == 0uL) return Ok(rng.nextU64())
            val (result, loOrder) = rng.nextU64().wmul(range)
            var finalResult = result
            if (loOrder > (0uL - range)) {
                val (newHiOrder, _) = rng.nextU64().wmul(range)
                val isOverflow = loOrder > (ULong.MAX_VALUE - newHiOrder)
                if (isOverflow) finalResult += 1uL
            }
            return Ok(low + finalResult)
        }

        // Byte
        public fun new(low: Byte, high: Byte): Result<UniformInt<Byte>, Error> {
            if (low >= high) return Err(Error.EmptyRange)
            return newInclusive(low, (high - 1).toByte())
        }

        public fun newInclusive(low: Byte, high: Byte): Result<UniformInt<Byte>, Error> {
            if (low > high) return Err(Error.EmptyRange)
            val range = ((high.toInt() - low.toInt() + 1) and 0xFF).toUByte()
            val thresh =
                if (range > 0u.toUByte()) {
                    val rangeU32 = range.toUInt()
                    ((0u - rangeU32) % rangeU32).toUByte()
                } else {
                    0u.toUByte()
                }
            return Ok(
                UniformInt(
                    low = low,
                    range = range.toByte(),
                    thresh = thresh.toByte(),
                    sampleFn = { rng ->
                        val r = range.toUInt()
                        if (r == 0u) {
                            rng.nextU32().toByte()
                        } else {
                            val t = thresh.toUInt()
                            var hi: UInt
                            while (true) {
                                val (h, lo) = rng.nextU32().wmul(r)
                                if (lo >= t) {
                                    hi = h
                                    break
                                }
                            }
                            (low + hi.toInt()).toByte()
                        }
                    },
                ),
            )
        }

        public fun sampleSingle(low: Byte, high: Byte, rng: Rng): Result<Byte, Error> {
            if (low >= high) return Err(Error.EmptyRange)
            return sampleSingleInclusive(low, (high - 1).toByte(), rng)
        }

        public fun sampleSingleInclusive(low: Byte, high: Byte, rng: Rng): Result<Byte, Error> {
            if (low > high) return Err(Error.EmptyRange)
            val range = ((high.toInt() - low.toInt() + 1) and 0xFF).toUInt()
            if (range == 0u) return Ok(rng.nextU32().toByte())
            val (result, loOrder) = rng.nextU32().wmul(range)
            var finalResult = result
            if (loOrder > (0u - range)) {
                val (newHiOrder, _) = rng.nextU32().wmul(range)
                val isOverflow = loOrder > (UInt.MAX_VALUE - newHiOrder)
                if (isOverflow) finalResult += 1u
            }
            return Ok((low + finalResult.toInt()).toByte())
        }

        // UByte
        public fun new(low: UByte, high: UByte): Result<UniformInt<UByte>, Error> {
            if (low >= high) return Err(Error.EmptyRange)
            return newInclusive(low, (high - 1u).toUByte())
        }

        public fun newInclusive(low: UByte, high: UByte): Result<UniformInt<UByte>, Error> {
            if (low > high) return Err(Error.EmptyRange)
            val range = ((high.toInt() - low.toInt() + 1) and 0xFF).toUByte()
            val thresh =
                if (range > 0u.toUByte()) {
                    val rangeU32 = range.toUInt()
                    ((0u - rangeU32) % rangeU32).toUByte()
                } else {
                    0u.toUByte()
                }
            return Ok(
                UniformInt(
                    low = low,
                    range = range,
                    thresh = thresh,
                    sampleFn = { rng ->
                        val r = range.toUInt()
                        if (r == 0u) {
                            rng.nextU32().toUByte()
                        } else {
                            val t = thresh.toUInt()
                            var hi: UInt
                            while (true) {
                                val (h, lo) = rng.nextU32().wmul(r)
                                if (lo >= t) {
                                    hi = h
                                    break
                                }
                            }
                            (low + hi.toUByte()).toUByte()
                        }
                    },
                ),
            )
        }

        public fun sampleSingle(low: UByte, high: UByte, rng: Rng): Result<UByte, Error> {
            if (low >= high) return Err(Error.EmptyRange)
            return sampleSingleInclusive(low, (high - 1u).toUByte(), rng)
        }

        public fun sampleSingleInclusive(low: UByte, high: UByte, rng: Rng): Result<UByte, Error> {
            if (low > high) return Err(Error.EmptyRange)
            val range = ((high.toInt() - low.toInt() + 1) and 0xFF).toUInt()
            if (range == 0u) return Ok(rng.nextU32().toUByte())
            val (result, loOrder) = rng.nextU32().wmul(range)
            var finalResult = result
            if (loOrder > (0u - range)) {
                val (newHiOrder, _) = rng.nextU32().wmul(range)
                val isOverflow = loOrder > (UInt.MAX_VALUE - newHiOrder)
                if (isOverflow) finalResult += 1u
            }
            return Ok((low + finalResult.toUByte()).toUByte())
        }

        // Short
        public fun new(low: Short, high: Short): Result<UniformInt<Short>, Error> {
            if (low >= high) return Err(Error.EmptyRange)
            return newInclusive(low, (high - 1).toShort())
        }

        public fun newInclusive(low: Short, high: Short): Result<UniformInt<Short>, Error> {
            if (low > high) return Err(Error.EmptyRange)
            val range = ((high.toInt() - low.toInt() + 1) and 0xFFFF).toUShort()
            val thresh =
                if (range > 0u.toUShort()) {
                    val rangeU32 = range.toUInt()
                    ((0u - rangeU32) % rangeU32).toUShort()
                } else {
                    0u.toUShort()
                }
            return Ok(
                UniformInt(
                    low = low,
                    range = range.toShort(),
                    thresh = thresh.toShort(),
                    sampleFn = { rng ->
                        val r = range.toUInt()
                        if (r == 0u) {
                            rng.nextU32().toShort()
                        } else {
                            val t = thresh.toUInt()
                            var hi: UInt
                            while (true) {
                                val (h, lo) = rng.nextU32().wmul(r)
                                if (lo >= t) {
                                    hi = h
                                    break
                                }
                            }
                            (low + hi.toInt()).toShort()
                        }
                    },
                ),
            )
        }

        public fun sampleSingle(low: Short, high: Short, rng: Rng): Result<Short, Error> {
            if (low >= high) return Err(Error.EmptyRange)
            return sampleSingleInclusive(low, (high - 1).toShort(), rng)
        }

        public fun sampleSingleInclusive(low: Short, high: Short, rng: Rng): Result<Short, Error> {
            if (low > high) return Err(Error.EmptyRange)
            val range = ((high.toInt() - low.toInt() + 1) and 0xFFFF).toUInt()
            if (range == 0u) return Ok(rng.nextU32().toShort())
            val (result, loOrder) = rng.nextU32().wmul(range)
            var finalResult = result
            if (loOrder > (0u - range)) {
                val (newHiOrder, _) = rng.nextU32().wmul(range)
                val isOverflow = loOrder > (UInt.MAX_VALUE - newHiOrder)
                if (isOverflow) finalResult += 1u
            }
            return Ok((low + finalResult.toInt()).toShort())
        }

        // UShort
        public fun new(low: UShort, high: UShort): Result<UniformInt<UShort>, Error> {
            if (low >= high) return Err(Error.EmptyRange)
            return newInclusive(low, (high - 1u).toUShort())
        }

        public fun newInclusive(low: UShort, high: UShort): Result<UniformInt<UShort>, Error> {
            if (low > high) return Err(Error.EmptyRange)
            val range = ((high.toInt() - low.toInt() + 1) and 0xFFFF).toUShort()
            val thresh =
                if (range > 0u.toUShort()) {
                    val rangeU32 = range.toUInt()
                    ((0u - rangeU32) % rangeU32).toUShort()
                } else {
                    0u.toUShort()
                }
            return Ok(
                UniformInt(
                    low = low,
                    range = range,
                    thresh = thresh,
                    sampleFn = { rng ->
                        val r = range.toUInt()
                        if (r == 0u) {
                            rng.nextU32().toUShort()
                        } else {
                            val t = thresh.toUInt()
                            var hi: UInt
                            while (true) {
                                val (h, lo) = rng.nextU32().wmul(r)
                                if (lo >= t) {
                                    hi = h
                                    break
                                }
                            }
                            (low + hi.toUShort()).toUShort()
                        }
                    },
                ),
            )
        }

        public fun sampleSingle(low: UShort, high: UShort, rng: Rng): Result<UShort, Error> {
            if (low >= high) return Err(Error.EmptyRange)
            return sampleSingleInclusive(low, (high - 1u).toUShort(), rng)
        }

        public fun sampleSingleInclusive(low: UShort, high: UShort, rng: Rng): Result<UShort, Error> {
            if (low > high) return Err(Error.EmptyRange)
            val range = ((high.toInt() - low.toInt() + 1) and 0xFFFF).toUInt()
            if (range == 0u) return Ok(rng.nextU32().toUShort())
            val (result, loOrder) = rng.nextU32().wmul(range)
            var finalResult = result
            if (loOrder > (0u - range)) {
                val (newHiOrder, _) = rng.nextU32().wmul(range)
                val isOverflow = loOrder > (UInt.MAX_VALUE - newHiOrder)
                if (isOverflow) finalResult += 1u
            }
            return Ok((low + finalResult.toUShort()).toUShort())
        }
    }
}

/**
 * The back-end implementing [UniformSampler] for pointer-sized integers (represented in Kotlin as [ULong]).
 *
 * # Implementation notes
 *
 * Sampling a pointer-sized value is usually used in relation to the length of an
 * array or other memory structure, thus it is reasonable to assume that the
 * vast majority of use-cases will have a maximum size under [UInt.MAX_VALUE].
 * In part to optimise for this use-case, but mostly to ensure that results
 * are portable across 32-bit and 64-bit architectures (as far as is possible),
 * this implementation will use 32-bit sampling when possible.
 */
public data class UniformUsize(
    public val low: ULong,
    public val range: ULong,
    public val thresh: ULong,
    public val mode64: Boolean = false,
) : UniformSampler<ULong> {
    public typealias X = ULong

    override fun sample(rng: Rng): ULong {
        return if (!mode64) {
            val r32 = range.toUInt()
            if (r32 == 0u) {
                return rng.nextU32().toULong()
            }
            val t32 = thresh.toUInt()
            var hi: UInt
            while (true) {
                val (h, lo) = rng.nextU32().wmul(r32)
                if (lo >= t32) {
                    hi = h
                    break
                }
            }
            low + hi.toULong()
        } else {
            val r64 = range
            if (r64 == 0uL) {
                return rng.nextU64()
            }
            val t64 = thresh
            var hi: ULong
            while (true) {
                val (h, lo) = rng.nextU64().wmul(r64)
                if (lo >= t64) {
                    hi = h
                    break
                }
            }
            low + hi
        }
    }

    public companion object {
        public fun new(low: ULong, high: ULong): Result<UniformUsize, Error> {
            if (low >= high) return Err(Error.EmptyRange)
            return newInclusive(low, high - 1uL)
        }

        public fun newInclusive(low: ULong, high: ULong): Result<UniformUsize, Error> {
            if (low > high) return Err(Error.EmptyRange)
            val mode64 = high > UInt.MAX_VALUE.toULong()
            val range: ULong
            val thresh: ULong
            if (!mode64) {
                val range32 = high.toUInt() - low.toUInt() + 1u
                range = range32.toULong()
                thresh =
                    if (range32 > 0u) {
                        ((0u - range32) % range32).toULong()
                    } else {
                        0uL
                    }
            } else {
                range = high - low + 1uL
                thresh =
                    if (range > 0uL) {
                        (0uL - range) % range
                    } else {
                        0uL
                    }
            }
            return Ok(
                UniformUsize(
                    low = low,
                    range = range,
                    thresh = thresh,
                    mode64 = mode64,
                ),
            )
        }

        public fun sampleSingle(low: ULong, high: ULong, rng: Rng): Result<ULong, Error> {
            if (low >= high) return Err(Error.EmptyRange)
            return sampleSingleInclusive(low, high - 1uL, rng)
        }

        public fun sampleSingleInclusive(low: ULong, high: ULong, rng: Rng): Result<ULong, Error> {
            if (low > high) return Err(Error.EmptyRange)
            if (high > UInt.MAX_VALUE.toULong()) {
                return UniformInt.sampleSingleInclusive(low, high, rng)
            }
            return UniformInt.sampleSingleInclusive(low.toUInt(), high.toUInt(), rng).map { it.toULong() }
        }

        // Convenience overloads for Long / Int
        public fun new(low: Long, high: Long): Result<UniformUsize, Error> =
            new(low.toULong(), high.toULong())

        public fun newInclusive(low: Long, high: Long): Result<UniformUsize, Error> =
            newInclusive(low.toULong(), high.toULong())

        public fun sampleSingle(low: Long, high: Long, rng: Rng): Result<Long, Error> =
            sampleSingle(low.toULong(), high.toULong(), rng).map { it.toLong() }

        public fun sampleSingleInclusive(low: Long, high: Long, rng: Rng): Result<Long, Error> =
            sampleSingleInclusive(low.toULong(), high.toULong(), rng).map { it.toLong() }
    }
}
