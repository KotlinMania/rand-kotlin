// port-lint: source rand/src/seq/iterator.rs
package io.github.kotlinmania.rand.seq

import io.github.kotlinmania.rand.Rng

/**
 * Extension functions for choosing random elements from Iterators and Sequences.
 */
public fun <T> Sequence<T>.choose(rng: Rng): T? = iterator().choose(rng)

public fun <T> Sequence<T>.chooseMultiple(rng: Rng, amount: Int): List<T> = iterator().chooseMultiple(rng, amount)

public fun <T> Iterable<T>.choose(rng: Rng): T? = iterator().choose(rng)

public fun <T> Iterable<T>.chooseMultiple(rng: Rng, amount: Int): List<T> = iterator().chooseMultiple(rng, amount)

public fun <T> Iterator<T>.choose(rng: Rng): T? {
    if (!hasNext()) return null
    var result = next()
    var count = 1L
    while (hasNext()) {
        val item = next()
        count++
        val roll = (rng.nextU64() % count.toULong()).toLong()
        if (roll == 0L) {
            result = item
        }
    }
    return result
}

public fun <T> Iterator<T>.chooseMultiple(rng: Rng, amount: Int): List<T> {
    if (amount <= 0 || !hasNext()) return emptyList()
    val reservoir = ArrayList<T>(amount)
    var count = 0L
    while (hasNext() && reservoir.size < amount) {
        reservoir.add(next())
        count++
    }
    while (hasNext()) {
        val item = next()
        count++
        val roll = (rng.nextU64() % count.toULong()).toLong()
        if (roll < amount) {
            reservoir[roll.toInt()] = item
        }
    }
    reservoir.shuffle(rng)
    return reservoir
}
