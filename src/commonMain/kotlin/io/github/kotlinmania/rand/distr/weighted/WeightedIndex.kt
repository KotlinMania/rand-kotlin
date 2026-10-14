// port-lint: source distr/weighted/weighted_index.rs
package io.github.kotlinmania.rand.distr.weighted

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Distribution

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

    public fun updateWeights(newWeights: List<WeightUpdate>) {
        if (newWeights.isEmpty()) return

        var prevI: Int? = null
        var updatedTotal = totalWeight
        for (update in newWeights) {
            val i = update.index
            val w = update.weight
            if (prevI != null && prevI >= i) {
                throw WeightError.InvalidInput()
            }
            if (w.isNaN() || w < 0.0) {
                throw WeightError.InvalidWeight()
            }
            if (i > cumulativeWeights.size || i < 0) {
                throw WeightError.InvalidInput()
            }

            var oldW = if (i < cumulativeWeights.size) cumulativeWeights[i] else totalWeight
            if (i > 0) {
                oldW -= cumulativeWeights[i - 1]
            }

            updatedTotal -= oldW
            updatedTotal += w
            prevI = i
        }

        if (updatedTotal <= 0.0 || updatedTotal.isNaN()) {
            throw WeightError.InsufficientNonZero()
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
    }

    override fun sample(rng: Rng): Int {
        val chosen = (rng.nextU64().toDouble() / ULong.MAX_VALUE.toDouble()) * totalWeight
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
        public fun create(weights: DoubleArray): WeightedIndex {
            if (weights.isEmpty()) throw WeightError.InvalidInput()

            var total = weights[0]
            if (total.isNaN() || total < 0.0) throw WeightError.InvalidWeight()

            val cumulative = DoubleArray(weights.size - 1)
            for (i in 1 until weights.size) {
                val w = weights[i]
                if (w.isNaN() || w < 0.0) throw WeightError.InvalidWeight()
                cumulative[i - 1] = total
                total += w
                if (total.isInfinite()) throw WeightError.Overflow()
            }

            if (total <= 0.0 || total.isNaN()) throw WeightError.InsufficientNonZero()

            return WeightedIndex(cumulative, total)
        }

        public fun create(weights: List<Double>): WeightedIndex = create(weights.toDoubleArray())

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
         * - [WeightError.Overflow] when the sum of all weights overflows.
         */
        public fun new(weights: DoubleArray): io.github.kotlinmania.rand.distr.Result<WeightedIndex, WeightError> {
            return try {
                io.github.kotlinmania.rand.distr.Ok(create(weights))
            } catch (e: WeightError) {
                io.github.kotlinmania.rand.distr.Err(e)
            }
        }

        /**
         * Creates a [WeightedIndex] from [weights], returning a [Result].
         */
        public fun new(weights: List<Double>): io.github.kotlinmania.rand.distr.Result<WeightedIndex, WeightError> =
            new(weights.toDoubleArray())

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

        /**
         * Updates weights, returning a [Result] instead of throwing.
         */
        public fun updateWeightsResult(
            index: WeightedIndex,
            newWeights: List<WeightUpdate>,
        ): io.github.kotlinmania.rand.distr.Result<Unit, WeightError> {
            return try {
                index.updateWeights(newWeights)
                io.github.kotlinmania.rand.distr.Ok(Unit)
            } catch (e: WeightError) {
                io.github.kotlinmania.rand.distr.Err(e)
            }
        }
    }
}
