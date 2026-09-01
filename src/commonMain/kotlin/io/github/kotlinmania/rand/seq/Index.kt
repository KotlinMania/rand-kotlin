// port-lint: source seq/index.rs
package io.github.kotlinmania.rand.seq

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.weighted.WeightError
import kotlin.math.exp
import kotlin.math.ln

/**
 * A vector of indices sampled without replacement.
 */
public class IndexVec(
    private val indices: IntArray,
) : Iterable<Int> {
    public val size: Int
        get() = indices.size

    public val length: Int
        get() = indices.size

    public fun len(): Int = indices.size

    public fun isEmpty(): Boolean = indices.isEmpty()

    public operator fun get(index: Int): Int = indices[index]

    public fun index(index: Int): Int = indices[index]

    public fun toIntArray(): IntArray = indices.copyOf()

    public fun toList(): List<Int> = indices.toList()

    public fun intoVec(): List<Int> = indices.toList()

    override fun iterator(): Iterator<Int> = indices.iterator()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IndexVec) return false
        return indices.contentEquals(other.indices)
    }

    override fun hashCode(): Int = indices.contentHashCode()

    override fun toString(): String = indices.joinToString(prefix = "[", postfix = "]")

    public companion object {
        public fun from(indices: IntArray): IndexVec = IndexVec(indices.copyOf())

        public fun from(indices: List<Int>): IndexVec = IndexVec(indices.toIntArray())
    }
}

/**
 * Randomly sample exactly [amount] distinct indices from `0 until length`, and
 * return them in random order (fully shuffled).
 */
public fun sample(
    rng: Rng,
    length: Int,
    amount: Int,
): IndexVec {
    require(amount <= length) { "`amount` of samples must be less than or equal to `length`" }
    require(length >= 0 && amount >= 0) { "length and amount must be non-negative" }
    if (amount == 0) return IndexVec(IntArray(0))

    if (amount < 163) {
        val j = if (length >= 500_000) 1 else 0
        val c0 = if (j == 0) 1.6f else 8.0f / 45.0f
        val c1 = if (j == 0) 10.0f else 70.0f / 9.0f
        val amountFp = amount.toFloat()
        val m4 = c0 * amountFp

        return if (amount > 11 && length.toFloat() < (c1 + m4) * amountFp) {
            sampleInplace(rng, length, amount)
        } else {
            sampleFloyd(rng, length, amount)
        }
    } else {
        val j = if (length >= 500_000) 1 else 0
        val c = if (j == 0) 270.0f else 330.0f / 9.0f
        return if (length.toFloat() < c * amount.toFloat()) {
            sampleInplace(rng, length, amount)
        } else {
            sampleRejection(rng, length, amount)
        }
    }
}

/**
 * Randomly sample [amount] distinct indices from `0 until length` using weights.
 */
public fun sampleWeighted(
    rng: Rng,
    length: Int,
    amount: Int,
    weight: (Int) -> Double,
): IndexVec {
    require(amount <= length) { "`amount` of samples must be less than or equal to `length`" }
    require(length >= 0 && amount >= 0) { "length and amount must be non-negative" }
    return sampleEfraimidisSpirakis(rng, length, amount, weight)
}

/**
 * Randomly sample exactly [amount] indices from `0 until length`, using Floyd's
 * combination algorithm.
 */
public fun sampleFloyd(
    rng: Rng,
    length: Int,
    amount: Int,
): IndexVec {
    require(amount <= length)
    if (amount == 0) return IndexVec(IntArray(0))

    val indices = IntArray(amount)
    var count = 0
    for (j in length - amount until length) {
        val t = (rng.nextU64() % (j + 1).toULong()).toInt()
        var pos = -1
        for (i in 0 until count) {
            if (indices[i] == t) {
                pos = i
                break
            }
        }
        if (pos >= 0) {
            indices[pos] = j
        }
        indices[count++] = t
    }
    return IndexVec(indices)
}

/**
 * Randomly sample exactly [amount] indices from `0 until length`, using an inplace
 * partial Fisher-Yates method.
 */
public fun sampleInplace(
    rng: Rng,
    length: Int,
    amount: Int,
): IndexVec {
    require(amount <= length)
    if (amount == 0) return IndexVec(IntArray(0))

    val indices = IntArray(length) { it }
    for (i in 0 until amount) {
        val span = (length - i).toULong()
        val j = i + (rng.nextU64() % span).toInt()
        val tmp = indices[i]
        indices[i] = indices[j]
        indices[j] = tmp
    }
    return IndexVec(indices.copyOf(amount))
}

/**
 * Randomly sample exactly [amount] indices from `0 until length`, using rejection
 * sampling.
 */
