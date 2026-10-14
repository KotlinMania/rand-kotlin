// port-lint: source seq/mod.rs
package io.github.kotlinmania.rand.seq

import io.github.kotlinmania.rand.Rng

/**
 * Randomly sample exactly [amount] distinct indices from `0..length`, and
 * return them in random order (fully shuffled).
 *
 * This is implemented via Floyd's algorithm. Time complexity is `O(N^2)`
 * and memory complexity is `O(N)`.
 *
 * @return null if and only if [amount] > [length].
 */
public fun sampleArray(rng: Rng, length: Int, amount: Int): IntArray? {
    if (amount > length) return null

    // Floyd's algorithm
    val indices = IntArray(amount)
    for (i in 0 until amount) {
        val j = length - amount + i
        val t = rng.randomRange(0..j)
        // Check if t is already in indices[0..i)
        var foundPos = -1
        for (k in 0 until i) {
            if (indices[k] == t) {
                foundPos = k
                break
            }
        }
        if (foundPos >= 0) {
            indices[foundPos] = j
        }
        indices[i] = t
    }
    return indices
}