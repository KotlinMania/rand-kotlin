package io.github.kotlinmania.rand.distr.weighted

/**
 * Bounds on a weight.
 *
 * See usage in [WeightedIndex].
 */
public interface Weight {
    public val zero: Double

    /**
     * Checked addition.
     *
     * - On success, [v] is added to this weight.
     * - On failure (overflow), returns false and the value should be discarded.
     */
    public fun checkedAddAssign(v: Double): Boolean
}

/**
 * Implementation of [Weight] for floating-point types ([Float], [Double]).
 *
 * Floats have an explicit representation for overflow (infinity), so checked
 * addition always succeeds.
 */
public class FloatWeight : Weight {
    override var zero: Double = 0.0
        private set

    override fun checkedAddAssign(v: Double): Boolean {
        zero += v
        return true
    }
}

/**
 * Implementation of [Weight] for integer types.
 *
 * Returns false on overflow.
 */
public class IntWeight : Weight {
    override var zero: Double = 0.0
        private set

    override fun checkedAddAssign(v: Double): Boolean {
        val result = zero + v
        if (result > Long.MAX_VALUE.toDouble() || result < Long.MIN_VALUE.toDouble()) {
            return false
        }
        zero = result
        return true
    }
}

/**
 * Invalid weight errors from weighted index operations.
 *
 * This represents errors from [WeightedIndex.create], [WeightedIndex.updateWeights],
 * and other weighted distributions.
 */
public sealed class WeightError(
    message: String,
) : Exception(message) {
    /** The input weight sequence is empty, too long, or wrongly ordered. */
    public class InvalidInput(
        message: String = "Weights sequence is empty/too long/unordered",
    ) : WeightError(message)

    /** A weight is negative, too large for the distribution, or not a valid number. */
    public class InvalidWeight(
        message: String = "A weight is negative, too large or not a valid number",
    ) : WeightError(message)

    /**
     * Not enough non-zero weights are available to sample values.
     *
     * When attempting to sample a single value this implies that all weights
     * are zero. When attempting to sample `amount` values this implies that
     * less than `amount` weights are greater than zero.
     */
    public class InsufficientNonZero(
        message: String = "Not enough weights > zero",
    ) : WeightError(message)

    /** Overflow when calculating the sum of weights. */
    public class Overflow(
        message: String = "Overflow when summing weights",
    ) : WeightError(message)
}
