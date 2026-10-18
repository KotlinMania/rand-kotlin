// port-lint: source seq/slice.rs
package io.github.kotlinmania.rand.seq

import io.github.kotlinmania.rand.Rng
import kotlin.math.min
import kotlin.math.pow

/**
 * Extension functions for choosing random elements from indexable collections.
 */
public fun <T> List<T>.choose(rng: Rng): T? {
    if (isEmpty()) return null
    val idx = (rng.nextU64() % size.toULong()).toInt()
    return this[idx]
}

public fun <T> Array<T>.choose(rng: Rng): T? {
    if (isEmpty()) return null
    val idx = (rng.nextU64() % size.toULong()).toInt()
    return this[idx]
}

public fun IntArray.choose(rng: Rng): Int? {
    if (isEmpty()) return null
    val idx = (rng.nextU64() % size.toULong()).toInt()
    return this[idx]
}

public fun LongArray.choose(rng: Rng): Long? {
    if (isEmpty()) return null
    val idx = (rng.nextU64() % size.toULong()).toInt()
    return this[idx]
}

public fun ByteArray.choose(rng: Rng): Byte? {
    if (isEmpty()) return null
    val idx = (rng.nextU64() % size.toULong()).toInt()
    return this[idx]
}

public fun <T> List<T>.chooseMultiple(rng: Rng, amount: Int): List<T> {
    if (amount <= 0 || isEmpty()) return emptyList()
    val k = min(amount, size)
    val indices = sampleIndices(rng, size, k)
    return indices.map { this[it] }
}

public fun <T> Array<T>.chooseMultiple(rng: Rng, amount: Int): List<T> {
    if (amount <= 0 || isEmpty()) return emptyList()
    val k = min(amount, size)
    val indices = sampleIndices(rng, size, k)
    return indices.map { this[it] }
}

public fun <T> List<T>.chooseWeighted(rng: Rng, weight: (T) -> Double): T? {
    if (isEmpty()) return null
    val weights = map(weight)
    val totalWeight = weights.sum()
    require(totalWeight > 0.0) { "total weight must be positive" }
    var roll = (rng.nextU64().toDouble() / ULong.MAX_VALUE.toDouble()) * totalWeight
    for (i in indices) {
        roll -= weights[i]
        if (roll <= 0.0) return this[i]
    }
    return last()
}

public fun <T> Array<T>.chooseWeighted(rng: Rng, weight: (T) -> Double): T? {
    if (isEmpty()) return null
    val weights = map(weight)
    val totalWeight = weights.sum()
    require(totalWeight > 0.0) { "total weight must be positive" }
    var roll = (rng.nextU64().toDouble() / ULong.MAX_VALUE.toDouble()) * totalWeight
    for (i in indices) {
        roll -= weights[i]
        if (roll <= 0.0) return this[i]
    }
    return last()
}

public fun <T> List<T>.chooseMultipleWeighted(rng: Rng, amount: Int, weight: (T) -> Double): List<T> {
    if (amount <= 0 || isEmpty()) return emptyList()
    val candidates =
        mapIndexed { index, item ->
            val w = weight(item)
            if (w > 0.0) {
                val r = (rng.nextU64().toDouble() / ULong.MAX_VALUE.toDouble()).coerceIn(1e-15, 1.0)
                val score = r.pow(1.0 / w)
                index to score
            } else {
                null
            }
        }.filterNotNull()
    return candidates
        .sortedByDescending { it.second }
        .take(amount)
        .map { this[it.first] }
}

/**
 * Extension functions for shuffling mutable collections and arrays.
 */
public fun <T> MutableList<T>.shuffle(rng: Rng) {
    for (i in size - 1 downTo 1) {
        val j = (rng.nextU64() % (i + 1).toULong()).toInt()
        val tmp = this[i]
        this[i] = this[j]
        this[j] = tmp
    }
}

public fun <T> Array<T>.shuffle(rng: Rng) {
    for (i in size - 1 downTo 1) {
        val j = (rng.nextU64() % (i + 1).toULong()).toInt()
        val tmp = this[i]
        this[i] = this[j]
        this[j] = tmp
    }
}

public fun IntArray.shuffle(rng: Rng) {
    for (i in size - 1 downTo 1) {
        val j = (rng.nextU64() % (i + 1).toULong()).toInt()
        val tmp = this[i]
        this[i] = this[j]
        this[j] = tmp
    }
}

public fun LongArray.shuffle(rng: Rng) {
    for (i in size - 1 downTo 1) {
        val j = (rng.nextU64() % (i + 1).toULong()).toInt()
        val tmp = this[i]
        this[i] = this[j]
        this[j] = tmp
    }
}

