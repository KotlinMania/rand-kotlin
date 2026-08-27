// port-lint: source rand/src/distr/distribution.rs
package io.github.kotlinmania.rand.distr

import io.github.kotlinmania.rand.Rng

/**
 * Types (distributions) that can be used to create a random instance of [T].
 */
public fun interface Distribution<out T> {
    /**
     * Generate a random value of [T], using [rng] as the source of randomness.
     */
    public fun sample(rng: Rng): T
}

/**
 * Create an iterator/sequence that generates random values of [T].
 */
public fun <T> Distribution<T>.sampleIter(rng: Rng): Iter<T> = Iter(this, rng)

/**
 * Map sampled values to type [S].
 */
public fun <T, S> Distribution<T>.map(transform: (T) -> S): MapDistribution<T, S> = MapDistribution(this, transform)

/**
 * An iterator over a [Distribution].
 */
public class Iter<T>(
    private val distr: Distribution<T>,
    private val rng: Rng,
) : Iterator<T> {
    override fun hasNext(): Boolean = true

    override fun next(): T = distr.sample(rng)
}

/**
 * A [Distribution] which maps sampled values to type [S].
 */
public class MapDistribution<T, S>(
    public val distr: Distribution<T>,
    public val func: (T) -> S,
) : Distribution<S> {
    override fun sample(rng: Rng): S = func(distr.sample(rng))
}

/**
 * Helper interface to extend or sample strings from random distributions.
 */
public interface SampleString {
    public fun appendString(rng: Rng, string: StringBuilder, len: Int)

    public fun sampleString(rng: Rng, len: Int): String {
        val sb = StringBuilder(len)
        appendString(rng, sb, len)
        return sb.toString()
    }
}
