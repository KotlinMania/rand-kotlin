// port-lint: source distr/weighted/weighted_index.rs
package io.github.kotlinmania.rand.distr.weighted

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution
import io.github.kotlinmania.rand.distr.Result
import io.github.kotlinmania.rand.distr.UniformFloat

/**
 * An update for a specific weight index.
 */
public class WeightUpdate(
    public val index: Int,
    public val weight: Double,
)

/**
 * A lazy-loading iterator over the weights of a [WeightedIndex].
 */
public class WeightedIndexIter(
    private val weightedIndex: WeightedIndex,
    private var index: Int = 0,
) : Iterator<Double> {
    override fun hasNext(): Boolean = index < weightedIndex.size

    override fun next(): Double {
        val w = weightedIndex.weight(index) ?: throw NoSuchElementException("No more weights")
        index++
        return w
    }
}

/**
 * A distribution using weighted sampling of discrete items.
 *
 * Sampling a `WeightedIndex` distribution returns the index of a randomly selected
 * element. The chance of a given element being picked is proportional to the weight.
 */
public class WeightedIndex(
    cumulativeWeights: DoubleArray,
    private var totalWeight: Double,
) : Distribution<Int> {
    private val cumulativeWeights: DoubleArray = cumulativeWeights.copyOf()
    private var weightDistribution: UniformFloat<Double> = UniformFloat.new(0.0, totalWeight).unwrap()

    public val size: Int
        get() = cumulativeWeights.size + 1

    public fun totalWeight(): Double = totalWeight

    public fun weight(index: Int): Double? {
        if (index < 0 || index > cumulativeWeights.size) return null
        var weight =
            if (index < cumulativeWeights.size) {
                cumulativeWeights[index]
            } else {
                totalWeight
            }
        if (index > 0) {
            weight -= cumulativeWeights[index - 1]
        }
        return weight
    }

    public fun weights(): List<Double> {
        val list = ArrayList<Double>(cumulativeWeights.size + 1)
        var prev = 0.0
        for (w in cumulativeWeights) {
            list.add(w - prev)
            prev = w
        }
        list.add(totalWeight - prev)
        return list
    }

    /**
     * Returns a lazy-loading iterator over the current weights.
     */
    public fun weightsIter(): WeightedIndexIter = WeightedIndexIter(this)

    /**
     * Updates weights, returning validation errors without changing the distribution.
     * Sampler initialization failures still throw after validated updates are applied.
     */
    public fun updateWeights(newWeights: List<WeightUpdate>): Result<Unit, WeightError> {
        if (newWeights.isEmpty()) return Result.Ok(Unit)

        var prevI: Int? = null
        var updatedTotal = totalWeight
        for (update in newWeights) {
            val i = update.index
            val w = update.weight
            if (prevI != null && prevI >= i) {
                return Result.Err(WeightError.InvalidInput())
            }
            if (w.isNaN() || w < 0.0) {
                return Result.Err(WeightError.InvalidWeight())
            }
            if (i > cumulativeWeights.size || i < 0) {
                return Result.Err(WeightError.InvalidInput())
            }

            var oldW = if (i < cumulativeWeights.size) cumulativeWeights[i] else totalWeight
            if (i > 0) {
                oldW -= cumulativeWeights[i - 1]
            }

            updatedTotal -= oldW
            updatedTotal += w
            prevI = i
        }

        if (updatedTotal <= 0.0) {
            return Result.Err(WeightError.InsufficientNonZero())
        }

        // Apply updates
        var iterIdx = 0
        var prevWeight = 0.0
        val firstNewIndex = newWeights[0].index
        var cumulativeWeight = if (firstNewIndex > 0) cumulativeWeights[firstNewIndex - 1] else 0.0

        for (i in firstNewIndex until cumulativeWeights.size) {
            if (iterIdx < newWeights.size && i == newWeights[iterIdx].index) {
                cumulativeWeight += newWeights[iterIdx].weight
                iterIdx++
            } else {
                val tmp = cumulativeWeights[i] - prevWeight
                cumulativeWeight += tmp
            }
            prevWeight = cumulativeWeights[i]
            cumulativeWeights[i] = cumulativeWeight
        }

        totalWeight = updatedTotal
        weightDistribution = UniformFloat.new(0.0, totalWeight).unwrap()
        return Result.Ok(Unit)
    }

    override fun sample(rng: Rng): Int {
        val chosen = weightDistribution.sample(rng)
        var low = 0
        var high = cumulativeWeights.size
        while (low < high) {
            val mid = (low + high) ushr 1
            if (cumulativeWeights[mid] <= chosen) {
                low = mid + 1
            } else {
                high = mid
            }
        }
        return low
    }

    public companion object {
        public fun create(weights: DoubleArray): WeightedIndex = createIterator(weights.iterator())

        public fun create(weights: List<Double>): WeightedIndex = createIterator(weights.iterator())

        private fun createIterator(weights: Iterator<Double>): WeightedIndex =
            when (val result = newIterator(weights)) {
                is Result.Ok -> result.value
                is Result.Err -> throw result.error
            }

        private fun newIterator(weights: Iterator<Double>): Result<WeightedIndex, WeightError> {
            if (!weights.hasNext()) return Result.Err(WeightError.InvalidInput())

            var total = weights.next()
            if (total.isNaN() || total < 0.0) return Result.Err(WeightError.InvalidWeight())

            val cumulative = ArrayList<Double>()
            while (weights.hasNext()) {
                val weight = weights.next()
                if (weight.isNaN() || weight < 0.0) return Result.Err(WeightError.InvalidWeight())
                cumulative.add(total)
                total += weight
            }

            if (total == 0.0) return Result.Err(WeightError.InsufficientNonZero())

            return Result.Ok(WeightedIndex(cumulative.toDoubleArray(), total))
        }

        public fun fromInts(weights: IntArray): WeightedIndex =
            create(DoubleArray(weights.size) { weights[it].toDouble() })

        public fun fromInts(weights: List<Int>): WeightedIndex =
            create(DoubleArray(weights.size) { weights[it].toDouble() })

        public fun fromLongs(weights: LongArray): WeightedIndex =
            create(DoubleArray(weights.size) { weights[it].toDouble() })

        public fun fromLongs(weights: List<Long>): WeightedIndex =
            create(DoubleArray(weights.size) { weights[it].toDouble() })

        public fun fromFloats(weights: FloatArray): WeightedIndex =
            create(DoubleArray(weights.size) { weights[it].toDouble() })

        public fun fromFloats(weights: List<Float>): WeightedIndex =
            create(DoubleArray(weights.size) { weights[it].toDouble() })

        public fun fromUInts(weights: UIntArray): WeightedIndex =
            create(DoubleArray(weights.size) { weights[it].toDouble() })

        public fun fromULongs(weights: ULongArray): WeightedIndex =
            create(DoubleArray(weights.size) { weights[it].toDouble() })

        /**
         * Creates a [WeightedIndex] from [weights], returning a [Result].
         *
         * Error cases:
         * - [WeightError.InvalidInput] when [weights] is empty.
         * - [WeightError.InvalidWeight] when a weight is NaN or negative.
         * - [WeightError.InsufficientNonZero] when the sum of all weights is zero.
         *
         * A non-finite total cannot initialize the floating-point sampler and throws
         * instead of returning [WeightError.Overflow].
         */
        public fun new(weights: DoubleArray): io.github.kotlinmania.rand.distr.Result<WeightedIndex, WeightError> =
            newIterator(weights.iterator())

        /**
         * Creates a [WeightedIndex] from [weights], returning a [Result].
         */
        public fun new(weights: List<Double>): io.github.kotlinmania.rand.distr.Result<WeightedIndex, WeightError> =
            newIterator(weights.iterator())

        /** Creates a distribution while consuming weights only through the first validation error. */
        public fun new(weights: Iterator<Double>): Result<WeightedIndex, WeightError> = newIterator(weights)

        /**
         * Creates a [WeightedIndex] from integer [weights], returning a [Result].
         */
        public fun newFromInts(weights: IntArray): io.github.kotlinmania.rand.distr.Result<WeightedIndex, WeightError> =
            new(DoubleArray(weights.size) { weights[it].toDouble() })

        /**
         * Creates a [WeightedIndex] from integer [weights], returning a [Result].
         */
        public fun newFromInts(weights: List<Int>): io.github.kotlinmania.rand.distr.Result<WeightedIndex, WeightError> =
            new(DoubleArray(weights.size) { weights[it].toDouble() })

        /**
         * Creates a [WeightedIndex] from long [weights], returning a [Result].
         */
        public fun newFromLongs(weights: LongArray): io.github.kotlinmania.rand.distr.Result<WeightedIndex, WeightError> =
            new(DoubleArray(weights.size) { weights[it].toDouble() })

        /**
         * Creates a [WeightedIndex] from long [weights], returning a [Result].
         */
        public fun newFromLongs(weights: List<Long>): io.github.kotlinmania.rand.distr.Result<WeightedIndex, WeightError> =
            new(DoubleArray(weights.size) { weights[it].toDouble() })

        /**
         * Creates a [WeightedIndex] from float [weights], returning a [Result].
         */
        public fun newFromFloats(weights: FloatArray): io.github.kotlinmania.rand.distr.Result<WeightedIndex, WeightError> =
            new(DoubleArray(weights.size) { weights[it].toDouble() })

        /** Delegates weight updates to the distribution's typed-result API. */
        public fun updateWeightsResult(
            index: WeightedIndex,
            newWeights: List<WeightUpdate>,
        ): Result<Unit, WeightError> = index.updateWeights(newWeights)
    }
}