public fun ByteArray.shuffle(rng: Rng) {
    for (i in size - 1 downTo 1) {
        val j = (rng.nextU64() % (i + 1).toULong()).toInt()
        val tmp = this[i]
        this[i] = this[j]
        this[j] = tmp
    }
}

public fun DoubleArray.shuffle(rng: Rng) {
    for (i in size - 1 downTo 1) {
        val j = (rng.nextU64() % (i + 1).toULong()).toInt()
        val tmp = this[i]
        this[i] = this[j]
        this[j] = tmp
    }
}

internal fun sampleIndices(rng: Rng, length: Int, amount: Int): List<Int> {
    if (amount <= 0 || length <= 0) return emptyList()
    val k = min(amount, length)
    // Floyd's algorithm
    val indices = IntArray(k)
    for ((idx, j) in (length - k until length).withIndex()) {
        val t = (rng.nextU64() % (j + 1).toULong()).toInt()
        var pos = -1
        for (p in 0 until idx) {
            if (indices[p] == t) {
                pos = p
                break
            }
        }
        if (pos >= 0) {
            indices[pos] = j
        }
        indices[idx] = t
    }
    return indices.toList()
}

// ---------------------------------------------------------------------------
// IndexedMutRandom: chooseMut — returns the index for mutable access
// ---------------------------------------------------------------------------

/**
 * Uniformly sample one element, returning its index for mutable access.
 *
 * Returns the index of one uniformly-sampled random element, or null if the list is empty.
 */
public fun <T> MutableList<T>.chooseMut(rng: Rng): Int? {
    if (isEmpty()) return null
    return (rng.nextU64() % size.toULong()).toInt()
}

/**
 * Uniformly sample one element, returning its index for mutable access.
 *
 * Returns the index of one uniformly-sampled random element, or null if the array is empty.
 */
public fun <T> Array<T>.chooseMutIndex(rng: Rng): Int? {
    if (isEmpty()) return null
    return (rng.nextU64() % size.toULong()).toInt()
}

// ---------------------------------------------------------------------------
// IndexedRandom: chooseMultipleArray — fixed-size array of distinct elements
// ---------------------------------------------------------------------------

/**
 * Uniformly sample a fixed-size array of distinct elements from this list.
 *
 * Chooses [amount] elements at random, without repetition, and in random order.
 *
 * @return null if and only if [amount] > size.
 */
public fun <T> List<T>.chooseMultipleArray(rng: Rng, amount: Int): List<T>? {
    val indices = sampleArray(rng, size, amount) ?: return null
    return List(amount) { this[indices[it]] }
}

/**
 * Uniformly sample a fixed-size array of distinct elements from this array.
 *
 * Chooses [amount] elements at random, without repetition, and in random order.
 *
 * @return null if and only if [amount] > size.
 */
public fun <T> Array<T>.chooseMultipleArray(rng: Rng, amount: Int): List<T>? {
    val indices = sampleArray(rng, size, amount) ?: return null
    return List(amount) { this[indices[it]] }
}

// ---------------------------------------------------------------------------
// IndexedMutRandom: chooseWeightedMut — biased sampling returning index
// ---------------------------------------------------------------------------

/**
 * Biased sampling for one element, returning its index for mutable access.
 *
 * Returns the index of one element sampled according to the provided [weight] function,
 * or null if the list is empty.
 */
public fun <T> MutableList<T>.chooseWeightedMut(rng: Rng, weight: (T) -> Double): Int? {
    if (isEmpty()) return null
    val weights = map(weight)
    val totalWeight = weights.sum()
    require(totalWeight > 0.0) { "total weight must be positive" }
    var roll = (rng.nextU64().toDouble() / ULong.MAX_VALUE.toDouble()) * totalWeight
    for (i in indices) {
        roll -= weights[i]
        if (roll <= 0.0) return i
    }
    return lastIndex
}

/**
 * Biased sampling for one element, returning its index for mutable access.
 *
 * Returns the index of one element sampled according to the provided [weight] function,
 * or null if the array is empty.
 */
public fun <T> Array<T>.chooseWeightedMutIndex(rng: Rng, weight: (T) -> Double): Int? {
    if (isEmpty()) return null
    val weights = map(weight)
    val totalWeight = weights.sum()
    require(totalWeight > 0.0) { "total weight must be positive" }
    var roll = (rng.nextU64().toDouble() / ULong.MAX_VALUE.toDouble()) * totalWeight
    for (i in indices) {
        roll -= weights[i]
        if (roll <= 0.0) return i
    }
    return lastIndex
}

// ---------------------------------------------------------------------------
// IndexedRandom: chooseMultiple for primitive arrays
// ---------------------------------------------------------------------------

/**
 * Uniformly sample [amount] distinct elements from this IntArray.
 */
