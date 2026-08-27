// port-lint: source rand/src/distr/bernoulli.rs
package io.github.kotlinmania.rand.distr

import io.github.kotlinmania.rand.Rng

private const val ALWAYS_TRUE: ULong = ULong.MAX_VALUE
private const val SCALE: Double = 2.0 * 9223372036854775808.0

/**
 * The Bernoulli distribution `Bernoulli(p)`.
 */
public data class Bernoulli(
    private val pInt: ULong,
) : Distribution<Boolean> {

    public fun p(): Double {
        return if (pInt == ALWAYS_TRUE) {
            1.0
        } else {
            pInt.toDouble() / SCALE
        }
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
        public fun fromRatio(numerator: UInt, denominator: UInt): Bernoulli {
            require(denominator > 0u && numerator <= denominator) {
                "Invalid ratio: $numerator / $denominator"
            }
            if (numerator == denominator) {
                return Bernoulli(ALWAYS_TRUE)
            }
            val pInt = ((numerator.toDouble() / denominator.toDouble()) * SCALE).toULong()
            return Bernoulli(pInt)
        }
    }
}
