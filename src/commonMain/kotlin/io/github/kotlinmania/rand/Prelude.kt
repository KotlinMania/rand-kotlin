// port-lint: source rand/src/prelude.rs
package io.github.kotlinmania.rand

/**
 * Convenience re-exports of common Rand members.
 */
public object Prelude {
    public fun rng(): Rng = Rand.rng()
}