public fun IntArray.chooseMultiple(rng: Rng, amount: Int): List<Int> {
    if (amount <= 0 || isEmpty()) return emptyList()
    val k = min(amount, size)
    val indices = sampleIndices(rng, size, k)
    return indices.map { this[it] }
}

/**
 * Uniformly sample [amount] distinct elements from this LongArray.
 */
public fun LongArray.chooseMultiple(rng: Rng, amount: Int): List<Long> {
    if (amount <= 0 || isEmpty()) return emptyList()
    val k = min(amount, size)
    val indices = sampleIndices(rng, size, k)
    return indices.map { this[it] }
}

/**
 * Uniformly sample [amount] distinct elements from this ByteArray.
 */
public fun ByteArray.chooseMultiple(rng: Rng, amount: Int): List<Byte> {
    if (amount <= 0 || isEmpty()) return emptyList()
    val k = min(amount, size)
    val indices = sampleIndices(rng, size, k)
    return indices.map { this[it] }
}

/**
 * Uniformly sample [amount] distinct elements from this ShortArray.
 */
public fun ShortArray.chooseMultiple(rng: Rng, amount: Int): List<Short> {
    if (amount <= 0 || isEmpty()) return emptyList()
    val k = min(amount, size)
    val indices = sampleIndices(rng, size, k)
    return indices.map { this[it] }
}

/**
 * Uniformly sample [amount] distinct elements from this DoubleArray.
 */
public fun DoubleArray.chooseMultiple(rng: Rng, amount: Int): List<Double> {
    if (amount <= 0 || isEmpty()) return emptyList()
    val k = min(amount, size)
    val indices = sampleIndices(rng, size, k)
    return indices.map { this[it] }
}

/**
 * Uniformly sample [amount] distinct elements from this FloatArray.
 */
public fun FloatArray.chooseMultiple(rng: Rng, amount: Int): List<Float> {
    if (amount <= 0 || isEmpty()) return emptyList()
    val k = min(amount, size)
    val indices = sampleIndices(rng, size, k)
    return indices.map { this[it] }
}

// ---------------------------------------------------------------------------
// IndexedRandom: choose for additional primitive arrays
// ---------------------------------------------------------------------------

public fun ShortArray.choose(rng: Rng): Short? {
    if (isEmpty()) return null
    val idx = (rng.nextU64() % size.toULong()).toInt()
    return this[idx]
}

public fun DoubleArray.choose(rng: Rng): Double? {
    if (isEmpty()) return null
    val idx = (rng.nextU64() % size.toULong()).toInt()
    return this[idx]
}

public fun FloatArray.choose(rng: Rng): Float? {
    if (isEmpty()) return null
    val idx = (rng.nextU64() % size.toULong()).toInt()
    return this[idx]
}

// ---------------------------------------------------------------------------
// IndexedRandom: chooseWeighted for primitive arrays
// ---------------------------------------------------------------------------

/**
 * Biased sampling for one element from this IntArray.
 */
public fun IntArray.chooseWeighted(rng: Rng, weight: (Int) -> Double): Int? {
    if (isEmpty()) return null
    val weights = map(weight)
    val totalWeight = weights.sum()
    require(totalWeight > 0.0) { "total weight must be positive" }
    var roll = (rng.nextU64().toDouble() / ULong.MAX_VALUE.toDouble()) * totalWeight
    for (i in indices) {
        roll -= weights[i]
        if (roll <= 0.0) return this[i]
    }
    return last()
}

/**
 * Biased sampling for one element from this LongArray.
 */
public fun LongArray.chooseWeighted(rng: Rng, weight: (Long) -> Double): Long? {
    if (isEmpty()) return null
    val weights = map(weight)
    val totalWeight = weights.sum()
    require(totalWeight > 0.0) { "total weight must be positive" }
    var roll = (rng.nextU64().toDouble() / ULong.MAX_VALUE.toDouble()) * totalWeight
    for (i in indices) {
        roll -= weights[i]
        if (roll <= 0.0) return this[i]
    }
    return last()
}

/**
 * Biased sampling for one element from this DoubleArray.
 */
public fun DoubleArray.chooseWeighted(rng: Rng, weight: (Double) -> Double): Double? {
    if (isEmpty()) return null
    val weights = map(weight)
    val totalWeight = weights.sum()
    require(totalWeight > 0.0) { "total weight must be positive" }
    var roll = (rng.nextU64().toDouble() / ULong.MAX_VALUE.toDouble()) * totalWeight
    for (i in indices) {
        roll -= weights[i]
        if (roll <= 0.0) return this[i]
    }
    return last()
}

// ---------------------------------------------------------------------------
// IndexedRandom: chooseMultipleWeighted for Array
// ---------------------------------------------------------------------------

