// port-lint: source distr/uniform.rs
package io.github.kotlinmania.rand.distr

import io.github.kotlinmania.rand.Rng

/**
 * Error type returned from [Uniform.new] and [Uniform.newInclusive].
 */
public enum class Error {
    /**
     * `low > high`, or equal in case of exclusive range.
     */
    EmptyRange,

    /**
     * Input or range `high - low` is non-finite. Not relevant to integer types.
     */
    NonFinite,

    ;

    override fun toString(): String =
        when (this) {
            EmptyRange -> "low > high (or equal if exclusive) in uniform distribution"
            NonFinite -> "Non-finite range in uniform distribution"
        }
}

/**
 * A Result type representing either success ([Ok]) or failure ([Err]).
 */
public sealed class Result<out T, out E> {
    public data class Ok<out T>(
        public val value: T,
    ) : Result<T, Nothing>()

    public data class Err<out E>(
        public val error: E,
    ) : Result<Nothing, E>()

    public val isOk: Boolean get() = this is Ok
    public val isErr: Boolean get() = this is Err

    public fun ok(): T? =
        when (this) {
            is Ok -> value
            is Err -> null
        }

    public fun err(): E? =
        when (this) {
            is Ok -> null
            is Err -> error
        }

    public fun unwrap(): T =
        when (this) {
            is Ok -> value
            is Err -> error("Called unwrap on an Err value: $error")
        }

    public inline fun <R> map(transform: (T) -> R): Result<R, E> =
        when (this) {
            is Ok -> Ok(transform(value))
            is Err -> this
        }
}

public fun <T> Ok(value: T): Result<T, Nothing> = Result.Ok(value)

public fun <E> Err(error: E): Result<Nothing, E> = Result.Err(error)

/**
 * Helper trait handling actual uniform sampling.
 */
public interface UniformSampler<X> {
    public fun sample(rng: Rng): X
}

/**
 * Helper trait for creating objects using the correct implementation of
 * [UniformSampler] for the sampling type.
 */
public interface SampleUniform<X> {
    public fun sampler(): UniformSampler<X>
}

/**
 * Helper trait for borrowing from an owned value.
 */
public interface SampleBorrow<Borrowed> {
    public fun borrow(): Borrowed
}

/**
 * Range that supports generating a single sample efficiently.
 */
public interface SampleRange<T> {
    public fun sampleSingle(rng: Rng): Result<T, Error>

    public fun isEmpty(): Boolean
}

/**
 * A distribution uniformly sampling values within a given range.
 */
public class Uniform<T>(
    private val sampler: (Rng) -> T,
) : Distribution<T> {
    public constructor(sampler: UniformSampler<T>) : this({ rng -> sampler.sample(rng) })

    override fun sample(rng: Rng): T = sampler(rng)

    public companion object {
        public fun new(low: Int, high: Int): Result<Uniform<Int>, Error> =
            UniformInt.new(low, high).map { Uniform(it) }

        public fun newInclusive(low: Int, high: Int): Result<Uniform<Int>, Error> =
            UniformInt.newInclusive(low, high).map { Uniform(it) }

        public fun new(low: Long, high: Long): Result<Uniform<Long>, Error> =
            UniformInt.new(low, high).map { Uniform(it) }

        public fun newInclusive(low: Long, high: Long): Result<Uniform<Long>, Error> =
            UniformInt.newInclusive(low, high).map { Uniform(it) }

        public fun new(low: UInt, high: UInt): Result<Uniform<UInt>, Error> =
            UniformInt.new(low, high).map { Uniform(it) }

        public fun newInclusive(low: UInt, high: UInt): Result<Uniform<UInt>, Error> =
            UniformInt.newInclusive(low, high).map { Uniform(it) }

        public fun new(low: ULong, high: ULong): Result<Uniform<ULong>, Error> =
            UniformInt.new(low, high).map { Uniform(it) }

        public fun newInclusive(low: ULong, high: ULong): Result<Uniform<ULong>, Error> =
            UniformInt.newInclusive(low, high).map { Uniform(it) }

        public fun tryFromOpenEnd(range: OpenEndRange<Int>): Result<Uniform<Int>, Error> =
            new(range.start, range.endExclusive)

        public fun tryFrom(range: ClosedRange<Int>): Result<Uniform<Int>, Error> =
            newInclusive(range.start, range.endInclusive)

        public fun tryFrom(range: UIntRange): Result<Uniform<UInt>, Error> =
            newInclusive(range.first, range.last)

        public fun tryFrom(range: LongRange): Result<Uniform<Long>, Error> =
            newInclusive(range.first, range.last)

        public fun tryFrom(range: ULongRange): Result<Uniform<ULong>, Error> =
            newInclusive(range.first, range.last)

        public fun int(low: Int, highExclusive: Int): Uniform<Int> =
            new(low, highExclusive).unwrap()

        public fun intInclusive(low: Int, highInclusive: Int): Uniform<Int> =
            newInclusive(low, highInclusive).unwrap()

        public fun long(low: Long, highExclusive: Long): Uniform<Long> =
            new(low, highExclusive).unwrap()

        public fun longInclusive(low: Long, highInclusive: Long): Uniform<Long> =
            newInclusive(low, highInclusive).unwrap()

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
            UniformInt.new(low, highExclusive).unwrap().let { Uniform(it) }

        public fun byteInclusive(low: Byte, highInclusive: Byte): Uniform<Byte> =
            UniformInt.newInclusive(low, highInclusive).unwrap().let { Uniform(it) }

        public fun short(low: Short, highExclusive: Short): Uniform<Short> =
            UniformInt.new(low, highExclusive).unwrap().let { Uniform(it) }

        public fun shortInclusive(low: Short, highInclusive: Short): Uniform<Short> =
            UniformInt.newInclusive(low, highInclusive).unwrap().let { Uniform(it) }

        public fun uInt(low: UInt, highExclusive: UInt): Uniform<UInt> =
            new(low, highExclusive).unwrap()

        public fun uIntInclusive(low: UInt, highInclusive: UInt): Uniform<UInt> =
            newInclusive(low, highInclusive).unwrap()

        public fun uLong(low: ULong, highExclusive: ULong): Uniform<ULong> =
            new(low, highExclusive).unwrap()

        public fun uLongInclusive(low: ULong, highInclusive: ULong): Uniform<ULong> =
            newInclusive(low, highInclusive).unwrap()

        public fun char(low: Char, highExclusive: Char): Uniform<Char> =
            int(low.code, highExclusive.code).map { it.toChar() }.let { Uniform { rng -> it.sample(rng) } }

        public fun charInclusive(low: Char, highInclusive: Char): Uniform<Char> =
            intInclusive(low.code, highInclusive.code).map { it.toChar() }.let { Uniform { rng -> it.sample(rng) } }
    }
}
