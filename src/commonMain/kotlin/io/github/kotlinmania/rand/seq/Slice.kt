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