/**
 * Biased sampling of [amount] distinct elements from this array.
 *
 * Zero-weighted elements are never returned; the result may contain fewer
 * elements than [amount] even when size >= [amount].
 */
public fun <T> Array<T>.chooseMultipleWeighted(rng: Rng, amount: Int, weight: (T) -> Double): List<T> {
    if (amount <= 0 || isEmpty()) return emptyList()
    val candidates =
        mapIndexed { index, item ->
            val w = weight(item)
            if (w > 0.0) {
                val r = (rng.nextU64().toDouble() / ULong.MAX_VALUE.toDouble()).coerceIn(1e-15, 1.0)
                val score = r.pow(1.0 / w)
                index to score
            } else {
                null
            }
        }.filterNotNull()
    return candidates
        .sortedByDescending { it.second }
        .take(amount)
        .map { this[it.first] }
}

// ---------------------------------------------------------------------------
// SliceRandom: partialShuffle
// ---------------------------------------------------------------------------

/**
 * Shuffle this list in place, but exit early.
 *
 * Returns a pair of lists: the first contains [amount] elements randomly permuted,
 * the second has the remaining elements that are not fully shuffled.
 *
 * If [amount] is greater than the size, this performs a full shuffle.
 */
public fun <T> MutableList<T>.partialShuffle(rng: Rng, amount: Int): Pair<List<T>, List<T>> {
    val m = (size - amount).coerceAtLeast(0)
    for (i in m until size) {
        val j = (rng.nextU64() % (i + 1).toULong()).toInt()
        val tmp = this[i]
        this[i] = this[j]
        this[j] = tmp
    }
    return Pair(
        subList(m, size).toList(),
        subList(0, m).toList(),
    )
}

/**
 * Shuffle this array in place, but exit early.
 *
 * Returns a pair of lists: the first contains [amount] elements randomly permuted,
 * the second has the remaining elements that are not fully shuffled.
 */
public fun <T> Array<T>.partialShuffle(rng: Rng, amount: Int): Pair<List<T>, List<T>> {
    val m = (size - amount).coerceAtLeast(0)
    for (i in m until size) {
        val j = (rng.nextU64() % (i + 1).toULong()).toInt()
        val tmp = this[i]
        this[i] = this[j]
        this[j] = tmp
    }
    return Pair(
        toList().subList(m, size),
        toList().subList(0, m),
    )
}

/**
 * Shuffle this IntArray in place, but exit early.
 */
public fun IntArray.partialShuffle(rng: Rng, amount: Int): Pair<List<Int>, List<Int>> {
    val m = (size - amount).coerceAtLeast(0)
    for (i in m until size) {
        val j = (rng.nextU64() % (i + 1).toULong()).toInt()
        val tmp = this[i]
        this[i] = this[j]
        this[j] = tmp
    }
    return Pair(
        toList().subList(m, size),
        toList().subList(0, m),
    )
}

/**
 * Shuffle this LongArray in place, but exit early.
 */
public fun LongArray.partialShuffle(rng: Rng, amount: Int): Pair<List<Long>, List<Long>> {
    val m = (size - amount).coerceAtLeast(0)
    for (i in m until size) {
        val j = (rng.nextU64() % (i + 1).toULong()).toInt()
        val tmp = this[i]
        this[i] = this[j]
        this[j] = tmp
    }
    return Pair(
        toList().subList(m, size),
        toList().subList(0, m),
    )
}

// ---------------------------------------------------------------------------
// SliceRandom: shuffle for additional primitive arrays
// ---------------------------------------------------------------------------

public fun ShortArray.shuffle(rng: Rng) {
    for (i in size - 1 downTo 1) {
        val j = (rng.nextU64() % (i + 1).toULong()).toInt()
        val tmp = this[i]
        this[i] = this[j]
        this[j] = tmp
    }
}

public fun FloatArray.shuffle(rng: Rng) {
    for (i in size - 1 downTo 1) {
        val j = (rng.nextU64() % (i + 1).toULong()).toInt()
        val tmp = this[i]
        this[i] = this[j]
        this[j] = tmp
    }
}

// ---------------------------------------------------------------------------
// SliceChooseIter: iterator over multiple slice elements
// ---------------------------------------------------------------------------

/**
 * An iterator over multiple slice elements.
 *
 * Created by [chooseMultiple]. Yields elements from the original slice
 * at randomly selected indices.
 */
public class SliceChooseIter<T> internal constructor(
    private val slice: List<T>,
    private val indices: Iterator<Int>,
) : Iterator<T> {
    override fun hasNext(): Boolean = indices.hasNext()

    override fun next(): T = slice[indices.next()]
}
