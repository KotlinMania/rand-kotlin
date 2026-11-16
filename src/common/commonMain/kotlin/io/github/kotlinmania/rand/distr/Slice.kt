// port-lint: source distr/slice.rs
package io.github.kotlinmania.rand.distr

import io.github.kotlinmania.rand.Rng

/**
 * A distribution to uniformly sample elements of a slice or list with replacement.
 */
public class Choose<T> private constructor(
    private val elements: List<T>,
) : Distribution<T> {
    private val range: UniformUsize = UniformUsize.new(0uL, elements.size.toULong()).unwrap()

    public val numChoices: Int
        get() = elements.size

    override fun sample(rng: Rng): T {
        val idx = range.sample(rng).toInt()
        return elements[idx]
    }

    public companion object {
        /**
         * Create a distribution sampling uniformly from the supplied elements.
         * Returns [Empty] if the list is empty.
         */
        public fun <T> new(elements: List<T>): Result<Choose<T>, Empty> =
            if (elements.isEmpty()) Result.Err(Empty()) else Result.Ok(Choose(elements))

        /** Create a distribution from an array, returning [Empty] if it is empty. */
        public fun <T> new(elements: Array<T>): Result<Choose<T>, Empty> = new(elements.toList())
    }
}

/** Error returned when [Choose.new] is given empty elements. */
public class Empty {
    override fun toString(): String = "Tried to create a `rand::distr::slice::Choose` with an empty slice"
}
