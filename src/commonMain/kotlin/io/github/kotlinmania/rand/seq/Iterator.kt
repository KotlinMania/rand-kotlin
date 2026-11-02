// port-lint: source seq/iterator.rs
package io.github.kotlinmania.rand.seq

import io.github.kotlinmania.rand.Rng
import io.github.kotlinmania.rand.distr.Uniform

/**
 * Extension functions for choosing random elements from Iterators and Sequences.
 */
public fun <T> Sequence<T>.choose(rng: Rng): T? = iterator().choose(rng)

public fun <T> Sequence<T>.chooseMultiple(rng: Rng, amount: Int): List<T> = iterator().chooseMultiple(rng, amount)

public fun <T> Iterable<T>.choose(rng: Rng): T? = iterator().choose(rng)

public fun <T> Iterable<T>.chooseMultiple(rng: Rng, amount: Int): List<T> = iterator().chooseMultiple(rng, amount)

/**
 * Uniformly sample one element, or return null if the iterator is empty.
 *
 * With no size hint available, complexity is O(n), where n is the iterator
 * length. The number of RNG samples and the selected element can depend on
 * iterator size hints when those are available.
 */
public fun <T> Iterator<T>.choose(rng: Rng): T? {
    val coinFlipper = CoinFlipper(rng)
    var consumed = 0uL
    var result: T? = null
    while (hasNext()) {
        val item = next()
        consumed++
        if (coinFlipper.randomRatioOneOver(consumed)) {
            result = item
        }
    }
    return result
}

/**
 * Uniformly sample [amount] distinct elements into a list.
 *
 * This is equivalent to [chooseMultipleFill] except for the result type.
 * Although elements are selected randomly, their order is neither stable nor
 * fully random. Shuffle the result if random ordering is desired.
 *
 * The returned length equals [amount] unless the iterator contains insufficient
 * elements, in which case it equals the number available.
 * Complexity is O(n), where n is the length of the iterator.
 */
public fun <T> Iterator<T>.chooseMultiple(rng: Rng, amount: Int): List<T> {
    require(amount >= 0) { "amount must be non-negative" }
    val reservoir = ArrayList<T>(amount)
    while (reservoir.size < amount) {
        if (!hasNext()) return reservoir
        reservoir.add(next())
    }
    var consumed = amount.toULong()
    while (hasNext()) {
        val item = next()
        consumed++
        val index = sampleIteratorIndex(rng, consumed)
        if (index < amount.toULong()) {
            reservoir[index.toInt()] = item
        }
    }
    return reservoir
}

/**
 * Uniformly sample distinct elements into [buf].
 *
 * Collects values at random from the iterator into the supplied buffer until
 * it is filled. Their order is neither stable nor fully random. Shuffle the
 * result if random ordering is desired.
 *
 * Returns the number of elements added. This equals the buffer length unless
 * the iterator contains insufficient elements, in which case it equals the
 * number available. Complexity is O(n), where n is the iterator length.
 */
public fun <T> Iterator<T>.chooseMultipleFill(rng: Rng, buf: Array<T>): Int {
    val amount = buf.size
    var len = 0
    while (len < amount) {
        if (!hasNext()) return len
        buf[len] = next()
        len++
    }
    var consumed = amount.toULong()
    while (hasNext()) {
        val item = next()
        consumed++
        val index = sampleIteratorIndex(rng, consumed)
        if (index < amount.toULong()) {
            buf[index.toInt()] = item
        }
    }
    return len
}

internal fun sampleIteratorIndex(rng: Rng, upperExclusive: ULong): ULong =
    if (upperExclusive > UInt.MAX_VALUE.toULong()) {
        Uniform.sampleSingle(0uL, upperExclusive, rng).unwrap()
    } else {
        Uniform.sampleSingle(0u, upperExclusive.toUInt(), rng).unwrap().toULong()
    }
