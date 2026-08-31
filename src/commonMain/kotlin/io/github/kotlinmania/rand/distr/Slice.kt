// port-lint: source rand/src/distr/slice.rs
package io.github.kotlinmania.rand.distr

import io.github.kotlinmania.rand.Rng

/**
 * A distribution to uniformly sample elements of a slice or list with replacement.
 */
public class Choose<T>(
    private val elements: List<T>,
) : Distribution<T> {
    init {
        require(elements.isNotEmpty()) { "Tried to create a Choose distribution with an empty slice" }
    }

    public val numChoices: Int
        get() = elements.size

    override fun sample(rng: Rng): T {
        val idx = (rng.nextU64() % elements.size.toULong()).toInt()
        return elements[idx]
    }

    public companion object {
        public fun <T> fromList(elements: List<T>): Choose<T> = Choose(elements)

        public fun <T> fromArray(elements: Array<T>): Choose<T> = Choose(elements.toList())
    }
}