public fun sampleRejection(
    rng: Rng,
    length: Int,
    amount: Int,
): IndexVec {
    require(amount < length || (amount == 0 && length == 0))
    if (amount == 0) return IndexVec(IntArray(0))

    val cache = HashSet<Int>(amount)
    val indices = IntArray(amount)
    val lengthULong = length.toULong()

    for (i in 0 until amount) {
        var pos = (rng.nextU64() % lengthULong).toInt()
        while (!cache.add(pos)) {
            pos = (rng.nextU64() % lengthULong).toInt()
        }
        indices[i] = pos
    }
    return IndexVec(indices)
}

private class WeightedCandidate(
    val index: Int,
    val key: Double,
) : Comparable<WeightedCandidate> {
    override fun compareTo(other: WeightedCandidate): Int = key.compareTo(other.key)
}

private class SimpleMinHeap<T : Comparable<T>>(
    capacity: Int,
) {
    private val elements = ArrayList<T>(capacity)

    val size: Int get() = elements.size

    fun isNotEmpty(): Boolean = elements.isNotEmpty()

    fun peek(): T? = elements.firstOrNull()

    fun add(element: T) {
        elements.add(element)
        siftUp(elements.size - 1)
    }

    fun poll(): T? {
        if (elements.isEmpty()) return null
        val result = elements[0]
        val last = elements.removeAt(elements.size - 1)
        if (elements.isNotEmpty()) {
            elements[0] = last
            siftDown(0)
        }
        return result
    }

    fun toList(): List<T> = elements.toList()

    private fun siftUp(index: Int) {
        var curr = index
        while (curr > 0) {
            val parent = (curr - 1) / 2
            if (elements[curr] < elements[parent]) {
                val tmp = elements[curr]
                elements[curr] = elements[parent]
                elements[parent] = tmp
                curr = parent
            } else {
                break
            }
        }
    }

    private fun siftDown(index: Int) {
        var curr = index
        val size = elements.size
        while (true) {
            var smallest = curr
            val left = 2 * curr + 1
            val right = 2 * curr + 2
            if (left < size && elements[left] < elements[smallest]) {
                smallest = left
            }
            if (right < size && elements[right] < elements[smallest]) {
                smallest = right
            }
            if (smallest != curr) {
                val tmp = elements[curr]
                elements[curr] = elements[smallest]
                elements[smallest] = tmp
                curr = smallest
            } else {
                break
            }
        }
    }
}

/**
 * Implementation based on the algorithm A-ExpJ (Efraimidis and Spirakis, 2005).
 */
public fun sampleEfraimidisSpirakis(
    rng: Rng,
    length: Int,
    amount: Int,
    weight: (Int) -> Double,
): IndexVec {
    if (amount == 0 || length == 0) {
        return IndexVec(IntArray(0))
    }

    val candidates = SimpleMinHeap<WeightedCandidate>(amount)
    var index = 0
    while (index < length && candidates.size < amount) {
        val w = weight(index)
        if (w > 0.0) {
            val u = (rng.nextU64().toDouble() / ULong.MAX_VALUE.toDouble()).coerceIn(1e-15, 1.0)
            val key = ln(u) / w
            candidates.add(WeightedCandidate(index, key))
        } else if (w.isNaN() || w < 0.0) {
            throw WeightError.InvalidWeight()
        }
        index++
    }

    if (index < length && candidates.isNotEmpty()) {
        val minKey = candidates.peek()?.key ?: 0.0
        val uInit = (rng.nextU64().toDouble() / ULong.MAX_VALUE.toDouble()).coerceIn(1e-15, 1.0)
        var x = ln(uInit) / minKey

        while (index < length) {
            val w = weight(index)
            if (w > 0.0) {
                x -= w
                if (x <= 0.0) {
                    candidates.poll()
                    val t = exp(minKey * w)
                    val u = rng.randomRange(t.coerceAtMost(1.0)..1.0).coerceIn(1e-15, 1.0)
                    val key = ln(u) / w
                    candidates.add(WeightedCandidate(index, key))

                    val newMinKey = candidates.peek()?.key ?: 0.0
                    val uNext = (rng.nextU64().toDouble() / ULong.MAX_VALUE.toDouble()).coerceIn(1e-15, 1.0)
                    x = ln(uNext) / newMinKey
                }
            } else if (w.isNaN() || w < 0.0) {
                throw WeightError.InvalidWeight()
            }
            index++
        }
    }

    val list = candidates.toList()
    val result = IntArray(list.size)
    for (i in list.indices) {
        result[i] = list[i].index
    }
    return IndexVec(result)
}
