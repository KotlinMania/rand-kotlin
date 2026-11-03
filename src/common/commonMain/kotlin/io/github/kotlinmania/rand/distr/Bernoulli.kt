// port-lint: source distr/bernoulli.rs
package io.github.kotlinmania.rand.distr

import io.github.kotlinmania.rand.Rng

private const val ALWAYS_TRUE: ULong = ULong.MAX_VALUE
private const val SCALE: Double = 2.0 * 9223372036854775808.0

/**
 * Error type returned from Bernoulli construction.
 */
public enum class BernoulliError {
    /** Probability is outside the interval [0, 1]. */
    InvalidProbability,
    ;

    override fun toString(): String = "p is outside [0, 1] in Bernoulli distribution"
}

/**
 * The Bernoulli distribution `Bernoulli(p)`.
 */
public data class Bernoulli(
    private val pInt: ULong,
) : Distribution<Boolean> {
    public fun p(): Double =
        if (pInt == ALWAYS_TRUE) {
            1.0
        } else {
            pInt.toDouble() / SCALE
        }

    override fun sample(rng: Rng): Boolean {
        if (pInt == ALWAYS_TRUE) {
            return true
        }
        val v = rng.nextU64()
        return v < pInt
    }

    public companion object {
        /**
         * Construct a new [Bernoulli] with the given probability of success [p].
         */
        public fun create(p: Double): Bernoulli {
            require(p in 0.0..1.0) { "p is outside [0, 1] in Bernoulli distribution: $p" }
            if (p == 1.0) {
                return Bernoulli(ALWAYS_TRUE)
            }
            val pInt = (p * SCALE).toULong()
            return Bernoulli(pInt)
        }

        /**
         * Construct a new [Bernoulli] with the probability of success of [numerator]-in-[denominator].
         */
        public fun fromRatio(numerator: UInt, denominator: UInt): Result<Bernoulli, BernoulliError> {
            if (numerator > denominator || denominator == 0u) {
                return Result.Err(BernoulliError.InvalidProbability)
            }
            if (numerator == denominator) {
                return Result.Ok(Bernoulli(ALWAYS_TRUE))
            }
            val pInt = ((numerator.toDouble() / denominator.toDouble()) * SCALE).toULong()
            return Result.Ok(Bernoulli(pInt))
        }
    }
}
